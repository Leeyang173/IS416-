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
import sg.edu.smu.livelabs.mobicom.flow.Layout;
import sg.edu.smu.livelabs.mobicom.net.RestClient;
import sg.edu.smu.livelabs.mobicom.views.PrizesView;

/**
 * Created by smu on 22/2/16.
 */
@AutoScreen(
        component = @AutoComponent(dependencies = MainActivity.class, superinterfaces = AppDependencies.class),
        screenAnnotations = Layout.class
)
@DaggerScope(PrizesPresenter.class)
@Layout(R.layout.prizes_view)
public class PrizesPresenter extends ViewPresenter<PrizesView>{

        private ActionBarOwner actionBarOwner;
        private final RestClient restClient;
        private Bus bus;
        public static String NAME = "PrizePresenter";

        public PrizesPresenter(RestClient restClient, Bus bus, ActionBarOwner actionBarOwner) {
                this.restClient = restClient;
                this.bus = bus;
                this.actionBarOwner = actionBarOwner;
        }

        @Override
        protected void onLoad(Bundle savedInstanceState) {
                super.onLoad(savedInstanceState);
                Log.d(App.APP_TAG, " PrizePresenter onload");

                actionBarOwner.setConfig(new ActionBarOwner.Config(true, "Prizes", null));


                ConnectivityManager cm = (ConnectivityManager) getView().getContext().getSystemService(Context.CONNECTIVITY_SERVICE);

                if(cm.getActiveNetworkInfo() != null) {
                        loadPage(RestClient.PRIZE_BASE_URL);
                        getView().message.setVisibility(View.GONE);
                        getView().progressBar.setVisibility(View.GONE);
                }
                else{
                        getView().webView.setVisibility(View.GONE);
                        getView().progressBar.setVisibility(View.GONE);
                        getView().message.setVisibility(View.VISIBLE);
                        getView().message.setText("Prizes cannot be loaded without Internet connection");
                }

        }

        private void loadPage(String surveyURL){
                getView().webView.setVisibility(View.VISIBLE);
                getView().progressBar.setVisibility(View.GONE);
                getView().message.setVisibility(View.GONE);
                getView().webView.getSettings().setJavaScriptEnabled(true);
                getView().webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
                getView().webView.setWebViewClient(new WebViewClient());
                getView().webView.setWebChromeClient(new WebChromeClient());

                getView().webView.loadUrl(surveyURL);
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
                                        getView().progressBar.setVisibility(View.VISIBLE);
                                        getView().message.setVisibility(View.VISIBLE);
                                        getView().message.setText("Loading");
                                }

                        }

                        @Override
                        public void onPageFinished(WebView view, String url) {
                                super.onPageFinished(view, url);
                                if (getView() != null) {
                                        getView().progressBar.setVisibility(View.GONE);
                                        getView().message.setVisibility(View.GONE);
                                }
                        }
                });
        }

        @Override
        protected void onEnterScope(MortarScope scope) {
                super.onEnterScope(scope);
                bus.register(this);
                App.getInstance().currentPresenter = NAME;
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
        }
}
