package sg.edu.smu.livelabs.mobicom.presenters;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;

import autodagger.AutoComponent;
import automortar.AutoScreen;
import flow.Flow;
import mortar.ViewPresenter;
import sg.edu.smu.livelabs.mobicom.ActionBarOwner;
import sg.edu.smu.livelabs.mobicom.AppDependencies;
import sg.edu.smu.livelabs.mobicom.DaggerScope;
import sg.edu.smu.livelabs.mobicom.MainActivity;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.flow.Layout;
import sg.edu.smu.livelabs.mobicom.net.RestClient;
import sg.edu.smu.livelabs.mobicom.presenters.screen.ARNavigationScreen;
import sg.edu.smu.livelabs.mobicom.presenters.screen.AboutUsScreen;
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
        mainActivity.setVisibleBottombar(View.VISIBLE);
        actionBarOwner.setConfig(new ActionBarOwner.Config(true, "Navigation", null));
        getView().messageTV.setText("Where are you headed?");
        getView().arButton.setText("Toggle AR");
        getView().locDDL.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view,
                                   int position, long id) {

            String location = getView().locDDL.getSelectedItem().toString();
            System.out.println(location);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            // TODO Auto-generated method stub

        }
        });

        getView().arButton.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Flow.get(getView().getContext()).set(new ARNavigationScreen());
            }
        });
    }
}
