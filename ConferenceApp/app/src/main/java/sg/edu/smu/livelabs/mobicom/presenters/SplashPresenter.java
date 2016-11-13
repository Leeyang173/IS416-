package sg.edu.smu.livelabs.mobicom.presenters;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;

import autodagger.AutoComponent;
import automortar.AutoScreen;
import automortar.ScreenParam;
import flow.Flow;
import mortar.MortarScope;
import mortar.ViewPresenter;
import sg.edu.smu.livelabs.mobicom.ActionBarOwner;
import sg.edu.smu.livelabs.mobicom.App;
import sg.edu.smu.livelabs.mobicom.AppDependencies;
import sg.edu.smu.livelabs.mobicom.DaggerScope;
import sg.edu.smu.livelabs.mobicom.MainActivity;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.UIHelper;
import sg.edu.smu.livelabs.mobicom.busEvents.LoginFail;
import sg.edu.smu.livelabs.mobicom.busEvents.LoginSuccessFul;
import sg.edu.smu.livelabs.mobicom.flow.Layout;
import sg.edu.smu.livelabs.mobicom.models.User;
import sg.edu.smu.livelabs.mobicom.models.data.DaoMaster;
import sg.edu.smu.livelabs.mobicom.net.RestClient;
import sg.edu.smu.livelabs.mobicom.presenters.screen.IRBScreen;
import sg.edu.smu.livelabs.mobicom.presenters.screen.Login2Screen;
import sg.edu.smu.livelabs.mobicom.presenters.screen.TACScreen;
import sg.edu.smu.livelabs.mobicom.services.ChatService;
import sg.edu.smu.livelabs.mobicom.services.DatabaseService;
import sg.edu.smu.livelabs.mobicom.services.ScreenService;
import sg.edu.smu.livelabs.mobicom.views.SplashView;

/**
 * Created by smu on 21/1/16.
 */
@AutoScreen(
        component = @AutoComponent(dependencies = MainActivity.class, superinterfaces = AppDependencies.class),
        screenAnnotations = Layout.class
)
@DaggerScope(SplashPresenter.class)
@Layout(R.layout.splash_view)
public class SplashPresenter extends ViewPresenter<SplashView>{
    private static final int SPLASH_TIME = 1500;
    private ActionBarOwner actionBarOwner;
    private MainActivity mainActivity;
    private Bus bus;
    private ScreenService screenService;
    private Context context;

    private String notificationType;
    private int notificationId;
    private Object notificationData;
    private long currentTime;
    private Handler handler ;

    public SplashPresenter(ActionBarOwner actionBarOwner, Bus bus,
                         ScreenService screenService, MainActivity mainActivity,
                         @ScreenParam String notificationType, @ScreenParam int notificationId,
                         @ScreenParam Object notificationData) {
        this.actionBarOwner = actionBarOwner;
        this.mainActivity = mainActivity;
        this.bus = bus;
        this.screenService = screenService;
        this.notificationType = notificationType;
        this.notificationId = notificationId;
        this.notificationData = notificationData;
    }

    @Override
    protected void onLoad(Bundle savedInstanceState) {
        super.onLoad(savedInstanceState);
        if (!hasView()){
            return;
        }
        Log.d(App.APP_TAG, " SplashPresenter onload");
        context = getView().getContext();
        actionBarOwner.setConfig(new ActionBarOwner.Config(false, null));

        try{
            Picasso.with(context).load(R.drawable.login_bg).fit().centerCrop().into(getView().bgIV);
        }
        catch(OutOfMemoryError e){
            Log.d("FacultySummit", "SplashPresenter:"+e.toString());
        }

        mainActivity.setVisibleBottombar(View.GONE);
        mainActivity.setVisibleToolbar(View.GONE);
        currentTime = System.currentTimeMillis();
        handler = new Handler();
        //Login
        User user = DatabaseService.getInstance().getMe();
        if (user.getUID() != -1){
            ChatService.getInstance().login2(user.getQrCode(), false, false);
        } else {
            //first time login

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    SharedPreferences sharedPreferences = context.getSharedPreferences(MainActivity.SHARE_PREFERENCES, Context.MODE_APPEND);
                    boolean acceptIRB = sharedPreferences.getBoolean("IRB_accept", false);
                    boolean acceptedTAC = sharedPreferences.getBoolean("TAC_accept", false);
                    if(!acceptedTAC) {
                        Flow.get(context).set(new TACScreen());
                    } else if (!acceptIRB){
                        Flow.get(context).set(new IRBScreen());
                    } else {
                        Flow.get(context).set(new Login2Screen());
                    }

                }
            }, SPLASH_TIME);

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

    private void startApp(){
        long wait = System.currentTimeMillis() - currentTime;
        if ( wait >= SPLASH_TIME){
            mainActivity.goToMainPage();
            if (notificationType != null){
                processNotification();
            }
        } else {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mainActivity.goToMainPage();
                    if (notificationType != null){
                        processNotification();
                    }
                }
            }, SPLASH_TIME - wait);
        }
    }

    private void processNotification() {
        Log.d(App.APP_TAG, "SplashPresenter Has view: processNotification" + hasView());
        if (hasView()) {
            Handler h = new Handler(Looper.getMainLooper());
            h.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Log.d(App.APP_TAG, "SplashPresenter Has view: processNotification" + hasView());

                    mainActivity.processNotification(context, notificationType, notificationData);
                    notificationType = null;
                    notificationId = 0;
                    notificationData = null;
                }
            }, 1000);
        } else {
            Handler h = new Handler(Looper.getMainLooper());
            h.postDelayed(new Runnable() {
                @Override
                public void run() {
                    processNotification();
                }
            }, 200);
        }
    }

    @Subscribe
    public void loginSuccessfull(LoginSuccessFul loginSuccessFul){
        mainActivity.registerGCM();
        if(loginSuccessFul.firstTimeLogin && !loginSuccessFul.onResume) {
            long wait = System.currentTimeMillis() - currentTime;
            if (wait >= SPLASH_TIME) {
                mainActivity.goToMainPage();
            } else {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mainActivity.goToMainPage();
                    }
                }, SPLASH_TIME - wait);
            }
        }
        else if(!loginSuccessFul.onResume){
            SharedPreferences sharedPreferences = context.getSharedPreferences(MainActivity.SHARE_PREFERENCES, Context.MODE_PRIVATE);
            int dataVersion = sharedPreferences.getInt("SCHEMA_VERSION", 0);
            if (dataVersion < DaoMaster.SCHEMA_VERSION){
                sharedPreferences.edit().putInt("SCHEMA_VERSION", DaoMaster.SCHEMA_VERSION).commit();
                mainActivity.syncData(true);
            } else {
                mainActivity.syncData(false);
            }
            startApp();
        }
    }

    private void showCheckLogin(){

    }

    @Subscribe
    public void loginFailEvent(LoginFail loginFail){
        MainActivity.mode = MainActivity.OFFLINE_MODE;
        startApp();
    }

}
