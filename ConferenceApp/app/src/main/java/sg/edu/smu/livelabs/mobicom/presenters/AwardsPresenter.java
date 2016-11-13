package sg.edu.smu.livelabs.mobicom.presenters;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.os.Bundle;
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
import mortar.MortarScope;
import mortar.ViewPresenter;
import sg.edu.smu.livelabs.mobicom.ActionBarOwner;
import sg.edu.smu.livelabs.mobicom.App;
import sg.edu.smu.livelabs.mobicom.AppDependencies;
import sg.edu.smu.livelabs.mobicom.DaggerScope;
import sg.edu.smu.livelabs.mobicom.MainActivity;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.busEvents.UnregisterSurveyEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.UpdateNetworkEvent;
import sg.edu.smu.livelabs.mobicom.flow.Layout;
import sg.edu.smu.livelabs.mobicom.models.User;
import sg.edu.smu.livelabs.mobicom.net.RestClient;
import sg.edu.smu.livelabs.mobicom.services.DatabaseService;
import sg.edu.smu.livelabs.mobicom.services.GameService;
import sg.edu.smu.livelabs.mobicom.views.AwardsView;

/**
 * Created by smu on 22/2/16.
 */
@AutoScreen(
        component = @AutoComponent(dependencies = MainActivity.class, superinterfaces = AppDependencies.class),
        screenAnnotations = Layout.class
)
@DaggerScope(AwardsPresenter.class)
@Layout(R.layout.awards_view)
public class AwardsPresenter extends ViewPresenter<AwardsView>{

        private ActionBarOwner actionBarOwner;
        private final RestClient restClient;
        private Bus bus;
        public static String NAME = "AwardsPresenter";
        private MainActivity mainActivity;
        private boolean isFirstTime = true;
        private User me;
        private  ConnectivityManager cm;

        public AwardsPresenter(RestClient restClient, Bus bus, ActionBarOwner actionBarOwner, MainActivity mainActivity) {
                this.restClient = restClient;
                this.bus = bus;
                App.getInstance().currentPresenter = "AwardsPresenter";
                this.actionBarOwner = actionBarOwner;
                this.mainActivity = mainActivity;
        }

        @Override
        protected void onLoad(Bundle savedInstanceState) {
                super.onLoad(savedInstanceState);
                Log.d(App.APP_TAG, " AwardsPresenter onload");

                actionBarOwner.setConfig(new ActionBarOwner.Config(true, "Awards", null));

                me = DatabaseService.getInstance().getMe();

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
                        getView().messageTV.setText("Awards cannot be loaded without Internet connection");
                }

                App.getInstance().startNetworkMonitoringReceiver();
        }

        private void loadPage(){
                getView().surveyWV.setVisibility(View.VISIBLE);
                getView().progressBar.setVisibility(View.GONE);
                getView().messageTV.setVisibility(View.GONE);
                getView().surveyWV.getSettings().setJavaScriptEnabled(true);
                getView().surveyWV.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
                getView().surveyWV.setWebViewClient(new WebViewClient());
                getView().surveyWV.setWebChromeClient(new WebChromeClient());

//                String url = GOOGLE_FORM + surveyURL;
                getView().surveyWV.loadUrl(RestClient.AWARDS_RESULT);
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

        @Subscribe
        public void updateNetwork(UpdateNetworkEvent event) {
                if (event.isConnected && isFirstTime) {
                        isFirstTime = false;
                        loadPage();
                }
        }
}
