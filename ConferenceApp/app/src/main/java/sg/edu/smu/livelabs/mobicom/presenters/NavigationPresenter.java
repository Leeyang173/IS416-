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
import sg.edu.smu.livelabs.mobicom.views.NavigationView;

/**
 * Created by Jerms on 14/11/16.
 */
@AutoScreen(
        component = @AutoComponent(dependencies = MainActivity.class, superinterfaces = AppDependencies.class),
        screenAnnotations = Layout.class
)
@DaggerScope(NavigationPresenter.class)
@Layout(R.layout.navi_view)
public class NavigationPresenter extends ViewPresenter<NavigationView> {
    private MainActivity mainActivity;
    private ActionBarOwner actionBarOwner;
    private String url = RestClient.WEBVIEW_BASE_URL + "sponsor";
    private boolean error = false;

    public NavigationPresenter(MainActivity mainActivity, ActionBarOwner actionBarOwner){
        this.mainActivity = mainActivity;
        this.actionBarOwner = actionBarOwner;
    }
    @Override
    protected void onLoad(Bundle savedInstanceState) {
        super.onLoad(savedInstanceState);
        mainActivity.currentTab = MainActivity.OTHER_TAB;
        actionBarOwner.setConfig(new ActionBarOwner.Config(true, "Navigation", null));
        getView().messageTV.setText("Where are you headed?");
        getView().arButton.setText("Toggle AR");
        //getView().locDDL.setText("THIS IS A TEST");

       /* try {
            ConnectivityManager cm = (ConnectivityManager) getView().getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            getView().progressBar.setVisibility(View.GONE);
            if(cm.getActiveNetworkInfo() == null) {
                getView().webView.setVisibility(View.GONE);
                getView().progressBar.setVisibility(View.GONE);
                getView().messageTV.setVisibility(View.VISIBLE);
                getView().messageTV.setText("Unable to load web page");
                return;
            }


        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }*/
    }
}
