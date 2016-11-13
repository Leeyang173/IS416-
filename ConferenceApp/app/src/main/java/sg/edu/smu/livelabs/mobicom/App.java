package sg.edu.smu.livelabs.mobicom;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.support.multidex.MultiDex;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.otto.Bus;

import autodagger.AutoComponent;
import autodagger.AutoInjector;
import in.co.madhur.chatbubblesdemo.NativeLoader;
import io.fabric.sdk.android.Fabric;
import mortar.MortarScope;
import sg.edu.smu.livelabs.mobicom.busEvents.UpdateBeaconPresenterEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.UpdateNetworkEvent;
import sg.edu.smu.livelabs.mobicom.fileupload.UploadFileService;
import sg.edu.smu.livelabs.mobicom.net.RestClient;
import sg.edu.smu.livelabs.mobicom.services.AgendaService;
import sg.edu.smu.livelabs.mobicom.services.AttendeesService;
import sg.edu.smu.livelabs.mobicom.services.BEPService;
import sg.edu.smu.livelabs.mobicom.services.BeaconsService;
import sg.edu.smu.livelabs.mobicom.services.ChatService;
import sg.edu.smu.livelabs.mobicom.services.CoolfieCacheService;
import sg.edu.smu.livelabs.mobicom.services.DatabaseService;
import sg.edu.smu.livelabs.mobicom.services.EVAPromotionService;
import sg.edu.smu.livelabs.mobicom.services.FavoriteService;
import sg.edu.smu.livelabs.mobicom.services.ForumService;
import sg.edu.smu.livelabs.mobicom.services.GameService;
import sg.edu.smu.livelabs.mobicom.services.IceBreakerService;
import sg.edu.smu.livelabs.mobicom.services.InterestService;
import sg.edu.smu.livelabs.mobicom.services.MasterPointService;
import sg.edu.smu.livelabs.mobicom.services.MemoriesService;
import sg.edu.smu.livelabs.mobicom.services.PaperService;
import sg.edu.smu.livelabs.mobicom.services.ScavengerService;
import sg.edu.smu.livelabs.mobicom.services.SurveyService;
import sg.edu.smu.livelabs.mobicom.services.TrackingService;

/**
 * Created by smu on 21/1/16.
 */
@AutoComponent(
        superinterfaces = AppDependencies.class,
        modules = AppModule.class )
@AutoInjector
@DaggerScope(App.class)
public class App extends Application {
    public final static String APP_TAG = "mobicom";
    public final static String APP_ID = "1";
    public static String appVersion="";
    public static String appName="";
    private RestClient restClient;
    private Bus bus;
    private Gson gson;
    private MortarScope mortarScope;
    private static App instance;
    private MainActivity mainActivity;
    public static volatile Handler applicationHandler = null;
    public boolean loginFail = false;
    public boolean callingLoginFromOnResume = false;

    public long currentChat = -1;
    public int currentChatType = -1;
    public String currentPresenter = "";
    public String previousPresenter = "";
    public boolean isDemoSiteOpen = false;

    public static App getInstance() {
        return instance;
    }

    @Override
    public Object getSystemService(String name) {
        Log.d(APP_TAG, "App getSystemService: " + name);
        if (mortarScope == null) return super.getSystemService(name);
        return mortarScope.hasService(name) ? mortarScope.getService(name) : super.getSystemService(name);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(APP_TAG, "App onCreate");
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            this.appVersion = pInfo.versionName;
            this.appName = this.getResources().getString(R.string.app_name);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        applicationHandler = new Handler(getMainLooper());
        NativeLoader.initNativeLibs(this);
        UIHelper.getInstance().init(this);
        bus = new Bus();
        gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd HH:mm:ss")
                .create();
        instance = this;
        DatabaseService.getInstance().init(this);
        restClient = new RestClient();
        ChatService.getInstance().init(this, bus, restClient.getChatApi());
        AgendaService.getInstance().init(this, bus, restClient.getAgendaApi());
        UploadFileService.getInstance().init(this, restClient.getFileUploadApi());
        AttendeesService.getInstance().init(this, bus, restClient.getAttendeeApi());
        BeaconsService.getInstance().init(this, bus, restClient.getBeaconApi());
        IceBreakerService.getInstance().init(this);
        MasterPointService.getInstance().init(this, restClient.getBadgeApi(), bus);
        SurveyService.getInstance().init(this);
        GameService.getInstance().init(this, restClient.getGameApi(), bus, restClient);
        ScavengerService.getInstance().init(this);
        BEPService.getInstance().init(this, restClient.getBepApi());
        InterestService.getInstance().init(this, bus, restClient.getProfileApi());
        PaperService.getInstance().init(this, bus, restClient.getAgendaApi());
        FavoriteService.getInstance().init(this, bus, gson, restClient.getFavoriteApi());
        EVAPromotionService.getInstance().init(this, bus, gson, restClient.getSelfieAPI(), restClient.getProfileApi());
        MemoriesService.getInstance().init(this, bus, gson, restClient.getMemoriesApi());
        CoolfieCacheService.getInstance().init(this);
        TrackingService.getInstance().init(this, restClient.getTrackingApi());
        ForumService.getInstance().init(this, bus, restClient.getTopicApis(),
                restClient.getTopicCommentsApis(), restClient.getCommentsApis(),
                restClient.getTopicLikesApis(), restClient.getCommentLikesApis(),
                restClient.getTopicReportsApis(), restClient.getCommentReportsApis(),
                restClient.getUserTopicsApis());
        AppComponent component = DaggerAppComponent
                .builder()
                .appModule(new AppModule(this))
                .build();

        component.inject(this);

        mortarScope = MortarScope.buildRootScope()
                .withService(DaggerService.SERVICE_NAME, component)
                .build("Root");
    }

    public void startBeaconService(ServiceConnection serviceConnection){
        try{
            Intent intent = new Intent(this, BeaconServices.class);
            bindService(intent, serviceConnection, this.BIND_AUTO_CREATE);
        }
        catch(Exception e){}
    }

    public void stopBeaconService(BeaconServices beaconServices, ServiceConnection serviceConnection){
        try{
            beaconServices.setCallbacks(null); // unregister
            unbindService(serviceConnection);
        }
        catch(Exception e){}
    }

    public void startBluetoothReceiver(){
        try{
            IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mReceiver, filter);
        }
        catch(Exception e){}
    }

    public void stopBluetoothReceiver(){
        try{
            unregisterReceiver(mReceiver);
        }
        catch(Exception e){}
    }

    public void startNetworkMonitoringReceiver(){
        try{
            IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
            registerReceiver(networkBroadcastReceiver, filter);
        }
        catch(Exception e){
        }
    }

    public void stopNetworkMonitoringReceiver(){
        try{
            unregisterReceiver(networkBroadcastReceiver);
        }
        catch(Exception e){}
    }

    public void startSessionService(ServiceConnection serviceConnection){
        try{
            Intent intent = new Intent(this, SessionServices.class);
            bindService(intent, serviceConnection, this.BIND_AUTO_CREATE);
        }
        catch(Exception e){}
    }

    public void stopSessionService(SessionServices sessionServices, ServiceConnection serviceConnection){
        try{
            unbindService(serviceConnection);
        }
        catch(Exception e){}
    }

    public void setMainActivity(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    public MainActivity getMainActivity(){
        return mainActivity;
    }
    public Bus getBus() {
        return bus;
    }

    public RestClient getRestClient() {
        return restClient;
    }

    public Gson getGson() {
        return gson;
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        bus.post(new UpdateBeaconPresenterEvent(false));
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        break;
                    case BluetoothAdapter.STATE_ON:
                        bus.post(new UpdateBeaconPresenterEvent(true));
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        break;
                }
            }
        }
    };

    public final BroadcastReceiver networkBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            ConnectivityManager cm =  (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

            if (cm.getActiveNetworkInfo() != null) {
                bus.post(new UpdateNetworkEvent(true));

            } else {
                bus.post(new UpdateNetworkEvent(false));
            }
        }
    };

    public void setPrevious(){
        previousPresenter = currentPresenter;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
