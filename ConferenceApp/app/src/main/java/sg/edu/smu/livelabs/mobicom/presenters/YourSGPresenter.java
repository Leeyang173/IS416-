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
import sg.edu.smu.livelabs.mobicom.views.YourSGView;

/**
 * Created by smu on 28/4/16.
 */
@AutoScreen(
        component = @AutoComponent(dependencies = MainActivity.class, superinterfaces = AppDependencies.class),
        screenAnnotations = Layout.class
)
@DaggerScope(YourSGPresenter.class)
@Layout(R.layout.your_sg_view)
public class YourSGPresenter extends ViewPresenter<YourSGView>{
    private ActionBarOwner actionBarOwner;
    private MainActivity mainActivity;
    private String url = "http://www.yoursingapore.com/en.html";
    public YourSGPresenter (ActionBarOwner actionBarOwner, MainActivity mainActivity){
        this.actionBarOwner = actionBarOwner;
        this.mainActivity = mainActivity;
    }

    @Override
    protected void onLoad(Bundle savedInstanceState) {
        super.onLoad(savedInstanceState);
        mainActivity.currentTab = MainActivity.OTHER_TAB;
        actionBarOwner.setConfig(new ActionBarOwner.Config(true, "Your SG", null));
        try {
            getView().webView.loadUrl(url);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
