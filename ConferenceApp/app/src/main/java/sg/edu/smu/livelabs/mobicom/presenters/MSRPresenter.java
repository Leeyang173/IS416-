package sg.edu.smu.livelabs.mobicom.presenters;

import android.os.Bundle;

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
import sg.edu.smu.livelabs.mobicom.views.MSRView;

/**
 * Created by smu on 13/5/16.
 */
@AutoScreen(
        component = @AutoComponent(dependencies = MainActivity.class, superinterfaces = AppDependencies.class),
        screenAnnotations = Layout.class
)
@DaggerScope(MSRPresenter.class)
@Layout(R.layout.msr_view)
public class MSRPresenter extends ViewPresenter<MSRView> {
    private MainActivity mainActivity;
    private ActionBarOwner actionBarOwner;
    private String url = RestClient.WEBVIEW_BASE_URL + "terms";

    public MSRPresenter(MainActivity mainActivity, ActionBarOwner actionBarOwner){
        this.mainActivity = mainActivity;
        this.actionBarOwner = actionBarOwner;
    }

    @Override
    protected void onLoad(Bundle savedInstanceState) {
        super.onLoad(savedInstanceState);
        mainActivity.currentTab = MainActivity.OTHER_TAB;
        actionBarOwner.setConfig(new ActionBarOwner.Config(true, "MICROSOFT", null));
        try {
            getView().webView.loadUrl(url);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
