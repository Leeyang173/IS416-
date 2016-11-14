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
import sg.edu.smu.livelabs.mobicom.views.ARNavigationView;

/**
 * Created by Jerms on 14/11/16.
 */

@AutoScreen(
        component = @AutoComponent(dependencies = MainActivity.class, superinterfaces = AppDependencies.class),
        screenAnnotations = Layout.class
)
@DaggerScope(SponsorPresenter.class)
@Layout(R.layout.ar_view)
public class ARNavigationPresenter extends ViewPresenter<ARNavigationView> {
    private MainActivity mainActivity;
    private ActionBarOwner actionBarOwner;
    private String url = RestClient.WEBVIEW_BASE_URL + "sponsor";
    private boolean error = false;

    public ARNavigationPresenter(MainActivity mainActivity, ActionBarOwner actionBarOwner){
        this.mainActivity = mainActivity;
        this.actionBarOwner = actionBarOwner;
    }

    @Override
    protected void onLoad(Bundle savedInstanceState) {
        super.onLoad(savedInstanceState);
        mainActivity.currentTab = MainActivity.OTHER_TAB;
        mainActivity.setVisibleBottombar(View.INVISIBLE);
        actionBarOwner.setConfig(new ActionBarOwner.Config(true, "AR View", null));
    }
}
