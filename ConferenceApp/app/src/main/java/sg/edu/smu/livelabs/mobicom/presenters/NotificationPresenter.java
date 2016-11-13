package sg.edu.smu.livelabs.mobicom.presenters;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import autodagger.AutoComponent;
import automortar.AutoScreen;
import automortar.ScreenParam;
import mortar.MortarScope;
import mortar.ViewPresenter;
import rx.functions.Action0;
import sg.edu.smu.livelabs.mobicom.ActionBarOwner;
import sg.edu.smu.livelabs.mobicom.AppDependencies;
import sg.edu.smu.livelabs.mobicom.DaggerScope;
import sg.edu.smu.livelabs.mobicom.MainActivity;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.UIHelper;
import sg.edu.smu.livelabs.mobicom.flow.Layout;
import sg.edu.smu.livelabs.mobicom.net.response.SimpleResponse2;
import sg.edu.smu.livelabs.mobicom.services.AgendaService;
import sg.edu.smu.livelabs.mobicom.views.NotificationView;

/**
 * Created by smu on 1/6/16.
 */
@AutoScreen(
        component = @AutoComponent(dependencies = MainActivity.class, superinterfaces = AppDependencies.class),
        screenAnnotations = Layout.class
)
@DaggerScope(NotificationPresenter.class)
@Layout(R.layout.notification_view)
public class NotificationPresenter extends ViewPresenter<NotificationView> {
    private Context context;
    private MainActivity mainActivity;
    private ActionBarOwner actionBarOwner;
    private Bus bus;
    private Integer[] selected;
    private String groups;
    private String message;
    private String[] groupUsers;



    public NotificationPresenter(MainActivity mainActivity, ActionBarOwner actionBarOwner, Bus bus,
                                 @ScreenParam String[] groupUsers){
        this.actionBarOwner = actionBarOwner;
        this.bus = bus;
        this.mainActivity = mainActivity;
        this.groupUsers = groupUsers;
    }

    @Override
    protected void onEnterScope(MortarScope scope) {
        super.onEnterScope(scope);
        mainActivity.setVisibleBottombar(View.GONE);
        bus.register(this);
    }

    @Override
    protected void onExitScope() {
        bus.unregister(this);
        mainActivity.setVisibleBottombar(View.VISIBLE);
        super.onExitScope();
    }

    @Override
    protected void onLoad(Bundle savedInstanceState) {
        super.onLoad(savedInstanceState);
        if (!hasView()){
            return;
        }
        context = getView().getContext();
        groups = null;
        message = null;
        actionBarOwner.setConfig(new ActionBarOwner.Config(true, "Notification",
                new ActionBarOwner.MenuAction("Send", new Action0() {
                    @Override
                    public void call() {
                        if (groups == null){
                            UIHelper.getInstance().showAlert(context, "Please chose at least one group.");
                        } else {
                            if (getView().message.getText() != null){
                                message = getView().message.getText().toString().trim();
                                if (!message.isEmpty()){
                                    UIHelper.getInstance().showProgressDialog(context, context.getString(R.string.progressing), true);
                                    AgendaService.getInstance().sendNotifyUser(message, groups);
                                    return;
                                }
                            }
                            UIHelper.getInstance().showAlert(context, "Please type a message.");
                        }
                    }
                })));
        getView().selectUserpoolBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MaterialDialog.Builder(context)
                        .items(groupUsers)
                        .itemsCallbackMultiChoice(selected, new MaterialDialog.ListCallbackMultiChoice() {
                            @Override
                            public boolean onSelection(MaterialDialog dialog, Integer[] which, CharSequence[] text) {
                                if (text == null || text.length == 0){
                                    UIHelper.getInstance().showAlert(context, "Please chose at least one group.");
                                } else {
                                    groups = "";
                                    for (CharSequence str: text) {
                                        groups += str + ",";
                                    }
                                    groups = groups.substring(0, groups.length() - 1);
                                    getView().selectUserpoolBtn.setText(groups);
                                    selected = which;
                                }
                                return true;
                            }
                        })
                        .dividerColorRes(R.color.divider)
                        .positiveText("OK")
                        .negativeText("Cancel")
                        .show();
            }
        });
    }

    @Subscribe
    public void sendMessageEvent(SimpleResponse2 simpleResponse2){
        UIHelper.getInstance().dismissProgressDialog();
        if ("success".equals(simpleResponse2.status)){
            UIHelper.getInstance().showAlert(context, "Sent Successfully.");
            AgendaService.getInstance().saveOutbox(message);
            groups = null;
            message = null;
            selected = null;
            getView().message.setText("");
            getView().selectUserpoolBtn.setText("Select userpool to send");
        }
        else {
            UIHelper.getInstance().showAlert(context, "Fail.");
        }
    }
}
