package sg.edu.smu.livelabs.mobicom.presenters;

import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebViewClient;

import autodagger.AutoComponent;
import automortar.AutoScreen;
import mortar.ViewPresenter;
import sg.edu.smu.livelabs.mobicom.AppDependencies;
import sg.edu.smu.livelabs.mobicom.DaggerScope;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.flow.Layout;
import sg.edu.smu.livelabs.mobicom.net.RestClient;
import sg.edu.smu.livelabs.mobicom.views.Organizer3View;

/**
 * Created by smu on 29/4/16.
 */
@AutoScreen(
        component = @AutoComponent(dependencies = OrganizersPresenter.class, superinterfaces = AppDependencies.class),
        screenAnnotations = Layout.class
)
@DaggerScope(Organizer3Presenter.class)
@Layout(R.layout.organizer3_view)
public class Organizer3Presenter extends ViewPresenter<Organizer3View>{
    private String url = RestClient.WEBVIEW_BASE_URL + "ext_review";
    @Override
    protected void onLoad(Bundle savedInstanceState) {
        super.onLoad(savedInstanceState);
        try {
            getView().webView.getSettings().setJavaScriptEnabled(true);
            getView().webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
            getView().webView.setWebViewClient(new WebViewClient());
            getView().webView.setWebChromeClient(new WebChromeClient());
            getView().webView.loadUrl(url);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
