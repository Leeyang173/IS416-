package sg.edu.smu.livelabs.mobicom.presenters;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;

import com.marshalchen.ultimaterecyclerview.stickyheadersrecyclerview.StickyRecyclerHeadersDecoration;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import autodagger.AutoComponent;
import automortar.AutoScreen;
import de.greenrobot.dao.query.LazyList;
import flow.Flow;
import mortar.MortarScope;
import mortar.ViewPresenter;
import rx.functions.Action0;
import sg.edu.smu.livelabs.mobicom.ActionBarOwner;
import sg.edu.smu.livelabs.mobicom.App;
import sg.edu.smu.livelabs.mobicom.AppDependencies;
import sg.edu.smu.livelabs.mobicom.DaggerScope;
import sg.edu.smu.livelabs.mobicom.MainActivity;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.UIHelper;
import sg.edu.smu.livelabs.mobicom.adapters.InboxAdapter;
import sg.edu.smu.livelabs.mobicom.busEvents.InboxEvent;
import sg.edu.smu.livelabs.mobicom.flow.Layout;
import sg.edu.smu.livelabs.mobicom.models.data.InboxEntity;
import sg.edu.smu.livelabs.mobicom.net.response.UserPoolGroupResponse;
//import sg.edu.smu.livelabs.mobicom.presenters.screen.NotificationScreen;
import sg.edu.smu.livelabs.mobicom.presenters.screen.NotificationScreen;
import sg.edu.smu.livelabs.mobicom.services.AgendaService;
import sg.edu.smu.livelabs.mobicom.views.InboxView;

/**
 * Created by smu on 30/5/16.
 */
@AutoScreen(
        component = @AutoComponent(dependencies = MainActivity.class, superinterfaces = AppDependencies.class),
        screenAnnotations = Layout.class
)
@DaggerScope(InboxPresenter.class)
@Layout(R.layout.inbox_view)
public class InboxPresenter extends ViewPresenter<InboxView> implements View.OnClickListener{
    public final static String NAME = "InboxPresenter";
    public final static int MAX_LENGHT_MSG = 70;
    public static int CURRENT_TAB;

    private Bus bus;
    private InboxAdapter inboxAdapter;
    private Context context;
    private ActionBarOwner actionBarOwner;
    private MainActivity mainActivity;
    private String[] groups;


    public InboxPresenter(ActionBarOwner actionBarOwner, Bus bus, MainActivity mainActivity) {
        this.actionBarOwner = actionBarOwner;
        this.bus = bus;
        this.mainActivity = mainActivity;
    }

    @Override
    protected void onLoad(Bundle savedInstanceState) {
        super.onLoad(savedInstanceState);
        if (!hasView()){
            return;
        }
        context = getView().getContext();
        inboxAdapter = new InboxAdapter(context);
        getView().ultimateRecyclerView.setHasFixedSize(false);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        getView().ultimateRecyclerView.setLayoutManager(linearLayoutManager);
        getView().ultimateRecyclerView.setAdapter(inboxAdapter);
        StickyRecyclerHeadersDecoration headersDecor = new StickyRecyclerHeadersDecoration(inboxAdapter);
        getView().ultimateRecyclerView.addItemDecoration(headersDecor);
        getView().ultimateRecyclerView.disableLoadmore();
        setData(AgendaService.getInstance().getAllInbox());
        UIHelper.getInstance().showProgressDialog(context, context.getString(R.string.progressing), true );
        AgendaService.getInstance().getUserPoolGroup();
        AgendaService.getInstance().getInbox();

    }

    @Override
    protected void onEnterScope(MortarScope scope) {
        super.onEnterScope(scope);
        App.getInstance().currentPresenter = NAME;
        mainActivity.currentTab = MainActivity.OTHER_TAB;
        bus.register(this);
    }

    @Override
    protected void onExitScope() {
        inboxAdapter.closeData();
        App.getInstance().currentPresenter = "";
        bus.unregister(this);
        super.onExitScope();

    }

    @Subscribe
    public void refreshInboxData(InboxEvent event){
        if (CURRENT_TAB == AgendaService.INBOX){
            setData(AgendaService.getInstance().getAllInbox());
        }
    }

    @Subscribe
    public void showGroupUserPoolEvent(UserPoolGroupResponse userPoolGroupResponse){
        UIHelper.getInstance().dismissProgressDialog();
        if ("success".equals(userPoolGroupResponse.status)  && userPoolGroupResponse.details != null){
            if (userPoolGroupResponse.canSend){
                groups = new String[userPoolGroupResponse.details.size()];
                groups = userPoolGroupResponse.details.toArray(groups);
                actionBarOwner.setConfig(new ActionBarOwner.Config(true, null,
                        new ActionBarOwner.MenuAction("Inbox", new Action0() {
                            @Override
                            public void call() {
                                App.getInstance().currentPresenter = NAME;
                                CURRENT_TAB = AgendaService.INBOX;
                                setData(AgendaService.getInstance().getAllInbox());
                                getView().markAllAsReadBtn.setVisibility(View.VISIBLE);
                            }
                        }),
                        new ActionBarOwner.MenuAction("Outbox", new Action0() {
                            @Override
                            public void call() {
                                CURRENT_TAB = AgendaService.OUTBOX;
                                App.getInstance().currentPresenter = "";
                                setData(AgendaService.getInstance().getAllOutbox());
                                getView().markAllAsReadBtn.setVisibility(View.GONE);
                            }
                        }),
                        new ActionBarOwner.MenuAction(R.drawable.icon_new_selected_w, new Action0() {
                            @Override
                            public void call() {
                                Flow.get(context).set(new NotificationScreen(groups));
                            }
                        }),
                        ActionBarOwner.Config.LEFT_FOCUS));
                return;
            }
        }
        actionBarOwner.setConfig(new ActionBarOwner.Config(true, "Inbox", null));

    }

    private void setData(LazyList<InboxEntity> data){
        if (data == null || data.size() == 0){
            getView().ultimateRecyclerView.setVisibility(View.GONE);
        } else {
            inboxAdapter.setData(data);
            getView().ultimateRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.mark_all_as_read_view){
            UIHelper.getInstance().showConfirmAlert(context, context.getString(R.string.app_name),
                    context.getString(R.string.mask_all_as_read), "Yes", "No",
                    new Action0() {
                        @Override
                        public void call() {
                            AgendaService.getInstance().markAllAsRead();
                        }
                    },
                    new Action0() {
                        @Override
                        public void call() {

                        }
                    });
        }
    }
}
