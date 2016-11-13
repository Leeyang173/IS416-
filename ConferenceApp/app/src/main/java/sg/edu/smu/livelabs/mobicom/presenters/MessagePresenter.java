package sg.edu.smu.livelabs.mobicom.presenters;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import autodagger.AutoComponent;
import automortar.AutoScreen;
import automortar.ScreenParam;
import flow.Flow;
import flow.path.Path;
import mortar.MortarScope;
import mortar.ViewPresenter;
import rx.functions.Action0;
import sg.edu.smu.livelabs.mobicom.ActionBarOwner;
import sg.edu.smu.livelabs.mobicom.App;
import sg.edu.smu.livelabs.mobicom.AppDependencies;
import sg.edu.smu.livelabs.mobicom.DaggerScope;
import sg.edu.smu.livelabs.mobicom.MainActivity;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.adapters.SlidePagerAdapter;
import sg.edu.smu.livelabs.mobicom.busEvents.MessageReloadEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.ReloadRecommendedUserEvent;
import sg.edu.smu.livelabs.mobicom.flow.Layout;
import sg.edu.smu.livelabs.mobicom.models.data.ChatRoomEntity;
import sg.edu.smu.livelabs.mobicom.presenters.screen.ChatListScreen;
import sg.edu.smu.livelabs.mobicom.presenters.screen.ChatScreen;
import sg.edu.smu.livelabs.mobicom.presenters.screen.ContactsScreen;
import sg.edu.smu.livelabs.mobicom.presenters.screen.RecommendScreen;
import sg.edu.smu.livelabs.mobicom.services.ChatService;
import sg.edu.smu.livelabs.mobicom.services.ScreenService;
import sg.edu.smu.livelabs.mobicom.services.TrackingService;
import sg.edu.smu.livelabs.mobicom.views.MessageView;

/**
 * Created by smu on 4/4/16.
 */
@AutoScreen(
        component = @AutoComponent(dependencies = MainActivity.class, superinterfaces = AppDependencies.class),
        screenAnnotations = Layout.class
)
@DaggerScope(MessagePresenter.class)
@Layout(R.layout.message_view)
public class MessagePresenter extends ViewPresenter<MessageView> {

    private static String NAME = "MessagePresenter";
    private MainActivity mainActivity;
    private ActionBarOwner actionBarOwner;
    private ScreenService screenService;
    private Context context;
    private Bus bus;
    private SlidePagerAdapter adapter;
    private int focus;
    private boolean fromMainMenu;

    public MessagePresenter(MainActivity mainActivity, ActionBarOwner actionBarOwner,
                            ScreenService screenService, Bus bus, @ScreenParam int focus){
        this.mainActivity = mainActivity;
        this.actionBarOwner = actionBarOwner;
        this.screenService = screenService;
        this.fromMainMenu = fromMainMenu;
        this.bus = bus;
        this.focus = focus;
        if (this.focus == -1){
            this.focus = ActionBarOwner.Config.MIDDLE_FOCUS;
        }
    }

    @Override
    protected void onLoad(Bundle savedInstanceState) {
        super.onLoad(savedInstanceState);
        if(!hasView()) return;
        context = getView().getContext();
        Long justCreatedChatRoomId = null;
        try {
            justCreatedChatRoomId = screenService.pop(AddGroupChatPresenter.class);
        }
        catch (Throwable e){
            Log.d("MSR", "MessagePresenter:"+e.toString());
        }
        if (justCreatedChatRoomId != null) {
            ChatRoomEntity chatRoomEntity = ChatService.getInstance()
                    .findGroupChatRoom(justCreatedChatRoomId, false);
            if (chatRoomEntity != null) {
                Flow.get(getView()).set(new ChatScreen(chatRoomEntity));
            }
        }
        Path[] paths = new Path[]{new ContactsScreen(mainActivity),
                new ChatListScreen(),
                new RecommendScreen()};
        adapter = new SlidePagerAdapter(context, paths);
        getView().viewPager.setAdapter(adapter);
        setSelectTab();
    }

    @Override
    protected void onExitScope() {
        bus.unregister(this);
        super.onExitScope();
        App.getInstance().previousPresenter = "";
    }

    @Override
    protected void onEnterScope(MortarScope scope) {
        bus.register(this);
        super.onEnterScope(scope);
        if(App.getInstance().currentPresenter.equals(MorePresenter.NAME)){
            App.getInstance().setPrevious();
        }
        App.getInstance().currentPresenter = NAME;
    }

    @Subscribe
    public void changeFocusEvent(MessageReloadEvent messageReloadEvent){
        try {
            screenService.push(MessagePresenter.class, this.focus);
        }
        catch (Throwable e){
            Log.d("AAA","MessagePresenter:"+e.toString());
        }
        this.focus = messageReloadEvent.focus;
        if (this.focus == -1){
            this.focus = ActionBarOwner.Config.MIDDLE_FOCUS;
        }
        setSelectTab();
    }

    private void setSelectTab(){
        mainActivity.agendaBtn.setSelected(false);
        mainActivity.homeBtn.setSelected(false);
        mainActivity.moreBtn.setSelected(false);
        mainActivity.messageBtn.setSelected(true);
        mainActivity.gamesBtn.setSelected(false);
        mainActivity.currentTab = MainActivity.MESSAGE_TAB;

        actionBarOwner.setConfig(new ActionBarOwner.Config(true,
                new ActionBarOwner.MenuAction(context.getString(R.string.attendees), new Action0() {
                    @Override
                    public void call() {
                        if (!hasView()) return;
                        getView().viewPager.setCurrentItem(0);
                        TrackingService.getInstance().sendTracking("202", "messages", "attendees", "", "", "");
                    }
                }),
                new ActionBarOwner.MenuAction(context.getString(R.string.messages), new Action0() {
                    @Override
                    public void call() {
                        if (!hasView()) return;
                        getView().viewPager.setCurrentItem(1);
                        TrackingService.getInstance().sendTracking("206", "messages", "messages", "", "", "");
                    }
                })
                , new ActionBarOwner.MenuAction(context.getString(R.string.recommend), new Action0() {
            @Override
            public void call() {
                if (!hasView()) return;
                getView().viewPager.setCurrentItem(2);
                bus.post(new ReloadRecommendedUserEvent());
                TrackingService.getInstance().sendTracking("210", "messages", "recommend", "", "", "");
            }
        }), focus));
        getView().viewPager.setCurrentItem(focus);
    }

    public boolean canGoback(){
        try{
            this.focus = screenService.pop(MessagePresenter.class);
            setSelectTab();
            return true;
        } catch (Exception e){
            return false;
        }
    }
}
