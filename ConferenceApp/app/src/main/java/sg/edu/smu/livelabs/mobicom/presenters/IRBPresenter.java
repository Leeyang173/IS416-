package sg.edu.smu.livelabs.mobicom.presenters;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
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
import sg.edu.smu.livelabs.mobicom.busEvents.NetworkEvent;
import sg.edu.smu.livelabs.mobicom.flow.Layout;
import sg.edu.smu.livelabs.mobicom.net.RestClient;
import sg.edu.smu.livelabs.mobicom.views.IRBView;

/**
 * Created by smu on 13/6/16.
 */
@AutoScreen(
        component = @AutoComponent(dependencies = MainActivity.class, superinterfaces = AppDependencies.class),
        screenAnnotations = Layout.class
)
@DaggerScope(IRBPresenter.class)
@Layout(R.layout.irb_view)
public class IRBPresenter extends ViewPresenter<IRBView> {
    public static final String NAME = "IRBPresenter";
    private String url = RestClient.WEBVIEW_BASE_URL + "terms";
    private ActionBarOwner actionBarOwner;
    private MainActivity mainActivity;
    private Bus bus;
    private boolean error;

    public IRBPresenter( Bus bus, ActionBarOwner actionBarOwner, MainActivity mainActivity){
        this.actionBarOwner = actionBarOwner;
        this.mainActivity = mainActivity;
        this.bus = bus;
    }

    @Override
    protected void onEnterScope(MortarScope scope) {
        super.onEnterScope(scope);
        App.getInstance().currentPresenter = NAME;
        bus.register(this);
    }

    @Override
    protected void onExitScope() {
        bus.unregister(this);
        super.onExitScope();
    }

    @Override
    protected void onLoad(Bundle savedInstanceState) {
        super.onLoad(savedInstanceState);
        mainActivity.setVisibleToolbar(View.VISIBLE);
        mainActivity.setVisibleBottombar(View.GONE);
        actionBarOwner.setConfig(new ActionBarOwner.Config(false, "Terms & Conditions", null));
        try {
            getView().webView.getSettings().setJavaScriptEnabled(true);
            getView().webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
            getView().webView.setWebChromeClient(new WebChromeClient(){
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

            getView().webView.setWebViewClient(new WebViewClient() {
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
                            getView().webView.setVisibility(View.GONE);
                            getView().progressBar.setVisibility(View.GONE);
                            getView().messageTV.setVisibility(View.GONE);
                            getView().noResultTV.setText("Unable to load web page");

                        }
                    }
                }

                @Override
                public void onReceivedHttpError(final WebView view, final WebResourceRequest request, WebResourceResponse errorResponse) {
                    error = true;
                }
            });
            getView().webView.loadUrl(url);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (MainActivity.mode == MainActivity.OFFLINE_MODE){
            getView().webView.setVisibility(View.GONE);
        } else {
            getView().webView.setVisibility(View.VISIBLE);
        }
    }

    @Subscribe
    public void checkNetwork(NetworkEvent event){
        if (MainActivity.mode == MainActivity.ONLINE_MODE){
            try {
                getView().webView.loadUrl(url);
                getView().webView.setVisibility(View.VISIBLE);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            getView().webView.setVisibility(View.GONE);
        }
    }
}
