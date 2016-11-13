package sg.edu.smu.livelabs.mobicom.presenters;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import autodagger.AutoComponent;
import automortar.AutoScreen;
import mortar.MortarScope;
import mortar.ViewPresenter;
import sg.edu.smu.livelabs.mobicom.ActionBarOwner;
import sg.edu.smu.livelabs.mobicom.App;
import sg.edu.smu.livelabs.mobicom.AppDependencies;
import sg.edu.smu.livelabs.mobicom.DaggerScope;
import sg.edu.smu.livelabs.mobicom.MainActivity;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.busEvents.BadgeNotiEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.UnregisterSurveyEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.UpdateNetworkEvent;
import sg.edu.smu.livelabs.mobicom.flow.Layout;
import sg.edu.smu.livelabs.mobicom.net.RestClient;
import sg.edu.smu.livelabs.mobicom.services.DatabaseService;
import sg.edu.smu.livelabs.mobicom.services.GameService;
import sg.edu.smu.livelabs.mobicom.services.MasterPointService;
import sg.edu.smu.livelabs.mobicom.views.SurveyResultView;

/**
 * Created by smu on 22/2/16.
 */
@AutoScreen(
        component = @AutoComponent(dependencies = MainActivity.class, superinterfaces = AppDependencies.class),
        screenAnnotations = Layout.class
)
@DaggerScope(SurveyResultPresenter.class)
@Layout(R.layout.survey_result_view)
public class SurveyResultPresenter extends ViewPresenter<SurveyResultView>{

        private ActionBarOwner actionBarOwner;
        private final RestClient restClient;
        private Bus bus;
        public static String NAME = "SurveyResultPresenter";
        private MainActivity mainActivity;
        private boolean isFirstTime = true;
        private boolean error = false;

        private  ConnectivityManager cm;

        public SurveyResultPresenter(RestClient restClient, Bus bus, ActionBarOwner actionBarOwner, MainActivity mainActivity) {
                this.restClient = restClient;
                this.bus = bus;
                App.getInstance().currentPresenter = "SurveyResultPresenter";
                this.actionBarOwner = actionBarOwner;
                this.mainActivity = mainActivity;
        }

        @Override
        protected void onLoad(Bundle savedInstanceState) {
                super.onLoad(savedInstanceState);
                Log.d(App.APP_TAG, " SurveyResultPresenter onload");

                actionBarOwner.setConfig(new ActionBarOwner.Config(true, "Survey", null));


                cm = (ConnectivityManager) getView().getContext().getSystemService(Context.CONNECTIVITY_SERVICE);

                if(cm.getActiveNetworkInfo() != null) {
                        isFirstTime = false;
                        loadPage();
                        getView().messageTV.setVisibility(View.GONE);
                        getView().progressBar.setVisibility(View.GONE);
                }
                else{
                        getView().surveyWV.setVisibility(View.GONE);
                        getView().progressBar.setVisibility(View.GONE);
                        getView().messageTV.setVisibility(View.VISIBLE);
                        getView().messageTV.setText("Survey result cannot be loaded without Internet connection");
                }

                App.getInstance().startNetworkMonitoringReceiver();
        }

        private void loadPage(){
                getView().surveyWV.setVisibility(View.VISIBLE);
                getView().progressBar.setVisibility(View.GONE);
                getView().messageTV.setVisibility(View.GONE);
                getView().surveyWV.getSettings().setJavaScriptEnabled(true);
                getView().surveyWV.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);

                getView().surveyWV.setWebChromeClient(new WebChromeClient(){
                        /**
                         * 当WebView加载之后，返回 HTML 页面的标题 Title
                         * @param view
                         * @param title
                         */
                        @Override
                        public void onReceivedTitle(WebView view, String title) {
                                //判断标题 title 中是否包含有“error”字段，如果包含“error”字段，则设置加载失败，显示加载失败的视图
                                if(!TextUtils.isEmpty(title)&& (title.toLowerCase().contains("error") || title.toLowerCase().contains("not found"))){
                                        error = true;
                                }
                        }
                });

                getView().surveyWV.setWebViewClient(new WebViewClient() {
                        @SuppressWarnings("deprecation")
                        @Override
                        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                                error = true;
                                super.onReceivedError(view, errorCode, description, failingUrl);
                        }

                        @TargetApi(android.os.Build.VERSION_CODES.M)
                        @Override
                        public void onReceivedError(WebView view, WebResourceRequest req, WebResourceError rerr) {
                                // Redirect to deprecated method, so you can use it in all SDK versions
                                onReceivedError(view, rerr.getErrorCode(), rerr.getDescription().toString(), req.getUrl().toString());
                        }

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
                                        if(!error){
                                                getView().progressBar.setVisibility(View.GONE);
                                                getView().messageTV.setVisibility(View.GONE);
                                        }
                                        else{
                                                getView().surveyWV.setVisibility(View.GONE);
                                                getView().progressBar.setVisibility(View.GONE);
                                                getView().messageTV.setVisibility(View.VISIBLE);
                                                getView().messageTV.setText("Unable to load web page");
                                        }
                                }
                        }

                        @Override
                        public void onReceivedHttpError(final WebView view, final WebResourceRequest request, WebResourceResponse errorResponse) {
                                error = true;
                        }
                });

                getView().surveyWV.loadUrl(RestClient.SURVEY_RESULT + DatabaseService.getInstance().getMe().getUID());
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



        @Subscribe
        public void updateNetwork(UpdateNetworkEvent event) {
                if (event.isConnected && isFirstTime) {
                        isFirstTime = false;
                        loadPage();
                }
        }


        @Subscribe
        public void badgeNotiEvent(BadgeNotiEvent event){
                MasterPointService.getInstance().getBadgesAPI();
                MasterPointService.getInstance().showToolTips(getView().surveyWV, event.badgeName);
        }
}
