package sg.edu.smu.livelabs.mobicom.presenters;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import autodagger.AutoComponent;
import automortar.AutoScreen;
import automortar.ScreenParam;
import mortar.MortarScope;
import mortar.ViewPresenter;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import sg.edu.smu.livelabs.mobicom.ActionBarOwner;
import sg.edu.smu.livelabs.mobicom.App;
import sg.edu.smu.livelabs.mobicom.AppDependencies;
import sg.edu.smu.livelabs.mobicom.DaggerScope;
import sg.edu.smu.livelabs.mobicom.MainActivity;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.UIHelper;
import sg.edu.smu.livelabs.mobicom.busEvents.BadgeNotiEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.UnregisterSurveyEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.UpdateNetworkEvent;
import sg.edu.smu.livelabs.mobicom.flow.Layout;
import sg.edu.smu.livelabs.mobicom.models.User;
import sg.edu.smu.livelabs.mobicom.models.data.SurveyEntity;
import sg.edu.smu.livelabs.mobicom.net.RestClient;
import sg.edu.smu.livelabs.mobicom.net.response.FormResponse;
import sg.edu.smu.livelabs.mobicom.net.response.SurveyResponse;
import sg.edu.smu.livelabs.mobicom.services.DatabaseService;
import sg.edu.smu.livelabs.mobicom.services.GameService;
import sg.edu.smu.livelabs.mobicom.services.MasterPointService;
import sg.edu.smu.livelabs.mobicom.services.SurveyService;
import sg.edu.smu.livelabs.mobicom.views.SurveyWebView;

/**
 * Created by smu on 22/2/16.
 */
@AutoScreen(
        component = @AutoComponent(dependencies = MainActivity.class, superinterfaces = AppDependencies.class),
        screenAnnotations = Layout.class
)
@DaggerScope(SurveyWebPresenter.class)
@Layout(R.layout.survey_web_view)
public class SurveyWebPresenter extends ViewPresenter<SurveyWebView>{

        private ActionBarOwner actionBarOwner;
        private final RestClient restClient;
        private Bus bus;
        public static String NAME = "SurveyWebPresenter";
        private MainActivity mainActivity;
        private boolean isFirstTime = true;
//        private static String GOOGLE_FORM = "https://docs.google.com/forms/d/";

//        private String surveyURL;
        private SurveyEntity s;
        private long formId;
        private User me;
        private  ConnectivityManager cm;

        public SurveyWebPresenter(RestClient restClient, Bus bus, ActionBarOwner actionBarOwner, MainActivity mainActivity,
                                  @ScreenParam long formId) {
                this.restClient = restClient;
                this.bus = bus;
                this.s = SurveyService.getInstance().getSurvey(formId); //this is meant for loading survey coming from SurveyPresenter (not noti)
                this.formId = formId;
                App.getInstance().currentPresenter = "SurveyWebPresenter";
                this.actionBarOwner = actionBarOwner;
                this.mainActivity = mainActivity;
        }

        @Override
        protected void onLoad(Bundle savedInstanceState) {
                super.onLoad(savedInstanceState);
                Log.d(App.APP_TAG, " SurveyWebPresenter onload");

                actionBarOwner.setConfig(new ActionBarOwner.Config(true, "Survey", null));

                me = DatabaseService.getInstance().getMe();

                cm = (ConnectivityManager) getView().getContext().getSystemService(Context.CONNECTIVITY_SERVICE);

                if(cm.getActiveNetworkInfo() != null) {
                        isFirstTime = false;
                        getSurveyDetails();
                        getView().messageTV.setVisibility(View.GONE);
                        getView().progressBar.setVisibility(View.GONE);
                }
                else{
                        getView().surveyWV.setVisibility(View.GONE);
                        getView().progressBar.setVisibility(View.GONE);
                        getView().messageTV.setVisibility(View.VISIBLE);
                        getView().messageTV.setText("Survey cannot be loaded without Internet connection");
                }

                App.getInstance().startNetworkMonitoringReceiver();
        }

        private void loadPage(String surveyURL){
                getView().surveyWV.setVisibility(View.VISIBLE);
                getView().progressBar.setVisibility(View.GONE);
                getView().messageTV.setVisibility(View.GONE);
                getView().surveyWV.getSettings().setJavaScriptEnabled(true);
                getView().surveyWV.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
                getView().surveyWV.setWebViewClient(new WebViewClient());
                getView().surveyWV.setWebChromeClient(new WebChromeClient());

//                String url = GOOGLE_FORM + surveyURL;
                getView().surveyWV.loadUrl(surveyURL);
                getView().surveyWV.setWebViewClient(new WebViewClient() {

                        @Override
                        public boolean shouldOverrideUrlLoading(WebView view, String url) {
                                view.loadUrl(url);
                                return true;
                        }

                        @Override
                        public void onPageStarted(WebView view, String url, Bitmap favicon) {
                                super.onPageStarted(view, url, favicon);
                                if (getView() != null) {
                                        getView().progressBar.setVisibility(View.VISIBLE);
                                        getView().messageTV.setVisibility(View.VISIBLE);
                                        getView().messageTV.setText("Loading");
                                }

                        }

                        @Override
                        public void onPageFinished(WebView view, String url) {
                                super.onPageFinished(view, url);
                                if (getView() != null) {
                                        getView().progressBar.setVisibility(View.GONE);
                                        getView().messageTV.setVisibility(View.GONE);
                                }
                        }
                });
        }

        @Override
        protected void onEnterScope(MortarScope scope) {
                super.onEnterScope(scope);
                bus.register(this);
                App.getInstance().setPrevious();
                App.getInstance().currentPresenter = NAME;
                mainActivity.setVisibleBottombar(View.GONE);
        }

        @Override
        protected void onExitScope() {
                super.onExitScope();
                bus.unregister(this);

                if(!GameService.getInstance().fromGames(App.getInstance().previousPresenter)){
                        mainActivity.setVisibleBottombar(View.VISIBLE);
                }

                if (NAME.equals(App.getInstance().currentPresenter)){
                        App.getInstance().currentPresenter = "";
                }
        }

        @Subscribe
        public void unregisterSurveyReceiver(UnregisterSurveyEvent event){
                if(event.toUnregister) {
                        App.getInstance().stopNetworkMonitoringReceiver();
                }
                else{
                        App.getInstance().startNetworkMonitoringReceiver();
                }
        }

        /**
         * This basically check with the server if this is the first time user open this form before awarding the user
         */
        private void checkCount(String formType){
                if(getView() != null) {
                        final Handler mainHandler = new Handler(getView().getContext().getMainLooper());
                        restClient.getBadgeApi().checkFormCount(Long.toString(me.getUID()), formType, Long.toString(formId))
                                .subscribeOn(Schedulers.io())
                                .subscribe(new Action1<FormResponse>() {
                                        @Override
                                        public void call(final FormResponse response) {
                                                UIHelper.getInstance().dismissProgressDialog();
                                                if (response.details == 0)
                                                        mainHandler.post(new Runnable() {
                                                                @Override
                                                                public void run() {

//                                                                MasterPointService.getInstance().addPoint(MasterPointService.getInstance().SURVEY, getView().surveyWV);
                                                                }
                                                        });
                                        }
                                }, new Action1<Throwable>() {
                                        @Override
                                        public void call(Throwable throwable) {
                                                Log.e("Mobisys: ", "cannot check form count", throwable);
                                        }
                                });
                }

        }

        @Subscribe
        public void updateNetwork(UpdateNetworkEvent event) {
                if (event.isConnected && isFirstTime) {
                        isFirstTime = false;
                        getSurveyDetails();
                }
        }

        private void getSurveyDetails(){
                final Handler mainHandler = new Handler(getView().getContext().getMainLooper());
                restClient.getSurveyApi().getSurveyDetails(Long.toString(me.getUID()), Long.toString(formId))
                        .subscribeOn(Schedulers.io())
                        .subscribe(new Action1<SurveyResponse>() {
                                @Override
                                public void call(final SurveyResponse response) {
                                        UIHelper.getInstance().dismissProgressDialog();
                                        if(response.status.equals("success")) {
                                                if (response.details != null && response.details.size() > 0) {
                                                        mainHandler.post(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                        //load web page
                                                                        if (getView() != null) {
                                                                                loadPage(response.details.get(0).url);
                                                                        }
                                                                        //check have user open this survey before
                                                                        checkCount(response.details.get(0).type);
                                                                }
                                                        });
                                                }
                                                else{
                                                        mainHandler.post(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                        if (getView() != null) {
                                                                                getView().progressBar.setVisibility(View.GONE);
                                                                                getView().messageTV.setVisibility(View.VISIBLE);
                                                                                getView().messageTV.setText("No surveys at this moment");
                                                                        }
                                                                }
                                                        });
                                                }
                                        }
                                        else{
                                                mainHandler.post(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                                if (getView() != null) {
                                                                        getView().progressBar.setVisibility(View.GONE);
                                                                        getView().messageTV.setVisibility(View.VISIBLE);
                                                                        getView().messageTV.setText("No surveys at this moment");
                                                                }
                                                        }
                                                });
                                        }
                                }
                        }, new Action1<Throwable>() {
                                @Override
                                public void call(Throwable throwable) {
                                        Log.e("Mobisys: ", "cannot get survey details", throwable);
                                        mainHandler.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                        if (getView() != null) {
                                                                getView().progressBar.setVisibility(View.GONE);
                                                                getView().messageTV.setVisibility(View.VISIBLE);
                                                                getView().messageTV.setText("No surveys at this moment.");
                                                                UIHelper.getInstance().showAlert(getView().getContext(), "Please try again later.");
                                                        }
                                                }
                                        });
                                }
                        });
        }

        @Subscribe
        public void badgeNotiEvent(BadgeNotiEvent event){
                MasterPointService.getInstance().getBadgesAPI();
                MasterPointService.getInstance().showToolTips(getView().surveyWV, event.badgeName);
        }
}
