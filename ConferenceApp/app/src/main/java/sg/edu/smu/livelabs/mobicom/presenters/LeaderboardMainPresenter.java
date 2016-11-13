package sg.edu.smu.livelabs.mobicom.presenters;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import autodagger.AutoComponent;
import automortar.AutoScreen;
import automortar.ScreenParam;
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
import sg.edu.smu.livelabs.mobicom.busEvents.LeaderboardDismissProgressEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.LeaderboardEvent;
import sg.edu.smu.livelabs.mobicom.flow.Layout;
import sg.edu.smu.livelabs.mobicom.models.User;
//import sg.edu.smu.livelabs.mobicom.presenters.screen.Leaderboard2Screen;
import sg.edu.smu.livelabs.mobicom.presenters.screen.Leaderboard2Screen;
import sg.edu.smu.livelabs.mobicom.services.DatabaseService;
import sg.edu.smu.livelabs.mobicom.services.GameService;
import sg.edu.smu.livelabs.mobicom.services.ScreenService;
import sg.edu.smu.livelabs.mobicom.views.LeaderboardMainView;

/**
 * Created by smu on 4/4/16.
 */
@AutoScreen(
        component = @AutoComponent(dependencies = MainActivity.class, superinterfaces = AppDependencies.class),
        screenAnnotations = Layout.class
)
@DaggerScope(LeaderboardMainPresenter.class)
@Layout(R.layout.leaderboard_main_view)
public class LeaderboardMainPresenter extends ViewPresenter<LeaderboardMainView> {

    private static String NAME = "LeaderboardMainPresenter";
    private MainActivity mainActivity;
    private ActionBarOwner actionBarOwner;
    private ScreenService screenService;
    private Context context;
    private Bus bus;
    private SlidePagerAdapter adapter;
    private int focus;
    private ActionBarOwner.Config c;
    private String fromWhere;
    public MaterialDialog progress;

    public LeaderboardMainPresenter(MainActivity mainActivity, ActionBarOwner actionBarOwner,
                                    ScreenService screenService, Bus bus, @ScreenParam int focus, @ScreenParam String fromWhere){
        this.mainActivity = mainActivity;
        this.actionBarOwner = actionBarOwner;
        this.screenService = screenService;
        this.bus = bus;
        this.focus = focus;
        if (this.focus == -1){
            this.focus = ActionBarOwner.Config.LEFT_FOCUS;
        }
        this.fromWhere = fromWhere;
    }

    @Override
    protected void onLoad(Bundle savedInstanceState) {
        super.onLoad(savedInstanceState);
        if(!hasView()) return;
        context = getView().getContext();
        actionBarOwner.setConfig(new ActionBarOwner.Config(true, "Leaderboard", null));

        progress = new MaterialDialog.Builder(getView().getContext())
                .title(getView().getContext().getString(R.string.app_name))
                .content("Loading Leaderboard...")
                .progress(true, 0)
                .cancelable(false)
                .show();
//        UIHelper.getInstance().showProgressDialog(getView().getContext(), "Loading Leaderboard...", false);
        User me = DatabaseService.getInstance().getMe();
        GameService.getInstance().loadRanking(Long.toString(me.getUID()));

        Path[] paths = new Path[]{
                new Leaderboard2Screen(mainActivity, 1),
                new Leaderboard2Screen(mainActivity, 2)};
        adapter = new SlidePagerAdapter(context, paths);
        getView().viewPager.setAdapter(adapter);
        getView().viewPager.setCurrentItem(focus);
    }

    @Override
    protected void onExitScope() {
        bus.unregister(this);
        super.onExitScope();

        if(!GameService.getInstance().fromGames(App.getInstance().previousPresenter)){
            mainActivity.setVisibleBottombar(View.VISIBLE);
        }

        if(App.getInstance().currentPresenter.equals(GamesPresenter.NAME)){
            actionBarOwner.setConfig(new ActionBarOwner.Config(false, "Games", null));
        }
    }

    @Override
    protected void onEnterScope(MortarScope scope) {
        bus.register(this);
        super.onEnterScope(scope);
    }


    @Subscribe
    public void leaderboardEvent(LeaderboardEvent event){
//        progress.dismiss();
        if(event.showAsset) {
            c = actionBarOwner.getConfig();
            actionBarOwner.setConfig(new ActionBarOwner.Config(true, null,
                    new ActionBarOwner.MenuAction("Mobisys", new Action0() {
                        @Override
                        public void call() {
                            getView().viewPager.setCurrentItem(0);
                        }
                    }),
                    new ActionBarOwner.MenuAction("Asset", new Action0() {
                        @Override
                        public void call() {
                            getView().viewPager.setCurrentItem(1);
                        }
                    }),
                    null
                    , ActionBarOwner.Config.LEFT_FOCUS));
        }
    }

    @Subscribe
    public void dismissLoadingBar(LeaderboardDismissProgressEvent event){
        if(progress != null) {
            progress.dismiss();
        }
    }
}
