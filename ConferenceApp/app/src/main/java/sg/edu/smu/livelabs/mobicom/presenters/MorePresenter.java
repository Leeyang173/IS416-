package sg.edu.smu.livelabs.mobicom.presenters;

import android.os.Bundle;
import android.view.View;

import autodagger.AutoComponent;
import automortar.AutoScreen;
import flow.Flow;
import mortar.MortarScope;
import mortar.ViewPresenter;
import sg.edu.smu.livelabs.mobicom.ActionBarOwner;
import sg.edu.smu.livelabs.mobicom.App;
import sg.edu.smu.livelabs.mobicom.AppDependencies;
import sg.edu.smu.livelabs.mobicom.DaggerScope;
import sg.edu.smu.livelabs.mobicom.MainActivity;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.adapters.MoreAdapter;
import sg.edu.smu.livelabs.mobicom.flow.Layout;
import sg.edu.smu.livelabs.mobicom.presenters.screen.PrivacyPolicyScreen;
import sg.edu.smu.livelabs.mobicom.presenters.screen.TermScreen;
import sg.edu.smu.livelabs.mobicom.views.MoreView;

/**
 * Created by smu on 28/2/16.
 */
@AutoScreen(
        component = @AutoComponent(dependencies = MainActivity.class, superinterfaces = AppDependencies.class),
        screenAnnotations = Layout.class
)
@DaggerScope(MorePresenter.class)
@Layout(R.layout.more_view)
public class MorePresenter extends ViewPresenter<MoreView> {

    public static String NAME = "MorePresenter";
    private ActionBarOwner actionBarOwner;
    private MoreAdapter moreAdapter;
    private MainActivity mainActivity;

    public MorePresenter(ActionBarOwner actionBarOwner, MainActivity mainActivity){
        this.actionBarOwner = actionBarOwner;
        this.mainActivity = mainActivity;
    }

    @Override
    protected void onLoad(Bundle savedInstanceState) {
        super.onLoad(savedInstanceState);
        if(!hasView()) return;
        mainActivity.messageBtn.setSelected(false);
        mainActivity.gamesBtn.setSelected(false);
        mainActivity.agendaBtn.setSelected(false);
        mainActivity.homeBtn.setSelected(false);
        mainActivity.moreBtn.setSelected(true);
        mainActivity.currentTab = MainActivity.MORE_TAB;

        actionBarOwner.setConfig(new ActionBarOwner.Config(true, "More", null));
        moreAdapter = new MoreAdapter(getView().getContext(), mainActivity);
        getView().moreGridView.setAdapter(moreAdapter);
        getView().privacyPolicyTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Flow.get(getView().getContext()).set(new PrivacyPolicyScreen());
            }
        });

        getView().termTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Flow.get(getView().getContext()).set(new TermScreen());
            }
        });
    }

    @Override
    protected void onEnterScope(MortarScope scope) {
        super.onEnterScope(scope);
        App.getInstance().previousPresenter = "";
        mainActivity.setVisibleBottombar(View.VISIBLE);
    }

    @Override
    protected void onExitScope() {
        super.onExitScope();
    }
}
