package sg.edu.smu.livelabs.mobicom.presenters;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.View;

import com.flyco.animation.SlideEnter.SlideTopEnter;
import com.flyco.animation.SlideExit.SlideBottomExit;
import com.flyco.dialog.listener.OnBtnClickL;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import autodagger.AutoComponent;
import automortar.AutoScreen;
import flow.Flow;
import mortar.MortarScope;
import mortar.ViewPresenter;
import rx.functions.Action1;
import sg.edu.smu.livelabs.mobicom.ActionBarOwner;
import sg.edu.smu.livelabs.mobicom.AppDependencies;
import sg.edu.smu.livelabs.mobicom.DaggerScope;
import sg.edu.smu.livelabs.mobicom.MainActivity;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.UIHelper;
import sg.edu.smu.livelabs.mobicom.busEvents.LoginFail;
import sg.edu.smu.livelabs.mobicom.busEvents.LoginSuccessFul;
import sg.edu.smu.livelabs.mobicom.flow.Layout;
import sg.edu.smu.livelabs.mobicom.presenters.screen.Profile1Screen;
import sg.edu.smu.livelabs.mobicom.qrScanner.QRScannerService;
import sg.edu.smu.livelabs.mobicom.services.ChatService;
import sg.edu.smu.livelabs.mobicom.views.Login2View;
import sg.edu.smu.livelabs.mobicom.views.MyDialog;

/**
 * Created by smu on 26/4/16.
 */
@AutoScreen(
        component = @AutoComponent(dependencies = MainActivity.class, superinterfaces = AppDependencies.class),
        screenAnnotations = Layout.class
)
@DaggerScope(Login2Presenter.class)
@Layout(R.layout.login2_view)
public class Login2Presenter extends ViewPresenter<Login2View> {
    private ConnectivityManager cm;
    private MainActivity mainActivity;
    private Context context;
    private Bus bus;
    private MyDialog alertDialog;
    private SharedPreferences sharedPreferences;
    private ActionBarOwner actionBarOwner;
    public Login2Presenter(MainActivity mainActivity, Bus bus,
                           ActionBarOwner actionBarOwner){
        this.bus = bus;
        this.mainActivity = mainActivity;
        this.actionBarOwner = actionBarOwner;
    }

    @Override
    protected void onLoad(Bundle savedInstanceState) {
        super.onLoad(savedInstanceState);
        if (!hasView()) return;
        context = getView().getContext();
        mainActivity.setVisibleBottombar(View.GONE);
        mainActivity.setVisibleToolbar(View.GONE);
        actionBarOwner.setConfig(new ActionBarOwner.Config(false, "Login", null));
        cm = (ConnectivityManager) getView().getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
//        sharedPreferences = getView().getContext().getSharedPreferences(MainActivity.SHARE_PREFERENCES, Context.MODE_PRIVATE);
//        boolean acceptedTerm = sharedPreferences.getBoolean("acceptedTerm", false);
//        if (!acceptedTerm){
//            showTerm();
//        }
        getView().scanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnectedOrConnecting()) {
                    QRScannerService.getInstance().requestScan(mainActivity, new Action1<String>() {
                        @Override
                        public void call(String s) {
                            login("10013");
                        }
                    });
                }
            }
        });
    }

    private void login(String qrCode) {
        UIHelper.getInstance().showProgressDialog(context, "Logging you in", false);
        ChatService.getInstance().login2(qrCode, true, false);
    }

    @Override
    protected void onEnterScope(MortarScope scope) {
        super.onEnterScope(scope);
        bus.register(this);
    }

    @Override
    protected void onExitScope() {
        bus.unregister(this);
        super.onExitScope();
    }

    @Subscribe
    public void loginEvent(LoginSuccessFul loginSuccessFul){
        //TODO
        UIHelper.getInstance().dismissProgressDialog();
        mainActivity.registerGCM();
        mainActivity.syncData(true);
        mainActivity.startSessionServices();
        UIHelper.getInstance().dismissProgressDialog();
        Flow.get(context).set(new Profile1Screen());
    }

    @Subscribe
    public void loginFailEvent(LoginFail loginFail){
        UIHelper.getInstance().dismissProgressDialog();
        UIHelper.getInstance().showAlert(context, context.getString(R.string.app_name),
                context.getString(R.string.login_fail), true);
    }

    private void showTerm(){
        try {
            alertDialog = new MyDialog(context)
                    .style(MyDialog.STYLE_TWO)
                    .btnNum(2)
                    .title("Terms of Service")
                    .content("By continuing this application, you agree with the Terms of service:\n(Tap here to view)\nhttps://smu.sg/mobisystnc")
                    .btnText("Disagree", "Agree")
                    .showAnim(SlideTopEnter.class.newInstance())
                    .dismissAnim(SlideBottomExit.class.newInstance());
            alertDialog.setLink(mainActivity, "https://smu.sg/mobisystnc");
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.show();
            alertDialog.setOnBtnClickL(new OnBtnClickL() {
                                           @Override
                                           public void onBtnClick() {
                                               alertDialog.dismiss();
                                               Flow.get(getView()).goBack();
                                               mainActivity.finish();
                                               System.exit(0);
                                           }
                                       }, new OnBtnClickL() { //accepted
                                           @Override
                                           public void onBtnClick() {
                                               alertDialog.dismiss();
                                               SharedPreferences.Editor editor = sharedPreferences.edit();
                                               editor.putBoolean("acceptedTerm", true);
                                               editor.commit();
                                           }
                                       }
            );

        }
        catch (Exception e){}

    }
}
