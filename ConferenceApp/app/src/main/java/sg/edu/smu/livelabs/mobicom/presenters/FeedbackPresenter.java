package sg.edu.smu.livelabs.mobicom.presenters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import autodagger.AutoComponent;
import automortar.AutoScreen;
import mortar.MortarScope;
import mortar.ViewPresenter;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import sg.edu.smu.livelabs.mobicom.ActionBarOwner;
import sg.edu.smu.livelabs.mobicom.AppDependencies;
import sg.edu.smu.livelabs.mobicom.DaggerScope;
import sg.edu.smu.livelabs.mobicom.MainActivity;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.UIHelper;
import sg.edu.smu.livelabs.mobicom.flow.Layout;
import sg.edu.smu.livelabs.mobicom.net.RestClient;
import sg.edu.smu.livelabs.mobicom.net.response.FeedbackResponse;
import sg.edu.smu.livelabs.mobicom.services.DatabaseService;
import sg.edu.smu.livelabs.mobicom.views.FeedbackView;

/**
 * Created by smu on 26/5/16.
 */
@AutoScreen(
        component = @AutoComponent(dependencies = MainActivity.class, superinterfaces = AppDependencies.class),
        screenAnnotations = Layout.class
)
@DaggerScope(FeedbackPresenter.class)
@Layout(R.layout.feedback_view)
public class FeedbackPresenter extends ViewPresenter<FeedbackView> {

    private static final String FEATURES = "Suggestion";
    private static final String BUG = "Report a bug";
    private String subject;
    private RestClient restClient;
    private ActionBarOwner actionBarOwner;
    private Context context;
    private MainActivity mainActivity;
    public FeedbackPresenter(MainActivity mainActivity, RestClient restClient, ActionBarOwner actionBarOwner){
        this.restClient = restClient;
        this.actionBarOwner = actionBarOwner;
        this.mainActivity = mainActivity;
    }

    @Override
    protected void onLoad(Bundle savedInstanceState) {
        super.onLoad(savedInstanceState);
        context = getView().getContext();
        mainActivity.currentTab = MainActivity.OTHER_TAB;
        actionBarOwner.setConfig(new ActionBarOwner.Config(true, "Feedback",
                new ActionBarOwner.MenuAction("Send", new Action0() {
                    @Override
                    public void call() {
                        String content = getView().content.getText().toString();
                        if (subject != null && content != null && content.length() > 0) {
                            if (MainActivity.mode == MainActivity.OFFLINE_MODE){
                                UIHelper.getInstance().showAlert(context, context.getString(R.string.please_check_internet));
                                return;
                            }
                            sendFeedBack();
                            getView().content.clearFocus();
                            InputMethodManager imm = (InputMethodManager) context.getSystemService(
                                    Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(getView().content.getWindowToken(), 0);
                            getView().type.setText("");
                            getView().content.setText("");
                            UIHelper.getInstance().showAlert(context, "Thank you for your feedback");
                        }
                    }
                })));
        getView().type.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Select feedback type");
                builder.setItems(
                        new CharSequence[]{FEATURES, BUG, "Others"},
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                if (which == 0) {
                                    subject = FEATURES;
                                } else if (which == 1) {
                                    subject = "Bug";
                                } else {
                                    subject = "Others";
                                }
                                getView().type.setText(subject);
                            }
                        });
                builder.show();
            }
        });
    }

    @Override
    protected void onEnterScope(MortarScope scope) {
        super.onEnterScope(scope);
        mainActivity.setVisibleBottombar(View.GONE);
    }

    @Override
    protected void onExitScope() {
        super.onExitScope();
        mainActivity.setVisibleBottombar(View.VISIBLE);
    }

    private void sendFeedBack() {
        subject += " (" + getDeviceName() + ", "
                + getAndroidVersion() + ")";
        String message = getView().content.getText().toString();
        long UID = DatabaseService.getInstance().getMe().getUID();
        if (UID != -1) {
            restClient.getFeedbackApi().sendFeedback(UID, "android", subject, message)
                    .subscribeOn(Schedulers.io())
                    .subscribe(new Action1<FeedbackResponse>() {
                        @Override
                        public void call(FeedbackResponse simpleResponse) {

                        }
                    });
        }
    }

    public String getAndroidVersion() {
        return Build.VERSION.RELEASE;
    }

    public String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }

    private String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }
}
