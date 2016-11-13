package sg.edu.smu.livelabs.mobicom.presenters;

import android.os.Bundle;

import autodagger.AutoComponent;
import automortar.AutoScreen;
import flow.Flow;
import flow.path.Path;
import mortar.MortarScope;
import mortar.ViewPresenter;
import rx.functions.Action0;
import sg.edu.smu.livelabs.mobicom.ActionBarOwner;
import sg.edu.smu.livelabs.mobicom.App;
import sg.edu.smu.livelabs.mobicom.AppDependencies;
import sg.edu.smu.livelabs.mobicom.DaggerScope;
import sg.edu.smu.livelabs.mobicom.MainActivity;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.adapters.SlidePagerAdapter;
import sg.edu.smu.livelabs.mobicom.flow.Layout;
//import sg.edu.smu.livelabs.mobicom.presenters.screen.Organizer1Screen;
//import sg.edu.smu.livelabs.mobicom.presenters.screen.Organizer2Screen;
//import sg.edu.smu.livelabs.mobicom.presenters.screen.Organizer3Screen;
import sg.edu.smu.livelabs.mobicom.presenters.screen.NotificationScreen;
import sg.edu.smu.livelabs.mobicom.presenters.screen.Organizer1Screen;
import sg.edu.smu.livelabs.mobicom.presenters.screen.Organizer2Screen;
import sg.edu.smu.livelabs.mobicom.presenters.screen.Organizer3Screen;
import sg.edu.smu.livelabs.mobicom.views.OrganizersView;

/**
 * Created by smu on 28/4/16.
 */
@AutoScreen(
        component = @AutoComponent(dependencies = MainActivity.class, superinterfaces = AppDependencies.class),
        screenAnnotations = Layout.class
)
@DaggerScope(OrganizersPresenter.class)
@Layout(R.layout.organizers_view)
public class OrganizersPresenter extends ViewPresenter<OrganizersView>{
    private SlidePagerAdapter pagerAdapter;
    private ActionBarOwner actionBarOwner;
    private MainActivity mainActivity;
    public OrganizersPresenter(ActionBarOwner actionBarOwner, MainActivity mainActivity){
        this.actionBarOwner = actionBarOwner;
        this.mainActivity = mainActivity;
    }

    @Override
    protected void onLoad(Bundle savedInstanceState) {
        super.onLoad(savedInstanceState);
        mainActivity.currentTab = MainActivity.OTHER_TAB;
        actionBarOwner.setConfig(new ActionBarOwner.Config(true,
                new ActionBarOwner.MenuAction("Organizers", new Action0() {
                    @Override
                    public void call() {
                        getView().viewPager.setCurrentItem(0);
                    }
                }),
                new ActionBarOwner.MenuAction("Technical Program", new Action0() {
                    @Override
                    public void call() {
                        getView().viewPager.setCurrentItem(1);
                    }
                }),
//                new ActionBarOwner.MenuAction(R.drawable.icon_openned, new Action0() {
//                    @Override
//                    public void call() {
//
//                    }
//                }),
                ActionBarOwner.Config.LEFT_FOCUS));
        pagerAdapter = new SlidePagerAdapter(getView().getContext(),
                new Path[]{new Organizer1Screen(), new Organizer2Screen()});
        getView().viewPager.setAdapter(pagerAdapter);
    }

    @Override
    protected void onEnterScope(MortarScope scope) {
        super.onEnterScope(scope);
        if(App.getInstance().currentPresenter.equals(MorePresenter.NAME)) {
            App.getInstance().setPrevious();
        }
        App.getInstance().currentPresenter = "OrgnaizersPresenter";
    }

    @Override
    protected void onExitScope() {
        super.onExitScope();
        App.getInstance().currentPresenter = "";

    }
}
