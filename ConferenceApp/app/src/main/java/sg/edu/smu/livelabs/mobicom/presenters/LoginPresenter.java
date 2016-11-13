package sg.edu.smu.livelabs.mobicom.presenters;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import autodagger.AutoComponent;
import automortar.AutoScreen;
import flow.Flow;
import mortar.MortarScope;
import mortar.ViewPresenter;
import rx.functions.Action0;
import sg.edu.smu.livelabs.mobicom.AppDependencies;
import sg.edu.smu.livelabs.mobicom.DaggerScope;
import sg.edu.smu.livelabs.mobicom.MainActivity;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.UIHelper;
import sg.edu.smu.livelabs.mobicom.busEvents.LoginFail;
import sg.edu.smu.livelabs.mobicom.busEvents.LoginSuccessFul;
import sg.edu.smu.livelabs.mobicom.flow.Layout;
//import sg.edu.smu.livelabs.mobicom.presenters.screen.Profile1Screen;
import sg.edu.smu.livelabs.mobicom.views.LoginView;

/**
 * Created by smu on 21/3/16.
 */
@AutoScreen(
        component = @AutoComponent(dependencies = MainActivity.class, superinterfaces = AppDependencies.class),
        screenAnnotations = Layout.class
)
@DaggerScope(LoginPresenter.class)
@Layout(R.layout.login_view)
public class LoginPresenter extends ViewPresenter<LoginView> implements View.OnClickListener{

    private MainActivity mainActivity;
    private Bus bus;
    private String email;
    private String password;
    private Context context;
    private EmailValidator emailValidator;

    public LoginPresenter(MainActivity mainActivity, Bus bus){
        this.mainActivity = mainActivity;
        this.bus = bus;
        emailValidator = new EmailValidator();
    }

    @Override
    protected void onLoad(Bundle savedInstanceState) {
        super.onLoad(savedInstanceState);
        if (!hasView()) return;
        context = getView().getContext();
        getView().login2Layout.setVisibility(View.GONE);
        getView().login1Layout.setVisibility(View.VISIBLE);
        getView().nextBtn.setOnClickListener(this);
        getView().confirmBtn.setOnClickListener(this);
        if (MainActivity.mode == MainActivity.OFFLINE_MODE){
            UIHelper.getInstance().showAlert(context, context.getString(R.string.please_check_internet), false);
        }
    }

    @Override
    protected void onEnterScope(MortarScope scope) {
        super.onEnterScope(scope);
        bus.register(this);
    }

    @Override
    protected void onExitScope() {
        super.onExitScope();
        bus.unregister(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id){
            case R.id.next_btn:
                if (MainActivity.mode == MainActivity.OFFLINE_MODE){
                    UIHelper.getInstance().showAlert(context, context.getString(R.string.please_check_internet), false);
                    return;
                }
                email = getView().emailTxt.getText().toString();
                if (checkEmail(email)){
                    getView().login1Layout.setVisibility(View.GONE);
                    getView().login2Layout.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.confirm_btn:
                password = getView().passText.getText().toString();
                if (checkPassword(password)){
                    UIHelper.getInstance().showProgressDialog(context, context.getString(R.string.progressing), false);
//                    ChatService.getInstance().login(email, password, true);
                }
                break;
            //TODO SKIP USER
        }
    }

    @Subscribe
    public void loginEvent(LoginSuccessFul loginSuccessFul){
        //TODO
        mainActivity.registerGCM();
        mainActivity.syncData(true);
        UIHelper.getInstance().dismissProgressDialog();
//        mainActivity.goToMainPage();
//        Flow.get(context).set(new Profile1Screen());
//        Flow.get(context).set(new AvatarScreen());
    }

    @Subscribe
    public void loginFailEvent(LoginFail loginFail){
        UIHelper.getInstance().dismissProgressDialog();
        UIHelper.getInstance().showAlert(context, context.getString(R.string.app_name),
                context.getString(R.string.login_fail), true, new Action0() {
                    @Override
                    public void call() {
                        getView().login1Layout.setVisibility(View.VISIBLE);
                        getView().login2Layout.setVisibility(View.GONE);
                    }
                });
    }

    private boolean checkPassword(String password) {
        //TODO
        return true;
    }

    private boolean checkEmail(String email) {
        //TODO
        if (email == null || email.isEmpty()){
            UIHelper.getInstance().showAlert(context, context.getString(R.string.please_enter_email));
            return false;
        } else {
            if (!emailValidator.validate(email)){
                UIHelper.getInstance().showAlert(context, context.getString(R.string.please_check_your_email));
                return false;
            } else {
                return true;
            }
        }
    }

    class EmailValidator {

        private Pattern pattern;
        private Matcher matcher;

        private static final String EMAIL_PATTERN =
                "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

        public EmailValidator() {
            pattern = Pattern.compile(EMAIL_PATTERN);
        }

        /**
         * Validate hex with regular expression
         *
         * @param hex
         *            hex for validation
         * @return true valid hex, false invalid hex
         */
        public boolean validate(final String hex) {

            matcher = pattern.matcher(hex);
            return matcher.matches();

        }
    }
}
