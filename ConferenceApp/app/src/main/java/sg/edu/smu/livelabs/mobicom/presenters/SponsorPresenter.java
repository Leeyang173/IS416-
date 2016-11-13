package sg.edu.smu.livelabs.mobicom.presenters;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
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

import autodagger.AutoComponent;
import automortar.AutoScreen;
import mortar.ViewPresenter;
import sg.edu.smu.livelabs.mobicom.ActionBarOwner;
import sg.edu.smu.livelabs.mobicom.AppDependencies;
import sg.edu.smu.livelabs.mobicom.DaggerScope;
import sg.edu.smu.livelabs.mobicom.MainActivity;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.flow.Layout;
import sg.edu.smu.livelabs.mobicom.net.RestClient;
import sg.edu.smu.livelabs.mobicom.views.SponsorView;

/**
 * Created by smu on 28/4/16.
 */
@AutoScreen(
        component = @AutoComponent(dependencies = MainActivity.class, superinterfaces = AppDependencies.class),
        screenAnnotations = Layout.class
)
@DaggerScope(SponsorPresenter.class)
@Layout(R.layout.sponsor_view)
public class SponsorPresenter extends ViewPresenter<SponsorView>{
    private MainActivity mainActivity;
    private ActionBarOwner actionBarOwner;
    private String url = RestClient.WEBVIEW_BASE_URL + "sponsor";
    private boolean error = false;

    public SponsorPresenter(MainActivity mainActivity, ActionBarOwner actionBarOwner){
        this.mainActivity = mainActivity;
        this.actionBarOwner = actionBarOwner;
    }

    @Override
    protected void onLoad(Bundle savedInstanceState) {
        super.onLoad(savedInstanceState);
        mainActivity.currentTab = MainActivity.OTHER_TAB;
        actionBarOwner.setConfig(new ActionBarOwner.Config(true, "With Thanks", null));
        try {
            ConnectivityManager cm = (ConnectivityManager) getView().getContext().getSystemService(Context.CONNECTIVITY_SERVICE);

            if(cm.getActiveNetworkInfo() == null) {
                getView().webView.setVisibility(View.GONE);
                getView().progressBar.setVisibility(View.GONE);
                getView().messageTV.setVisibility(View.VISIBLE);
                getView().messageTV.setText("Unable to load web page");
                return;
            }

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
            getView().webView.loadUrl(url);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
