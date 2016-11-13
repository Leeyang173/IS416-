package sg.edu.smu.livelabs.mobicom.presenters;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import autodagger.AutoComponent;
import automortar.AutoScreen;
import flow.Flow;
import mortar.MortarScope;
import mortar.ViewPresenter;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import sg.edu.smu.livelabs.mobicom.ActionBarOwner;
import sg.edu.smu.livelabs.mobicom.App;
import sg.edu.smu.livelabs.mobicom.AppDependencies;
import sg.edu.smu.livelabs.mobicom.BeaconServices;
import sg.edu.smu.livelabs.mobicom.DaggerScope;
import sg.edu.smu.livelabs.mobicom.MainActivity;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.UIHelper;
import sg.edu.smu.livelabs.mobicom.adapters.BeaconsListAdapter;
import sg.edu.smu.livelabs.mobicom.busEvents.BadgeNotiEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.BeaconRatingEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.BeaconRefreshEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.CloseSidePanelEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.UnregisterBeaconEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.UpdateBeaconPresenterEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.UpdateNetworkEvent;
import sg.edu.smu.livelabs.mobicom.flow.Layout;
import sg.edu.smu.livelabs.mobicom.models.BeaconOccurrences;
import sg.edu.smu.livelabs.mobicom.models.User;
import sg.edu.smu.livelabs.mobicom.models.data.BeaconEntity;
import sg.edu.smu.livelabs.mobicom.models.data.GameListEntity;
import sg.edu.smu.livelabs.mobicom.net.RestClient;
import sg.edu.smu.livelabs.mobicom.net.response.BeaconRatingResponse;
import sg.edu.smu.livelabs.mobicom.net.response.BeaconResponse;
import sg.edu.smu.livelabs.mobicom.net.response.FormResponse;
import sg.edu.smu.livelabs.mobicom.net.response.SimpleResponse;
import sg.edu.smu.livelabs.mobicom.services.BeaconsService;
import sg.edu.smu.livelabs.mobicom.services.DatabaseService;
import sg.edu.smu.livelabs.mobicom.services.GameService;
import sg.edu.smu.livelabs.mobicom.services.MasterPointService;
import sg.edu.smu.livelabs.mobicom.services.TrackingService;
import sg.edu.smu.livelabs.mobicom.views.BeaconView;

/**
 * Created by smu on 22/2/16.
 */
@AutoScreen(
        component = @AutoComponent(dependencies = MainActivity.class, superinterfaces = AppDependencies.class),
        screenAnnotations = Layout.class
)
@DaggerScope(BeaconPresenter.class)
@Layout(R.layout.beacon_view)
public class BeaconPresenter extends ViewPresenter<BeaconView> implements AdapterView.OnItemClickListener{

        private static String GOOGLE_DRIVE = "http://drive.google.com/viewerng/viewer?embedded=true&url=";
        private final RestClient restClient;
        private ActionBarOwner actionBarOwner;
        private Bus bus;
        public static String NAME = "BeaconPresenter";
        private MainActivity activity;
        private boolean bound = false;
        BeaconServices myService;
        private String url;
        private String lastTime;
        private  ConnectivityManager cm;
        private boolean isConnected;

        private int screenWidth;
        private int screenHeight;
        private User me;
        private boolean isFirstTime = true;
        private ActionBarOwner.Config tmpActionBarOwnerConfig;
        private String gameName;

        private BeaconsListAdapter beaconsListAdapter;

        public BeaconPresenter(RestClient restClient, Bus bus, ActionBarOwner actionBarOwner, MainActivity activity) {
                this.restClient = restClient;
                this.bus = bus;
                this.activity = activity;
                this.actionBarOwner = actionBarOwner;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(!isConnected) //no internet connection
                        return;

                final BeaconEntity b = beaconsListAdapter.getItem(position);
                getBeaconRating(b); //get rating from server
                setSelectedStar(b.getRate() == null ? 0 : b.getRate()); //once open, check whether is there any rating for this or not

                TrackingService.getInstance().sendTracking("410", "games",
                        "demos", Long.toString(b.getId()), "", "");

                if(b.getHasViewed() == null) {
                        checkCount(b);
                }
                else{
                        if(!b.getHasViewed()){
                                checkCount(b);
                        }
                }

                activity.setUpButtonEnabled(false);
                App.getInstance().isDemoSiteOpen = true;
//                actionBarOwner.setConfig(new ActionBarOwner.Config(false, gameName, null));

                String url = b.getUrl();
                if(url ==null || url.isEmpty()){
                        url = GOOGLE_DRIVE + b.getPdfUrl();
                }
                getView().webView.loadUrl(url);
                getView().webView.getSettings().setJavaScriptEnabled(true);
                getView().webView.setWebViewClient(new WebViewClient() {

                        @Override
                        public boolean shouldOverrideUrlLoading(WebView view, String url) {
                                view.loadUrl(url);
                                return true;
                        }

                        @Override
                        public void onPageStarted(WebView view, String url, Bitmap favicon) {
                                super.onPageStarted(view, url, favicon);
                                if (getView() != null) {
                                        getView().wvProgressBar.setVisibility(View.VISIBLE);
                                        getView().wvMessageTV.setVisibility(View.VISIBLE);
                                }
                        }

                        @Override
                        public void onPageFinished(WebView view, String url) {
                                super.onPageFinished(view, url);
                                if (getView() != null) {
                                        getView().wvProgressBar.setVisibility(View.GONE);
                                        getView().wvMessageTV.setVisibility(View.GONE);
                                }
                        }

                        @SuppressWarnings("deprecation")
                        @Override
                        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                                // Handle the error
                                if (getView() != null) {
                                        getView().wvProgressBar.setVisibility(View.GONE);
                                        getView().wvMessageTV.setVisibility(View.GONE);
                                }
                        }

                        @TargetApi(android.os.Build.VERSION_CODES.M)
                        @Override
                        public void onReceivedError(WebView view, WebResourceRequest req, WebResourceError rerr) {
                                // Redirect to deprecated method, so we can use it in all SDK versions
                                onReceivedError(view, rerr.getErrorCode(), rerr.getDescription().toString(), req.getUrl().toString());
                        }
                });


                getView().webView.setVisibility(View.VISIBLE);
                getView().starLL.setVisibility(View.VISIBLE);
                getView().listOverlay.setVisibility(View.VISIBLE);
                getView().closeWVBtn.setVisibility(View.VISIBLE);
                getView().closeWVBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
//                                actionBarOwner.setConfig(tmpActionBarOwnerConfig);
                                closeSitePanel();
                        }
                });

                //slide out
                getView().mmContainer.animate().translationX(-1 * (int) (screenWidth * 0.9)).setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                if(getView()!=null) {
                                        getView().beaconLV.setEnabled(false);
                                }
                        }
                });


                //have to update the rating based on the star clicked
                getView().star1.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                                updateBeaconRating(b, 1);
                                BeaconsService.getInstance().rateBeacon(b, 1);
                        }
                });

                getView().star2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                                updateBeaconRating(b, 2);
                                BeaconsService.getInstance().rateBeacon(b, 2);
                        }
                });

                getView().star3.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                                updateBeaconRating(b, 3);
                                BeaconsService.getInstance().rateBeacon(b, 3);
                        }
                });

                getView().star4.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                                updateBeaconRating(b, 4);
                                BeaconsService.getInstance().rateBeacon(b, 4);
                        }
                });

                getView().star5.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                                updateBeaconRating(b, 5);
                                BeaconsService.getInstance().rateBeacon(b, 5);
                        }
                });


                if(b.getPdfUrl() == null || b.getPdfUrl().isEmpty()){
                        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                        getView().starContainer.setLayoutParams(layoutParams);
                        getView().pdfContainer.setVisibility(View.GONE);
                }
                else{
                        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
                        layoutParams.weight = 0.78f;
                        getView().starContainer.setLayoutParams(layoutParams);
                        getView().pdfContainer.setVisibility(View.VISIBLE);
                }

                getView().pdfIV.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                                if(b.getPdfUrl() != null || !b.getPdfUrl().isEmpty())
                                        if(!b.getPdfUrl().isEmpty())
                                                UIHelper.getInstance().openPDF(b.getPdfUrl(), activity);
                        }
                });


        }

        public void closeSitePanel(){
                activity.setUpButtonEnabled(true);
                App.getInstance().isDemoSiteOpen = false;
                getView().listOverlay.setVisibility(View.GONE);
                getView().closeWVBtn.setVisibility(View.GONE);
                //slide in
                getView().mmContainer.animate().translationX(0).setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                if (getView() != null)
                                        getView().beaconLV.setEnabled(true);
                        }
                });
        }

        @Subscribe
        public void closeSitePanelReceiver(CloseSidePanelEvent event){
                closeSitePanel();
        }

        public interface ServiceCallbacks {
                void loadWebSite(List<BeaconOccurrences> topThreeBeacons);
        }

        @Subscribe
         public void unregisterBeaconReceiver(UnregisterBeaconEvent event){
                if(event.toUnregister) {
                        App.getInstance().stopBluetoothReceiver();
                        App.getInstance().stopBeaconService(myService, serviceConnection);
                        App.getInstance().stopNetworkMonitoringReceiver();
                }
                else{
                        App.getInstance().startBluetoothReceiver();
                        App.getInstance().startBeaconService(serviceConnection);
                        App.getInstance().startNetworkMonitoringReceiver();
                }
        }

        @Subscribe
        public void updateBeaconPresenter(UpdateBeaconPresenterEvent event){
                if(event.isOn){
                        if(getView() != null) {
                                if(this.isConnected) {
                                        getView().mmContainer.setVisibility(View.VISIBLE);
                                        getView().calibratingLL.setVisibility(View.VISIBLE);
                                }
                                else{
                                        noInternetConnectionSetting();
                                }
                        }
                }
                else{
                        if(getView() != null) {
                                getView().calibratingLL.setVisibility(View.GONE);
                                UIHelper.getInstance().showAlert(getView().getContext(), ""+getView().getContext().getResources().getText(R.string.turn_on_bluetooth));
                        }
                }

        }

        @Subscribe
        public void updateNetwork(UpdateNetworkEvent event){
                this.isConnected = event.isConnected;
                if(event.isConnected){
                        if(isFirstTime) {
                                loadBeaconsFromServer();
                                isFirstTime = false;
                        }

                        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                        if (mBluetoothAdapter == null) {
                                // Device does not support Bluetooth
                                UIHelper.getInstance().showAlert(getView().getContext(), ""+getView().getContext().getResources().getText(R.string.no_bluetooth));
                        } else {
                                if (!mBluetoothAdapter.isEnabled()) {
                                        // Bluetooth is not enable :)
                                        UIHelper.getInstance().showAlert(getView().getContext(), ""+getView().getContext().getResources().getText(R.string.turn_on_bluetooth));
                                }
                                else{
                                        getView().calibratingLL.setVisibility(View.GONE);
                                        getView().mmContainer.setVisibility(View.VISIBLE);
                                }
                        }
                }
                else{
                        noInternetConnectionSetting();
                }
        }


        @Override
        protected void onLoad(Bundle savedInstanceState) {
                super.onLoad(savedInstanceState);
                Log.d(App.APP_TAG, " BeaconPresenter onload");

                //for android 6 and above, detecting beacon require this setup instead (http://developer.radiusnetworks.com/2015/09/29/is-your-beacon-app-ready-for-android-6.html)
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                        if(activity.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                                UIHelper.getInstance().dismissAlertDialog();
                                UIHelper.getInstance().dismissProgressDialog();
                                final AlertDialog.Builder builder=new AlertDialog.Builder(activity);
                                builder.setTitle(getView().getContext().getResources().getText(R.string.location_access_title));
                                builder.setMessage(getView().getContext().getResources().getText(R.string.grant_location_access));
                                builder.setPositiveButton(android.R.string.ok, null);
                                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                        @Override
                                        public void onDismiss(DialogInterface dialog) {
                                                activity.requestPermissions(new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION}, activity.PERMISSION_REQUEST_COARSE_LOCATION);
                                        }
                                });
                                builder.show();
                        }
                }

                GameListEntity game = GameService.getInstance().getGameByKeyword("demo");
                if(game != null &&  !game.getGameName().isEmpty()){
                        gameName = game.getGameName();
                        actionBarOwner.setConfig(new ActionBarOwner.Config(true, gameName, null));
                }
                else{
                        gameName = "Demo";
                        actionBarOwner.setConfig(new ActionBarOwner.Config(true, gameName, null));
                }

                tmpActionBarOwnerConfig = actionBarOwner.getConfig();

                WindowManager wm = (WindowManager) getView().getContext().getSystemService(Context.WINDOW_SERVICE);
                Display display = wm.getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);
                screenWidth = size.x;
                screenHeight = size.y;

                me = DatabaseService.getInstance().getMe();

                LinearLayout.LayoutParams layoutParams =
                        new LinearLayout.LayoutParams(screenWidth + (int) (screenWidth * 0.9),
                                LinearLayout.LayoutParams.MATCH_PARENT);
                getView().mmContainer.setLayoutParams(layoutParams);
                getView().listContainer.setLayoutParams(new RelativeLayout.LayoutParams(screenWidth,
                        RelativeLayout.LayoutParams.MATCH_PARENT));

                RelativeLayout.LayoutParams starLLlayoutParams = new RelativeLayout.LayoutParams((int) (screenWidth * 0.9) ,
                        RelativeLayout.LayoutParams.WRAP_CONTENT);
                starLLlayoutParams.addRule(RelativeLayout.RIGHT_OF, R.id.list_container);
                getView().starLL.setLayoutParams(starLLlayoutParams);

                RelativeLayout.LayoutParams wvlayoutParams = new RelativeLayout.LayoutParams((int) (screenWidth * 0.9) ,
                        RelativeLayout.LayoutParams.MATCH_PARENT);
                wvlayoutParams.addRule(RelativeLayout.RIGHT_OF, R.id.list_container);
                wvlayoutParams.addRule(RelativeLayout.BELOW, R.id.star_layout);
                getView().webView.setLayoutParams(wvlayoutParams);



                RelativeLayout.LayoutParams relativelayoutParams = new RelativeLayout.LayoutParams(20 + (int) (screenWidth * 0.1) ,
                        20 + (int) (screenWidth * 0.1));
                relativelayoutParams.addRule(RelativeLayout.ALIGN_RIGHT, R.id.list_container);
                relativelayoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
                relativelayoutParams.rightMargin = -20;
                getView().closeWVBtn.setLayoutParams(relativelayoutParams);
                getView().closeWVBtn.bringToFront();

                cm = (ConnectivityManager) getView().getContext().getSystemService(Context.CONNECTIVITY_SERVICE);

                App.getInstance().startBluetoothReceiver();
                App.getInstance().startBeaconService(serviceConnection);
                App.getInstance().startNetworkMonitoringReceiver();

//                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.ENGLISH);
//                BeaconEntity beaconEntity = BeaconsService.getInstance().getBeacon();
                lastTime = "2016-01-01";//beaconEntity != null ? df.format(beaconEntity.getLastUpdated()) : "2016-01-01";

                getView().mmContainer.setVisibility(View.VISIBLE);


                BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                if (mBluetoothAdapter == null) {
                        // Device does not support Bluetooth
                        getView().calibratingLL.setVisibility(View.GONE);
                } else {
                        if (!mBluetoothAdapter.isEnabled()) {
                                // Bluetooth is not enable :)
                                getView().calibratingLL.setVisibility(View.GONE);
                        }
                        else{
                                getView().mmContainer.setVisibility(View.VISIBLE);
                                getView().calibratingLL.setVisibility(View.VISIBLE);
                        }
                }

                getView().beaconLV.setOnItemClickListener(this);

                if(cm.getActiveNetworkInfo() == null){
                        noInternetConnectionSetting();
                        List<BeaconEntity> beaconEntities = BeaconsService.getInstance().getAllBeacons();
                        if(beaconEntities!= null && beaconEntities.size() > 0) {
                                beaconsListAdapter = new BeaconsListAdapter(getView(), bus, beaconEntities);
                                getView().beaconLV.setAdapter(beaconsListAdapter);
                        }
                }
                else{
                        loadBeaconsFromServer();
                        isFirstTime = false;
                }
        }




        @Override
        protected void onEnterScope(MortarScope scope) {
                super.onEnterScope(scope);
                bus.register(this);

                App.getInstance().setPrevious();
                App.getInstance().currentPresenter = NAME;

                App.getInstance().startBeaconService(serviceConnection);

                activity.setVisibleBottombar(View.GONE);

                if(App.getInstance().previousPresenter.equals(MorePresenter.NAME)){
                        App.getInstance().previousPresenter = "";
                }
        }

        @Override
        protected void onExitScope() {
                super.onExitScope();
                bus.unregister(this);
                if (NAME.equals(App.getInstance().currentPresenter)){
                        App.getInstance().currentPresenter = "";
                }

                App.getInstance().stopBluetoothReceiver();
                App.getInstance().stopBeaconService(myService, serviceConnection);
                activity.setVisibleBottombar(View.VISIBLE);
        }

        /** Callbacks for service binding, passed to bindService() */
        private ServiceConnection serviceConnection = new ServiceConnection() {

                @Override
                public void onServiceConnected(ComponentName className, IBinder service) {
                        statusCheck();
                        // cast the IBinder and get MyService instance
                        BeaconServices.LocalBinder binder = (BeaconServices.LocalBinder) service;
                        myService = binder.getService();
                        bound = true;
                        myService.setCallbacks(new ServiceCallbacks() {
                                @Override
                                public void loadWebSite(List<BeaconOccurrences> topThreeBeacons) {
                                        getView().calibratingLL.setVisibility(View.GONE);
                                        int numberOfNearByBeacons = topThreeBeacons.size();
                                        getView().mmContainer.setVisibility(View.VISIBLE);

                                        List<BeaconEntity> tmp = new ArrayList<>();
                                        List<BeaconEntity> beaconEntities = BeaconsService.getInstance().getAllBeacons();
                                        for(BeaconOccurrences beaconOccurrence: topThreeBeacons){ //start from the nearest (higher occurence)
                                                for(BeaconEntity beaconEntity: beaconEntities){
                                                        if(beaconOccurrence.getBeacon().getId2().toString().equals(beaconEntity.getMajor()) &&
                                                                beaconOccurrence.getBeacon().getId3().toString().equals(beaconEntity.getMinor())){
                                                                tmp.add(beaconEntity);
                                                                break;
                                                        }
                                                }
                                        }

                                        for(BeaconEntity b: tmp){
                                                for(BeaconEntity beaconEntity: beaconEntities){
                                                        if(b.getMajor().equals(beaconEntity.getMajor()) && b.getMinor().equals(beaconEntity.getMinor())){
                                                                beaconEntities.remove(beaconEntity);
                                                                beaconEntities.add(0, beaconEntity);
                                                                break;
                                                        }
                                                }
                                        }

                                        if(beaconsListAdapter != null) {
                                                beaconsListAdapter.updateBeaconPosition(beaconEntities, numberOfNearByBeacons);
                                                beaconsListAdapter.notifyDataSetChanged();
                                        }
                                }
                        }); // register
                }

                @Override
                public void onServiceDisconnected(ComponentName arg0) {
                        bound = false;
                }
        };

        private void noInternetConnectionSetting(){
                getView().calibratingLL.setVisibility(View.GONE);
        }

        /**
         * This is a bus event call to update the star. This is called from BeaconService of rateBeacon function
         * @param event
         */
        @Subscribe
        public void UpdateRating(BeaconRatingEvent event){
                setSelectedStar(event.b.getRate());
                updateBeaconListEvent(new BeaconRefreshEvent());
        }

    /**
     * To update the list (only update the avg rating), need to maintain the list position
     * @param event
     */
    @Subscribe
        public void updateBeaconListEvent(BeaconRefreshEvent event){
                final Handler mainHandler = new Handler(getView().getContext().getMainLooper());
                restClient.getBeaconApi().getBeacons("2016-01-01", Long.toString(me.getUID()))
                        .subscribeOn(Schedulers.io())
                        .subscribe(new Action1<BeaconResponse>() {
                                @Override
                                public void call(BeaconResponse beaconResponse) {
                                        if (beaconResponse.status.equals("success")) {
                                                if (beaconResponse.beaconItems != null) {
                                                        final List<BeaconEntity> beaconEntities = BeaconsService.getInstance().updateBeaconList(beaconResponse.beaconItems); //update to DB in case of showing it when no internet

                                                        mainHandler.post(new Runnable() {
                                                                @Override
                                                                public void run() {

//                                                                      //this will initially list out all the beacons while the service continue to scan for nearby Beacons
                                                                        if (beaconEntities.size() > 0 && getView() != null) {
                                                                                if(beaconsListAdapter == null) {
                                                                                        beaconsListAdapter = new BeaconsListAdapter(getView(), bus, beaconEntities);
                                                                                        getView().beaconLV.setAdapter(beaconsListAdapter);
                                                                                }
                                                                                else{
                                                                                        List<BeaconEntity> oldBeacons = beaconsListAdapter.getBeaconEntities();
                                                                                        for(int i=0; i< oldBeacons.size(); i++){
                                                                                                BeaconEntity oldBeacon = oldBeacons.get(i);
                                                                                                for(BeaconEntity newBeacon: beaconEntities){
                                                                                                        if(oldBeacon.getMajor().equals(newBeacon.getMajor()) && oldBeacon.getMajor().equals(newBeacon.getMajor())){
                                                                                                                oldBeacons.get(i).setAvgRating(newBeacon.getAvgRating());
                                                                                                                break;
                                                                                                        }
                                                                                                }
                                                                                        }

                                                                                        beaconsListAdapter.update2(oldBeacons);
                                                                                }
                                                                        }

                                                                }
                                                        });
                                                }
                                        }
                                }
                        }, new Action1<Throwable>() {
                                @Override
                                public void call(Throwable throwable) {
//                                        getView().msgLL.setVisibility(View.VISIBLE);
                                        UIHelper.getInstance().dismissProgressDialog();
                                        Log.e("Mobisys: ", "cannot get beacons", throwable);
                                }
                        });
        }


        /**
         * This helps to record how many star for the rating is/are being selected
         * @param rate
         */
        private void setSelectedStar(int rate){
                if (rate > 5){
                        rate = 5;
                }
                for (int i = 0; i < rate; i++){
                        getView().stars[i].setSelected(true);
                }
                for (int i = rate; i < 5; i++){
                        getView().stars[i].setSelected(false);
                }
        }

        private void getBeaconRating(final BeaconEntity b){
                final Handler mainHandler = new Handler(getView().getContext().getMainLooper());
                restClient.getBeaconApi().getUserBeaconRating(Long.toString(me.getUID()), Long.toString(b.getId()))
                        .subscribeOn(Schedulers.io())
                        .subscribe(new Action1<BeaconRatingResponse>() {
                                @Override
                                public void call(final BeaconRatingResponse ratingResponse) {
                                        if (ratingResponse.status.equals("success")) {

                                                mainHandler.post(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                                if (ratingResponse.beaconRatingItems != null && ratingResponse.beaconRatingItems.size() > 0) {
                                                                        BeaconsService.getInstance().rateBeacon(b, Integer.parseInt(ratingResponse.beaconRatingItems.get(0).rating));
                                                                } else {
                                                                        BeaconsService.getInstance().rateBeacon(b, 0);
                                                                }
                                                        }
                                                });


                                        }
                                }
                        }, new Action1<Throwable>() {
                                @Override
                                public void call(Throwable throwable) {
//                                        getView().msgLL.setVisibility(View.VISIBLE);
                                        Log.e("Mobisys: ", "cannot get beacon rating", throwable);
                                }
                        });
        }

        private void updateBeaconRating(final BeaconEntity b, final int rating){
                restClient.getBeaconApi().updateUserBeaconRating(Long.toString(me.getUID()), Long.toString(b.getId()), Integer.toString(rating))
                        .subscribeOn(Schedulers.io())
                        .subscribe(new Action1<SimpleResponse>() {
                                @Override
                                public void call(SimpleResponse response) {
                                        if (response.status.equals("success")) {
                                        }
                                }
                        }, new Action1<Throwable>() {
                                @Override
                                public void call(Throwable throwable) {
//                                        getView().msgLL.setVisibility(View.VISIBLE);
                                        Log.e("Mobisys: ", "cannot update beacon rating", throwable);
                                }
                        });
        }

        //this function call the server to get the list of beacons/updated beacons
        private void loadBeaconsFromServer(){
                final Handler mainHandler = new Handler(getView().getContext().getMainLooper());

                User me = DatabaseService.getInstance().getMe();
                UIHelper.getInstance().showProgressDialog(getView().getContext(), "Loading demos...", false);
                restClient.getBeaconApi().getBeacons("2016-01-01", Long.toString(me.getUID()))
                        .subscribeOn(Schedulers.io())
                        .subscribe(new Action1<BeaconResponse>() {
                                @Override
                                public void call(BeaconResponse beaconResponse) {
                                        if (beaconResponse.status.equals("success")) {
                                                if (beaconResponse.beaconItems != null) {
                                                        final List<BeaconEntity> beaconEntities = BeaconsService.getInstance().updateBeaconList(beaconResponse.beaconItems); //update to DB in case of showing it when no internet


//                                                        for(BeaconItem beaconItem: beaconResponse.beaconItems){
//                                                                BeaconEntity beaconEntity = new BeaconEntity();
//                                                                beaconEntity.setId(Long.parseLong(beaconItem.id));
//                                                                beaconEntity.setUuid(beaconItem.uuid);
//                                                                beaconEntity.setMajor(beaconItem.major);
//                                                                beaconEntity.setMinor(beaconItem.minor);
//                                                                beaconEntity.setUrl(beaconItem.url);
//                                                                beaconEntity.setPaperName(beaconItem.paperName);
//                                                                beaconEntity.setCapChar(beaconItem.capChar);
//                                                                beaconEntities.add(beaconEntity);
//                                                        }

                                                        Collections.sort(beaconEntities, new Comparator<BeaconEntity>() {
                                                                public int compare(BeaconEntity b1, BeaconEntity b2) {
                                                                        return b1.getPaperName().compareTo(b2.getPaperName());

                                                                }
                                                        });

                                                        mainHandler.post(new Runnable() {
                                                                @Override
                                                                public void run() {

//                                                                      //this will initially list out all the beacons while the service continue to scan for nearby Beacons
                                                                        if (beaconEntities.size() > 0 && getView() != null) {
                                                                                if(beaconsListAdapter == null) {
                                                                                        beaconsListAdapter = new BeaconsListAdapter(getView(), bus, beaconEntities);
                                                                                        getView().beaconLV.setAdapter(beaconsListAdapter);
                                                                                }
                                                                                else{
                                                                                        beaconsListAdapter.update(beaconEntities);
                                                                                }

                                                                                UIHelper.getInstance().dismissProgressDialog();

                                                                                BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                                                                                if (mBluetoothAdapter != null){
                                                                                        if (mBluetoothAdapter.isEnabled()) {
                                                                                                getView().calibratingLL.setVisibility(View.VISIBLE);
                                                                                        }
                                                                                        else{
                                                                                                getView().calibratingLL.setVisibility(View.GONE);
                                                                                        }
                                                                                }else{
                                                                                        getView().calibratingLL.setVisibility(View.GONE);
                                                                                }
                                                                        }

                                                                }
                                                        });
                                                }
                                        }
                                }
                        }, new Action1<Throwable>() {
                                @Override
                                public void call(Throwable throwable) {
//                                        getView().msgLL.setVisibility(View.VISIBLE);
                                        UIHelper.getInstance().dismissProgressDialog();
                                        Log.e("Mobisys: ", "cannot get beacons", throwable);
                                }
                        });

        }

        /**
         * This basically check with the server if this is the first time user open this form before awarding the user
         */
        private void checkCount(final BeaconEntity b){
                final Handler mainHandler = new Handler(getView().getContext().getMainLooper());
                restClient.getBadgeApi().checkFormCount(Long.toString(me.getUID()), "beacon", Long.toString(b.getId()))
                        .subscribeOn(Schedulers.io())
                        .subscribe(new Action1<FormResponse>() {
                                @Override
                                public void call(final FormResponse response) {
                                        UIHelper.getInstance().dismissProgressDialog();
                                        if (response.details == 0) {
                                                BeaconsService.getInstance().updateHasOpened(b, true);
                                        }
                                        else{
                                                BeaconsService.getInstance().updateHasOpened(b, true);
                                        }
                                }
                        }, new Action1<Throwable>() {
                                @Override
                                public void call(Throwable throwable) {
                                        Log.e("Mobisys: ", "cannot call check form count", throwable);
                                }
                        });
        }

        @Subscribe
        public void badgeNotiEvent(BadgeNotiEvent event){
                MasterPointService.getInstance().getBadgesAPI();
                MasterPointService.getInstance().showToolTips(getView().mmContainer, event.badgeName);
        }

        /**
         * This function is intend for Android 6 and above as the location must be on before the Beacon can be detected even when Bluetooth is turn.
         */
        public void statusCheck()
        {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        final LocationManager manager = (LocationManager) getView().getContext().getSystemService(Context.LOCATION_SERVICE);

                        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                                buildAlertMessageNoGps();

                        }
                }
        }

        private void buildAlertMessageNoGps() {
                if(getView() != null) {
                        final AlertDialog.Builder builder = new AlertDialog.Builder(getView().getContext());
                        final AlertDialog.Builder builder1 = builder.setMessage(getView().getContext().getResources().getText(R.string.gps))
                                .setCancelable(false)
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        public void onClick(final DialogInterface dialog, final int id) {
                                                getView().getContext().startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                                        }
                                })
                                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                        public void onClick(final DialogInterface dialog, final int id) {
                                                dialog.cancel();
                                                Flow.get(getView().getContext()).goBack();
                                        }
                                });
                        final AlertDialog alert = builder.create();
                        alert.show();
                }
        }


}
