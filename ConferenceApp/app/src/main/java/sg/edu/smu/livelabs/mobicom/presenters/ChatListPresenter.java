package sg.edu.smu.livelabs.mobicom.presenters;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import autodagger.AutoComponent;
import automortar.AutoScreen;
import flow.Flow;
import mortar.MortarScope;
import mortar.ViewPresenter;
import sg.edu.smu.livelabs.mobicom.App;
import sg.edu.smu.livelabs.mobicom.AppDependencies;
import sg.edu.smu.livelabs.mobicom.DaggerScope;
import sg.edu.smu.livelabs.mobicom.MainActivity;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.UIHelper;
import sg.edu.smu.livelabs.mobicom.adapters.ChatListAdapter;
import sg.edu.smu.livelabs.mobicom.busEvents.ChatMessageReceivedEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.ChatRoomDeleted;
import sg.edu.smu.livelabs.mobicom.busEvents.ChatRoomLeft;
import sg.edu.smu.livelabs.mobicom.busEvents.ChatRoomMessageSynced;
import sg.edu.smu.livelabs.mobicom.busEvents.ChatRoomUpdatedEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.ChatRoomsSyncEvent;
import sg.edu.smu.livelabs.mobicom.flow.Layout;
import sg.edu.smu.livelabs.mobicom.models.data.ChatRoomEntity;
import sg.edu.smu.livelabs.mobicom.presenters.screen.AddGroupChatScreen;
import sg.edu.smu.livelabs.mobicom.services.ChatService;
import sg.edu.smu.livelabs.mobicom.services.TrackingService;
import sg.edu.smu.livelabs.mobicom.views.ChatListView;

/**
 * Created by smu on 8/3/16.
 */
@AutoScreen(
        component = @AutoComponent(dependencies = MessagePresenter.class, superinterfaces = AppDependencies.class),
        screenAnnotations = Layout.class
)
@DaggerScope(ChatListPresenter.class)
@Layout(R.layout.chat_list_view)
public class ChatListPresenter extends ViewPresenter<ChatListView>{
    private  Bus bus;
    private Context context;
    private ChatListAdapter adapter;
    private Handler handler;
    private Runnable runnable;

    public ChatListPresenter(Bus bus) {
        this.bus = bus;
    }

    @Override
    protected void onEnterScope(MortarScope scope) {
        bus.register(this);
        App.getInstance().getMainActivity().setVisibleBottombar(View.VISIBLE);
        if(App.getInstance().currentPresenter.equals(MorePresenter.NAME)){
            App.getInstance().setPrevious();
            App.getInstance().currentPresenter = "ChatListPresenter";
        }
    }

    @Override
    protected void onExitScope() {
        bus.unregister(this);
        App.getInstance().previousPresenter = "";
    }

    @Override
    protected void onLoad(Bundle savedInstanceState) {
        super.onLoad(savedInstanceState);
        if (!hasView()) {
            return;
        }
        context = getView().getContext();
        adapter = new ChatListAdapter(context);
        adapter.setChats(new ArrayList<ChatRoomEntity>());
        getView().chatList.setHasFixedSize(false);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        getView().chatList.setLayoutManager(linearLayoutManager);
        getView().chatList.setAdapter(adapter);
        getView().chatList.disableLoadmore();
        getView().chatList.setDefaultOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshRemote();
            }
        });
        getView().addGroupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO
//                screenService.push(ConnectMainPresenter.class, ConnectMainPresenter.CHAT_TAB);
                Flow.get(getView()).set(new AddGroupChatScreen(null));
                TrackingService.getInstance().sendTracking("209", "messages", "messages", "", "", "");
            }
        });
        getView().searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                handler.removeCallbacks(runnable);
                handler.postDelayed(runnable, 1000);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                adapter.getFilter().filter(getView().searchText.getText().toString().trim().toLowerCase());
            }
        };
        refreshLocal();
        refreshRemote();
    }

    @Subscribe
    public void chatRoomDeleted(ChatRoomDeleted event) {
        UIHelper.getInstance().dismissProgressDialog();
        if (event.chatRoomEntity != null) {
            refreshLocal();
        } else {
            UIHelper.getInstance().showAlert(context, context.getString(R.string.delete_chat_room_error));
        }
    }

    @Subscribe
    public void chatRoomLeft(ChatRoomLeft event) {
        UIHelper.getInstance().dismissProgressDialog();
        if (event.chatRoomEntity != null) {
            refreshLocal();
        } else {
            UIHelper.getInstance().showAlert(context, context.getString(R.string.leave_chat_room_error));
        }
    }

    @Subscribe
    public void chatRoomsSynced(ChatRoomsSyncEvent event) {
        if (event.result) {
            refreshLocal();
        }
    }

    @Subscribe
    public void chatRoomMessagesSynced(ChatRoomMessageSynced event) {
        if (event.result) {
            refreshLocal();
        }
    }

    @Subscribe
    public void chatMessageReceived(ChatMessageReceivedEvent event) {
        refreshLocal();
    }

    @Subscribe
    public void chatRoomUpdated(ChatRoomUpdatedEvent event) {
        refreshLocal();
    }

    private void refreshLocal() {
        if (MainActivity.mode != MainActivity.SKIP_USER_MODE){
            List<ChatRoomEntity> result = ChatService.getInstance().getAllChatRooms();
            if (result != null){
                adapter.setChats(result);
//                getView().chatList.setRefreshing(false);
            }
        }
    }

    private void refreshRemote() {
        ChatService.getInstance().syncAllChats();
    }
}
