package sg.edu.smu.livelabs.mobicom;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.squareup.otto.Bus;

import java.util.ArrayList;
import java.util.Locale;

import javax.inject.Inject;

import autodagger.AutoComponent;
import autodagger.AutoInjector;
import butterknife.Bind;
import butterknife.ButterKnife;
import flow.Flow;
import flow.FlowDelegate;
import flow.History;
import flow.path.PathContainerView;
import mortar.MortarScope;
import mortar.bundler.BundleServiceRunner;
import rx.functions.Action0;
import rx.functions.Action1;
import sg.edu.smu.livelabs.mobicom.busEvents.CloseSidePanelEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.MainActivityPauseEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.NetworkEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.SaveAndChangeScreenEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.UnregisterBeaconEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.UnregisterFavoriteEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.UnregisterGameEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.UnregisterIceBreakerEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.UnregisterLeaderboardEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.UnregisterProfileEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.UnregisterQuizEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.UnregisterScavengerEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.UnregisterStumpEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.UnregisterSurveyEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.UnregisterVotingEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.UpdateScreenEvent;
import sg.edu.smu.livelabs.mobicom.fileupload.UploadFileService;
import sg.edu.smu.livelabs.mobicom.flow.GsonParceler;
import sg.edu.smu.livelabs.mobicom.flow.HandlesBack;
import sg.edu.smu.livelabs.mobicom.models.User;
import sg.edu.smu.livelabs.mobicom.models.data.ChatRoomEntity;
import sg.edu.smu.livelabs.mobicom.net.RestClient;
import sg.edu.smu.livelabs.mobicom.presenters.AddGroupChatPresenter;
import sg.edu.smu.livelabs.mobicom.presenters.AgendaPresenter;
import sg.edu.smu.livelabs.mobicom.presenters.BeaconPresenter;
import sg.edu.smu.livelabs.mobicom.presenters.ChatPresenter;
import sg.edu.smu.livelabs.mobicom.presenters.HomePresenter;
import sg.edu.smu.livelabs.mobicom.presenters.IRBPresenter;
import sg.edu.smu.livelabs.mobicom.presenters.MorePresenter;
import sg.edu.smu.livelabs.mobicom.presenters.TACPresenter;
import sg.edu.smu.livelabs.mobicom.presenters.screen.AgendaPaperScreen;
import sg.edu.smu.livelabs.mobicom.presenters.screen.AgendaScreen;
import sg.edu.smu.livelabs.mobicom.presenters.screen.ChatScreen;
import sg.edu.smu.livelabs.mobicom.presenters.screen.GamesScreen;
import sg.edu.smu.livelabs.mobicom.presenters.screen.HomeScreen;
import sg.edu.smu.livelabs.mobicom.presenters.screen.InboxScreen;
import sg.edu.smu.livelabs.mobicom.presenters.screen.MessageScreen;
import sg.edu.smu.livelabs.mobicom.presenters.screen.MoreScreen;
import sg.edu.smu.livelabs.mobicom.presenters.screen.SurveyWebScreen;
import sg.edu.smu.livelabs.mobicom.presenters.screen.VotingScreen;
import sg.edu.smu.livelabs.mobicom.presenters.screen.SplashScreen;
import sg.edu.smu.livelabs.mobicom.qrScanner.QRScannerService;
import sg.edu.smu.livelabs.mobicom.services.AgendaService;
import sg.edu.smu.livelabs.mobicom.services.AttendeesService;
import sg.edu.smu.livelabs.mobicom.services.BEPService;
import sg.edu.smu.livelabs.mobicom.services.ChatService;
import sg.edu.smu.livelabs.mobicom.services.DatabaseService;
import sg.edu.smu.livelabs.mobicom.services.InterestService;
import sg.edu.smu.livelabs.mobicom.services.ScreenService;
import sg.edu.smu.livelabs.mobicom.services.TrackingService;
import sg.edu.smu.livelabs.mobicom.views.UserInfoPopup;

@AutoComponent(
        dependencies = App.class,
        superinterfaces = AppDependencies.class,
        modules = MainActivityModule.class)
@AutoInjector
@DaggerScope(MainActivity.class)
public class MainActivity extends AppCompatActivity implements Flow.Dispatcher, ActionBarOwner.Activity, View.OnClickListener{
    //activity result code
    public static final String SHARE_PREFERENCES = "sg.edu.smu.livelabs.mobicom";
    public static final String LAST_UPDATE_AGENDA = "LastUpdateAgenda";
    public static final String LAST_UPDATE_ATTENDEE = "LastUpdateAgenda";
    public static final String LAST_UPDATE_PAPERS = "LastUpdatePapers";
    public static final String SYNC_OFFLINE_MY_EVENT = "SyncOfflineMyEvent";
    public static final String TIMEZONE = "LastUpdateTimeZone";
    public static final String SYNC_INBOX = "SyncInbox";
    public static final int REQUEST_IMAGE_CAPTURE = 1;
    public static final int REQUEST_IMAGE_GALLERY = 2;
    public static final int REQUEST_QR_CODE = 0x0000c0de;
    public static final int REQUEST_LOGIN = 3;
    public static final int SPEAK_CHECK_CODE = 4;
    //permission code
    public static final int OVERLAY_PERMISSION_REQ_CODE = 5;
    public static final int PERMISSION_REQUEST_COARSE_LOCATION = 10;

    public static final int AGENDA_TAB = 0;
    public static final int MESSAGE_TAB = 1;
    public static final int HOME_TAB = 2;
    public static final int GAMES_TAB = 3;
    public static final int MORE_TAB = 4;
    public static final int OTHER_TAB = 5;
    public int currentTab = -1;

    public static final int SKIP_USER_MODE = 3;
    public static final int OFFLINE_MODE = 1;
    public static final int ONLINE_MODE = 0;
    public static int mode = ONLINE_MODE;
    public boolean openHomePage = true;
    public boolean confirmExist = false;

    MortarScope mortarScope;
    FlowDelegate flowDelegate;
    @Bind(R.id.container)
    public PathContainerView pathContainerView;
    @Bind(R.id.no_internet_connection_txt)
    public TextView noInternet;
    @Inject
    RestClient restClient;
    @Inject
    Bus bus;
    private ScreenService screenService;
    private Toolbar toolbar;
    private ActionBarOwner actionBarOwner;
    private ActionBarController actionBarController;
    private ActionBarOwner.MenuAction actionBarMenuAction;
    private boolean canGoBack;
    public Action1<String> permissionCallback;

    @Bind(R.id.bottom_bar)
    public RelativeLayout bottomBar;

    @Bind(R.id.message_btn)
    public RelativeLayout messageBtn;
    @Bind(R.id.message_tv)
    public TextView messageTV;
    @Bind(R.id.message_bag)
    public TextView messageBag;

    @Bind(R.id.home_btn)
    public RelativeLayout homeBtn;
    @Bind(R.id.home_tv)
    public TextView homeTV;
    @Bind(R.id.home_bag)
    public TextView homeBag;

    @Bind(R.id.games_btn)
    public Button gamesBtn;
    @Bind(R.id.agenda_btn)
    public Button agendaBtn;

    @Bind(R.id.more_btn)
    public Button moreBtn;


    private SplashScreen splashScreen;
    private TextToSpeech speaker;

    @Override
    public Object getSystemService(String name) {
        // see: https://github.com/square/mortar/issues/155
        if (getApplication() == null) {
            return super.getSystemService(name);
        }

        Object service = null;
        if (flowDelegate != null) {
            service = flowDelegate.getSystemService(name);
        }

        if (mortarScope != null && mortarScope.isDestroyed()) {
            MainActivityComponent component = DaggerMainActivityComponent
                    .builder()
                    .mainActivityModule(new MainActivityModule(this, actionBarOwner))
                    .appComponent(DaggerService.<AppComponent>getDaggerComponent(getApplicationContext()))
                    .build();

            String scopeName = getClass().getName() + "|" + getTaskId();

            Log.d(App.APP_TAG, " CREATING NEW SCOPE BECAUSE IT'S NULL: " + scopeName);
            mortarScope = MortarScope.buildChild(getApplication())
                    .withService(BundleServiceRunner.SERVICE_NAME, new BundleServiceRunner())
                    .withService(DaggerService.SERVICE_NAME, component)
                    .build(scopeName);
        }
        if (service == null && mortarScope != null && mortarScope.hasService(name)) {
            service = mortarScope.getService(name);
        }

        return service != null ? service : super.getSystemService(name);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(App.APP_TAG, "MainActivity onCreate");
        openHomePage = true;
        currentTab = -1;
        confirmExist = false;
        actionBarOwner = new ActionBarOwner();
        screenService = new ScreenService();

        String scopeName = getClass().getName() + "|" + getTaskId();
        mortarScope = MortarScope.findChild(getApplication(), scopeName);
        if (mortarScope == null) {
            Log.d(App.APP_TAG, "CREATING SCOPE: " + scopeName);
            MainActivityComponent component = DaggerMainActivityComponent
                    .builder()
                    .mainActivityModule(new MainActivityModule(this, actionBarOwner))
                    .appComponent(DaggerService.<AppComponent>getDaggerComponent(getApplicationContext()))
                    .build();

            mortarScope = MortarScope.buildChild(getApplication())
                    .withService(BundleServiceRunner.SERVICE_NAME, new BundleServiceRunner())
                    .withService(DaggerService.SERVICE_NAME, component)
                    .build(scopeName);
        }

        DaggerService.<MainActivityComponent>getDaggerComponent(this).inject(this);

        BundleServiceRunner.getBundleServiceRunner(this).onCreate(savedInstanceState);
        screenService.restore(savedInstanceState);

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        actionBarController = new ActionBarController(this, toolbar, UIHelper.getInstance());
        actionBarOwner.takeView(this);
        actionBarOwner.setConfig(new ActionBarOwner.Config(false));

        homeBtn.setOnClickListener(this);
        agendaBtn.setOnClickListener(this);
        gamesBtn.setOnClickListener(this);
        messageBtn.setOnClickListener(this);
        moreBtn.setOnClickListener(this);
        //TODO font size
        UIHelper.getInstance().setTypeface(noInternet, homeTV, homeBag, agendaBtn, gamesBtn,
                messageTV, messageBag, moreBtn);
        noInternet.setVisibility(View.GONE);

        GsonParceler parceler = new GsonParceler(new Gson());
        @SuppressWarnings("deprecation")
        FlowDelegate.NonConfigurationInstance nonConfig =
                (FlowDelegate.NonConfigurationInstance) getLastNonConfigurationInstance();

        bus.register(this);
        Intent intent = getIntent();
        Log.d(App.APP_TAG, "INTENT: " + intent);
        String notificationType = null;
        Object notificationData = null;
        int notiId = 0;
        if (intent != null) {
            notificationType= intent.getStringExtra(AppNotifications.NOTI_TYPE);
            notiId = intent.getIntExtra(AppNotifications.NOTI_ID, 0);
            notificationData = intent.getExtras();
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.cancel(notiId);
        }
        splashScreen = new SplashScreen(notificationType, notiId, notificationData);
        flowDelegate = FlowDelegate.onCreate(nonConfig, getIntent(), savedInstanceState, parceler, History.single(splashScreen), this);

        User me = DatabaseService.getInstance().getMe();
        if(me.getUID() != -1){
            startSessionServices();
        }
//        openTextToSpeak();
    }

    public void startSessionServices(){
        startService(new Intent(this, SessionServices.class));
    }

    public void stopSessionsService(){
        stopService(new Intent(this, SessionServices.class));
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(App.APP_TAG, "INTENT: " + intent);
        String notificationType = null;
        Object notificationData = null;
        int notiId = 0;
        if (intent != null) {
            notificationType= intent.getStringExtra(AppNotifications.NOTI_TYPE);
            notiId = intent.getIntExtra(AppNotifications.NOTI_ID, 0);
            notificationData = intent.getExtras();
            processNotification(getContext(), notificationType, notificationData);
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.cancel(notiId);
        }
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public void dispatch(Flow.Traversal traversal, final Flow.TraversalCallback callback) {
        if(pathContainerView == null) return;
        pathContainerView.dispatch(traversal, new Flow.TraversalCallback() {
            @Override
            public void onTraversalCompleted() {
                Log.d(App.APP_TAG, "traveling");
                invalidateOptionsMenu();
                callback.onTraversalCompleted();
            }
        });
    }

    @Override
    public void setShowHomeEnabled(boolean enabled, String homeImageId) {
        actionBarController.changeIcon(enabled, homeImageId);
    }

    @Override
    public void setUpButtonEnabled(boolean enabled) {
        actionBarController.changeBackBtn(enabled);
        canGoBack = enabled;
    }

    @Override
    public void setTitle(CharSequence title, ActionBarOwner.MenuAction titleAction, ActionBarOwner.MenuAction leftAction,
                         ActionBarOwner.MenuAction middleAction, ActionBarOwner.MenuAction rightAction, int focus) {
        if (title != null){
            actionBarController.changeTitle(title);
            actionBarController.setTitleAction(titleAction);
        } else if (leftAction != null){
            actionBarController.enableCenterControl(leftAction, middleAction, rightAction, focus);
        } else {
            actionBarController.changeTitle("");
        }

    }

    public void unselectAllToolbarMenu(){
        actionBarController.unselectAll();
    }

    public void resetAllToolbarMenu(){
        actionBarController.reset();
    }

    @Override
    public void setMenu(ActionBarOwner.MenuAction action) {
        actionBarMenuAction = action;
        actionBarController.setupMenu(action);
    }

    @Override
    public void onBackPressed() {
        if(App.getInstance().previousPresenter.equals(MorePresenter.NAME)){
            App.getInstance().previousPresenter = "";
            setCurrentTab(MORE_TAB, -1);
        }
        else if(!canGoBack && App.getInstance().currentPresenter.equals(BeaconPresenter.NAME) && App.getInstance().isDemoSiteOpen){
            bus.post(new CloseSidePanelEvent());
        }
        else {
            if (!canGoBack || !((HandlesBack) pathContainerView).onBackPressed()) {
                if(App.getInstance().currentPresenter.equals(HomePresenter.NAME)
                        || App.getInstance().currentPresenter.equals(IRBPresenter.NAME)
                        || App.getInstance().currentPresenter.equals(TACPresenter.NAME)){
                    if (!confirmExist){
                        UIHelper.getInstance().showNoneAnimaConfirmAlert(getContext(),
                                getContext().getString(R.string.app_name),
                                "Do you want to exit the app?", "Yes", "No",
                                new Action0() {
                                    @Override
                                    public void call() {
                                        confirmExist = true;
                                        onBackPressed();

                                    }
                                }, new Action0() {
                                    @Override
                                    public void call() {
                                    }
                                });
                        return;
                    }
                }
                super.onBackPressed();
            }
        }
    }

        @Override
    protected void onStop() {
        super.onStop();
        System.out.println("On Stop");
//        App.getInstance().stopBluetoothReceiver();
        if(App.getInstance() != null && App.getInstance().currentPresenter != null){
            if(App.getInstance().currentPresenter.equals("BeaconPresenter")){
                bus.post(new UnregisterBeaconEvent(true));
            }
            else if(App.getInstance().currentPresenter.equals("ProfilePresenter")){
                bus.post(new UnregisterProfileEvent(true));
            }
            else if(App.getInstance().currentPresenter.equals("IceBreakerPresenter")){
                bus.post(new UnregisterIceBreakerEvent(true));
            }
            else if(App.getInstance().currentPresenter.equals("IceBreakerLeaderBoardPresenter")){
                bus.post(new UnregisterIceBreakerEvent(true));
            }
            else if(App.getInstance().currentPresenter.equals("VotingPresenter")){
                bus.post(new UnregisterVotingEvent(true));
            }
            else if(App.getInstance().currentPresenter.equals("QuizPresenter")){
                bus.post(new UnregisterQuizEvent(true));
            }
            else if(App.getInstance().currentPresenter.equals("SurveyWebPresenter") ||
                    App.getInstance().currentPresenter.equals("SurveyPresenter")){
                bus.post(new UnregisterSurveyEvent(true));
            }
            else if(App.getInstance().currentPresenter.equals("ScavengerHuntDetailPresenter")){
                bus.post(new UnregisterScavengerEvent(true));
            }
            else if(App.getInstance().currentPresenter.equals("FavoritePresenter") || App.getInstance().currentPresenter.equals("FavoriteItemPresenter")){
                bus.post(new UnregisterFavoriteEvent(true));
            }
            else if(App.getInstance().currentPresenter.equals("GamesPresenter")){
                bus.post(new UnregisterGameEvent(true));
            }
            else if(App.getInstance().currentPresenter.equals("StumpPresenter")){
                bus.post(new UnregisterStumpEvent(true));
            }
            else if(App.getInstance().currentPresenter.equals("LeaderboardPresenter")){
                bus.post(new UnregisterLeaderboardEvent(true));
            }
        }

    }

    @Override
    protected void onDestroy() {
        try {
            if (mortarScope != null) {
                mortarScope.destroy();
            }
            mortarScope = null;
        } catch (Throwable t) {
            Log.e("XXX:", "Error", t);
        }
        System.out.println("On destory");
        if (bus != null) {
            bus.unregister(this);
        }
        stopSessionsService();
        UploadFileService.getInstance().dismissFileUploadProgressDialog();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();

        checkNetwork();

        final App app = (App) getApplication();
        app.setMainActivity(this);
        if (flowDelegate != null) {
            flowDelegate.onResume();
        }
        System.out.println("On resume");

        User me = DatabaseService.getInstance().getMe();
        if(me.getUID() != -1) {
            App.getInstance().callingLoginFromOnResume = true;
            ChatService.getInstance().login2(me.getQrCode(), false, true);
        }

        if(App.getInstance() != null && App.getInstance().currentPresenter != null){
            if(App.getInstance().currentPresenter.equals("BeaconPresenter")){
                bus.post(new UnregisterBeaconEvent(false));
            }
            else if(App.getInstance().currentPresenter.equals("ProfilePresenter")){
                bus.post(new UnregisterProfileEvent(false));
            }
            else if(App.getInstance().currentPresenter.equals("IceBreakerPresenter")){
                bus.post(new UnregisterIceBreakerEvent(false));
            }
            else if(App.getInstance().currentPresenter.equals("IceBreakerLeaderBoardPresenter")){
                bus.post(new UnregisterIceBreakerEvent(false));
            }
            else if(App.getInstance().currentPresenter.equals("VotingPresenter")){
                bus.post(new UnregisterVotingEvent(false));
            }
            else if(App.getInstance().currentPresenter.equals("QuizPresenter")){
                bus.post(new UnregisterQuizEvent(false));
            }
            else if(App.getInstance().currentPresenter.equals("SurveyWebPresenter") ||
                    App.getInstance().currentPresenter.equals("SurveyPresenter")){
                bus.post(new UnregisterSurveyEvent(false));
            }
            else if(App.getInstance().currentPresenter.equals("ScavengerHuntDetailPresenter")){
                bus.post(new UnregisterScavengerEvent(false));
            }
            else if(App.getInstance().currentPresenter.equals("FavoritePresenter") || App.getInstance().currentPresenter.equals("FavoriteItemPresenter")){
                bus.post(new UnregisterFavoriteEvent(false));
            }
            else if(App.getInstance().currentPresenter.equals("GamesPresenter")){
                bus.post(new UnregisterGameEvent(false));
            }
            else if(App.getInstance().currentPresenter.equals("StumpPresenter")){
                bus.post(new UnregisterStumpEvent(false));
            }
            else if(App.getInstance().currentPresenter.equals("LeaderboardPresenter")){
                bus.post(new UnregisterLeaderboardEvent(false));
            }
        }

    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (flowDelegate == null) {
            return;
        }
    }

    @Override
    protected void onPause() {
        App app = (App) getApplication();
        app.setMainActivity(null);
        if (flowDelegate != null) {
            flowDelegate.onPause();
        }
        if (bus != null){
            bus.post(new MainActivityPauseEvent());
        }
        UIHelper.getInstance().dismissProgressDialog();
        if(speaker !=null){
            speaker.stop();
            speaker.shutdown();
        }
        super.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        try {
            if (flowDelegate != null && screenService != null) {
                BundleServiceRunner.getBundleServiceRunner(this).onSaveInstanceState(outState);
                flowDelegate.onSaveInstanceState(outState);
                screenService.save(outState);
            }
        } catch (Exception e) {
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            return ((HandlesBack) pathContainerView).onBackPressed();
        }
        else if (actionBarMenuAction != null) {
            actionBarMenuAction.action.call();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public Toolbar getToolbar() {
        return toolbar;
    }

    public ScreenService getScreenService() {
        return screenService;
    }

    public void processNotification(final Context context, String notificationType, Object notificationData) {
        String currentPresenter = App.getInstance().currentPresenter;
        Log.d(App.APP_TAG, "CurrentPresenter is " + currentPresenter);
        if ("Message".equals(notificationType) || "Group Invited".equals(notificationType)) {
            Bundle data = (Bundle) notificationData;
            long groupId = data.getLong("GROUP_ID");
            long fromUser = data.getLong("USER_ID");
            final ChatRoomEntity chatRoomEntity = groupId == 0 ?
                    ChatService.getInstance().findSingleChatRoom(fromUser, true) :
                    ChatService.getInstance().findGroupChatRoom(groupId, true);
            if (chatRoomEntity == null || chatRoomEntity.getUserIds() == null
                    || chatRoomEntity.getUserIds().isEmpty()){
                return;
            }
            if (ChatPresenter.NAME.equals(currentPresenter)){
                bus.post(new UpdateScreenEvent(chatRoomEntity));
            } else {
                saveAndChangeScreen(context, currentPresenter, new ChatScreen(chatRoomEntity));
            }

        } else if ("BEP noti".equals(notificationType)){

            Bundle data = (Bundle) notificationData;
            String content = data.getString("CONTENT");
            String title = data.getString("TITLE");
            Bundle notiData = data.getBundle("DATA");
            long notiType = notiData.getLong("notification_type", 0);
            long notiId = notiData.getLong("id", 0);
            BEPService.getInstance().trackClickNotification(Long.toString(notiId), notiType);
            UIHelper.getInstance().showAlert(context, title, content, true);
        } else if ("BEP survey noti".equals(notificationType)){

            Bundle data = (Bundle) notificationData;
            String content = data.getString("CONTENT");
            String title = data.getString("TITLE");
            Bundle surveyData = data.getBundle("DATA");
            long surveyId = surveyData.getLong("Survey_id", 0);
            long notiType = surveyData.getLong("notification_type", 0);
            BEPService.getInstance().trackClickNotification(Long.toString(surveyId), notiType);
            saveAndChangeScreen(context, currentPresenter, new SurveyWebScreen(surveyId));
        }else if ("System noti".equals(notificationType)){
            Bundle data = (Bundle) notificationData;
            String content = data.getString("CONTENT");
            UIHelper.getInstance().showAlert(context, content);
        }else if ("Polling noti".equals(notificationType)){
            Bundle data = (Bundle) notificationData;
            String title = data.getString("TITLE");
            Bundle pollData = data.getBundle("DATA");
            long pollId = pollData.getLong("poll_id", 0l);
            saveAndChangeScreen(context, currentPresenter, new VotingScreen(pollId, title));
        } else if ("Inbox noti".equals(notificationType)){
            saveAndChangeScreen(context, currentPresenter, new InboxScreen());
        }

    }

    private void saveAndChangeScreen(Context context, String currentPresenter, Object newScreen){
        openHomePage = false;
        if (AddGroupChatPresenter.NAME.equals(currentPresenter)){
            bus.post(new SaveAndChangeScreenEvent(newScreen));
        } else {
            Flow.get(context).set(newScreen);
        }
        UIHelper.getInstance().forceClosePopup();
        UserInfoPopup.dismiss();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK){
            if (requestCode == MainActivity.REQUEST_IMAGE_CAPTURE ) {
                UploadFileService.getInstance().upload(this);
                return;
            } else if (requestCode == MainActivity.REQUEST_IMAGE_GALLERY ) {
                UploadFileService.getInstance().uploadImageGallery(this, data);
                return;
            } else if (requestCode == MainActivity.REQUEST_QR_CODE ){
                IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
                QRScannerService.getInstance().returnFromScanner(this, data);
                return;
            } else if (requestCode == REQUEST_LOGIN){
                //TODO

            }
        }
        if (requestCode == SPEAK_CHECK_CODE) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                speaker =new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int status) {
                        if(status == TextToSpeech.SUCCESS) {
                            speaker.setLanguage(Locale.UK);
                        } else if(status == TextToSpeech.ERROR){
                            Toast.makeText(MainActivity.this, "Sorry! Text To Speech failed...", Toast.LENGTH_LONG).show();
                        }
                    }
                });
                return;
            }
            else {
                openTextToSpeak();
                Log.d(App.APP_TAG, "Fail to open text to speak");
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if(permissionCallback != null) {
                permissionCallback.call("");
            }
        } else {
            UIHelper.getInstance().showAlert(this, getResources().getString(R.string.permission_denied));
            Log.e(App.APP_TAG, "permission Denied");
        }

//        switch (requestCode) {
//            case PERMISSION_REQUEST_COARSE_LOCATION: {
//                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    Log.d(App.APP_TAG, "coarse location permission granted");
//                } else {
//                    UIHelper.getInstance().showAlert(this,"Functionality limited", "Since location access has not been granted, this app will not be able to discover beacons when in the background.", false);
//                }
//                return;
//            }
//        }

    }

    public void checkAndRequirePermissions(String[] permissions, Action1<String> callback){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ArrayList<String> requiredPermissions = new ArrayList<>();
            for (String permission : permissions){
                if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED){
                    requiredPermissions.add(permission);
                }
            }
            if (requiredPermissions.size() > 0){
                permissionCallback = callback;
                requestPermissions(requiredPermissions.toArray(new String[requiredPermissions.size()]), 1);
            } else {
                callback.call("");
            }
        } else{
            callback.call("");
        }
    }


    @Override
    public void onClick(View v) {
        openHomePage = true;
        int id = v.getId();
        switch (id){
            case R.id.message_btn:
                setCurrentTab(MESSAGE_TAB, -1);
                TrackingService.getInstance().sendTracking("201", "messages", "", "", "", "");
                break;
            case R.id.games_btn:
                setCurrentTab(GAMES_TAB, -1);
                TrackingService.getInstance().sendTracking("401", "games", "", "", "", "");
                break;
            case R.id.agenda_btn:
                setCurrentTab(AGENDA_TAB, AgendaPresenter.FULL_AGENDA);
                TrackingService.getInstance().sendTracking("101", "agenda", "", "", "", "");
                break;
            case R.id.home_btn:
                setCurrentTab(HOME_TAB, -1);
                TrackingService.getInstance().sendTracking("301", "mobisys", "", "", "", "");
                break;
            case R.id.more_btn:
                setCurrentTab(MORE_TAB, -1);
                TrackingService.getInstance().sendTracking("501", "more", "", "", "", "");
                break;
        }
    }

    public void setCurrentTab(int tab, int focusTab){
        if (tab == currentTab) return;
        switch (tab){
            case AGENDA_TAB:
                if (focusTab == AgendaPresenter.PAPER){
                    Flow.get(getContext()).set(new AgendaPaperScreen());
                } else {
                    Flow.get(getContext()).set(new AgendaScreen(focusTab));
                }
                break;
            case MESSAGE_TAB:
                Log.d(App.APP_TAG, "message");
                Flow.get(getContext()).set(new MessageScreen(focusTab));
                break;
            case HOME_TAB:
                resetHistory(new HomeScreen());
                break;
            case GAMES_TAB:
                Flow.get(getContext()).set(new GamesScreen());
                break;
            case MORE_TAB:
                Flow.get(getContext()).set(new MoreScreen());
                break;
        }
    }


    public void resetHistory(Object screen){
        Flow.get(getContext()).setHistory(
                History
                        .emptyBuilder()
                        .push(screen)
                        .build(), Flow.Direction.FORWARD);
        screenService.clearAll();
    }

    public void setVisibleBottombar(int visible){
        bottomBar.setVisibility(visible);
        if (visible == View.VISIBLE){
            new AsyncTask<Void, Void, Long>(){

                @Override
                protected Long doInBackground(Void... params) {
                    return ChatService.getInstance().getNumberUnreadChatRoom();
                }

                @Override
                protected void onPostExecute(Long aLong) {
                    super.onPostExecute(aLong);
                    setMessageBag(aLong);
                    setInboxBag();
                }
            }.execute();
        }
    }

    public void setVisibleToolbar(int visible){
        toolbar.setVisibility(visible);
    }

    public void goToMainPage(){
        if (openHomePage){
            setCurrentTab(HOME_TAB, -1);
            setVisibleBottombar(View.VISIBLE);
            setVisibleToolbar(View.VISIBLE);
        }
    }

    public void registerGCM(){
        Intent intent1 = new Intent(MainActivity.this, RegistrationIntentService.class);
        startService(intent1);
        //     SystemClock.sleep(100);
        //     AppNotifications.sendSimpleNotification(getContext(), "abc", AppNotifications.SIMPLE_NOTIFICATION_ID);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        try{
            if (App.getInstance().currentPresenter == ChatPresenter.NAME)
                return super.dispatchTouchEvent(event);
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                View v = getCurrentFocus();
                if ( v instanceof EditText) {
                    Rect outRect = new Rect();
                    v.getGlobalVisibleRect(outRect);
                    if (!outRect.contains((int)event.getRawX(), (int)event.getRawY())) {
                        v.clearFocus();
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    }
                }
            }
            return super.dispatchTouchEvent(event);
        } catch (Exception e){
            Log.e(App.APP_TAG, "dispatchTouchEvent", e);
        }
        return true;
    }
    private boolean networkFail = false;

    public void checkNetwork(){
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (!mWifi.isConnected() && !connManager.isActiveNetworkMetered()) {
            if (!networkFail){
                noInternet.setVisibility(View.VISIBLE);
                networkFail = true;
                if (MainActivity.mode != SKIP_USER_MODE){
                    MainActivity.mode = MainActivity.OFFLINE_MODE;
                }
            }
        } else {
            if (networkFail){
                UIHelper.getInstance().dismissAlertDialog();
                noInternet.setVisibility(View.GONE);
                networkFail = false;
                bus.post(new NetworkEvent());
                if (MainActivity.mode != SKIP_USER_MODE){
                    MainActivity.mode = MainActivity.ONLINE_MODE;
                    syncOfflineData();
                }
            }
        }
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                checkNetwork();
            }
        }, 1000);
    }

    private void openTextToSpeak(){
//        Intent checkTTSIntent = new Intent();
//        checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
//        startActivityForResult(checkTTSIntent, SPEAK_CHECK_CODE);
    }

    public TextToSpeech getSpeaker(){
        return speaker;
    }

    public void syncData(final boolean isFirst){
        if (MainActivity.mode == MainActivity.ONLINE_MODE){
            new AsyncTask<Void, Void, Long>(){

                @Override
                protected Long doInBackground(Void... params) {
                    AgendaService.getInstance().syncEvent(isFirst);
                    AgendaService.getInstance().syncPaper(isFirst);
                    AttendeesService.getInstance().syncAttendees(isFirst);
//                    BEPService.getInstance().updateUserMac(getContext(), "");
                    if (isFirst){
                        InterestService.getInstance().loadInterestAPI();
                        return new Long(0);
                    } else{
                        return ChatService.getInstance().getNumberUnreadChatRoom();
                    }
                }

                @Override
                protected void onPostExecute(Long unreadMessage) {
                    super.onPostExecute(unreadMessage);
                    setMessageBag(unreadMessage);
                    setInboxBag();
                }
            }.execute();

        }
    }

    private void syncOfflineData(){
        new AsyncTask<Void, Void, Void>(){

            @Override
            protected Void doInBackground(Void... params) {
                SharedPreferences sharedPreferences = getApplicationContext()
                        .getSharedPreferences(MainActivity.SHARE_PREFERENCES, Context.MODE_PRIVATE);
                Boolean isSync = sharedPreferences.getBoolean(SYNC_OFFLINE_MY_EVENT, false);
                if (isSync){
                    AgendaService.getInstance().syncMyEventToServer();
                }
                return null;
            }
        }.execute();
    }

    public void setMessageBag(Long number){
        if (number == null || number < 1){
            messageBag.setVisibility(View.GONE);
        } else {
            messageBag.setVisibility(View.VISIBLE);
            messageBag.setText(number+"");
        }
    }

    public void setInboxBag(){
        long unreadMsg = AgendaService.getInstance().getUnreadInbox();
        if (unreadMsg < 1){
            homeBag.setVisibility(View.GONE);
        } else {
            homeBag.setVisibility(View.VISIBLE);
            homeBag.setText(unreadMsg+"");
        }
    }
}
