package sg.edu.smu.livelabs.mobicom.presenters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

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
import sg.edu.smu.livelabs.mobicom.UIHelper;
import sg.edu.smu.livelabs.mobicom.adapters.GameListAdapter;
import sg.edu.smu.livelabs.mobicom.busEvents.GamesEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.UnregisterGameEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.UpdateNetworkEvent;
import sg.edu.smu.livelabs.mobicom.flow.Layout;
import sg.edu.smu.livelabs.mobicom.models.User;
import sg.edu.smu.livelabs.mobicom.models.data.GameListEntity;
import sg.edu.smu.livelabs.mobicom.net.RestClient;
//import sg.edu.smu.livelabs.mobicom.presenters.screen.BeaconScreen;
//import sg.edu.smu.livelabs.mobicom.presenters.screen.EVAPromotionScreen;
//import sg.edu.smu.livelabs.mobicom.presenters.screen.FavoriteScreen;
//import sg.edu.smu.livelabs.mobicom.presenters.screen.IceBreakerScreen;
//import sg.edu.smu.livelabs.mobicom.presenters.screen.QuizScreen;
//import sg.edu.smu.livelabs.mobicom.presenters.screen.ScavengerHuntScreen;
//import sg.edu.smu.livelabs.mobicom.presenters.screen.StumpListScreen;
//import sg.edu.smu.livelabs.mobicom.presenters.screen.SurveyScreen;
//import sg.edu.smu.livelabs.mobicom.presenters.screen.VotingListScreen;
//import sg.edu.smu.livelabs.mobicom.presenters.screen.VotingScreen;
import sg.edu.smu.livelabs.mobicom.presenters.screen.BeaconScreen;
import sg.edu.smu.livelabs.mobicom.presenters.screen.BingoScreen;
import sg.edu.smu.livelabs.mobicom.presenters.screen.EVAPromotionScreen;
import sg.edu.smu.livelabs.mobicom.presenters.screen.FavoriteScreen;
import sg.edu.smu.livelabs.mobicom.presenters.screen.IceBreakerScreen;
import sg.edu.smu.livelabs.mobicom.presenters.screen.QuizScreen;
import sg.edu.smu.livelabs.mobicom.presenters.screen.ScavengerHuntScreen;
import sg.edu.smu.livelabs.mobicom.presenters.screen.StumpListScreen;
import sg.edu.smu.livelabs.mobicom.presenters.screen.SurveyScreen;
import sg.edu.smu.livelabs.mobicom.presenters.screen.VotingListScreen;
import sg.edu.smu.livelabs.mobicom.presenters.screen.VotingScreen;
import sg.edu.smu.livelabs.mobicom.services.DatabaseService;
import sg.edu.smu.livelabs.mobicom.services.GameService;
import sg.edu.smu.livelabs.mobicom.services.TrackingService;
import sg.edu.smu.livelabs.mobicom.views.GamesView;

/**
 * Created by smu on 22/2/16.
 */
@AutoScreen(
        component = @AutoComponent(dependencies = MainActivity.class, superinterfaces = AppDependencies.class),
        screenAnnotations = Layout.class
)
@DaggerScope(GamesPresenter.class)
@Layout(R.layout.games_view)
public class GamesPresenter extends ViewPresenter<GamesView> {

        private final RestClient restClient;
        private Bus bus;
        public static String NAME = "GamesPresenter";
        private ActionBarOwner actionBarOwner;

        private User me;
        private boolean isMeAdmin;
        private MainActivity mainActivity;
        private GameListAdapter gameListAdapter;
        private List<GameListEntity> games;

        private ConnectivityManager cm;
        private boolean isFirstTime = true;

        public GamesPresenter(RestClient restClient, Bus bus, ActionBarOwner actionBarOwner, MainActivity mainActivity) {
                this.restClient = restClient;
                this.bus = bus;
                this.actionBarOwner = actionBarOwner;
                this.mainActivity = mainActivity;
                this.isMeAdmin = false;
                games = new ArrayList<>();
        }


        @Override
        protected void onLoad(Bundle savedInstanceState) {
                super.onLoad(savedInstanceState);
                Log.d(App.APP_TAG, " GamePresenter onload");
                View v = getView();

                mainActivity.messageBtn.setSelected(false);
                mainActivity.gamesBtn.setSelected(true);
                mainActivity.agendaBtn.setSelected(false);
                mainActivity.homeBtn.setSelected(false);
                mainActivity.moreBtn.setSelected(false);
                mainActivity.currentTab = MainActivity.GAMES_TAB;

                actionBarOwner.setConfig(new ActionBarOwner.Config(true, "Games", null));

                cm = (ConnectivityManager) getView().getContext().getSystemService(Context.CONNECTIVITY_SERVICE);

                me = DatabaseService.getInstance().getMe();
                if(me.getRole() != null && me.getRole().length > 0) {
                        for (String s : me.getRole()) {
                                if (s.trim().toLowerCase().equals("moderator")) {
                                        isMeAdmin = true;
                                        break;
                                }
                        }
                }

                if(cm.getActiveNetworkInfo() != null) {
                                UIHelper.getInstance().showProgressDialog(getView().getContext(), "Fetching games...", false);
                                GameService.getInstance().loadGameAPI(Long.toString(me.getUID()));

                                //TODO move this to the main page (login or home page)
                                isFirstTime = false;
                }
                else{
                        loadGameData(false);
                }

                App.getInstance().startNetworkMonitoringReceiver();
        }

        @Override
        protected void onEnterScope(MortarScope scope) {
                super.onEnterScope(scope);
                bus.register(this);
                if(App.getInstance().currentPresenter.equals(MorePresenter.NAME)) {
                        App.getInstance().setPrevious();
                }
                App.getInstance().currentPresenter = NAME;

                mainActivity.setVisibleBottombar(View.VISIBLE);
        }

        @Override
        protected void onExitScope() {
                super.onExitScope();
                bus.unregister(this);
                if (NAME.equals(App.getInstance().currentPresenter)){
                        App.getInstance().currentPresenter = "";
                }
                App.getInstance().previousPresenter = "";
        }

        public Drawable getResource(int id){
                return getView().getResources().getDrawable(id);
        }

        @Subscribe
        public void games(GamesEvent event){
                UIHelper.getInstance().dismissProgressDialog();
                games.clear();
                games = event.gameListEntityList;
                loadGameData(true);
        }



        private void loadGameData(boolean fromAPI){
                if(games == null){
                        games = new ArrayList<>();
                }

                if(!fromAPI) {
                        games.clear();
                        games = GameService.getInstance().getGames();
                }
                games.add(0, new GameListEntity(999l, "empty", "empty", "", "", null, null, null)); //need to add one more (empty) for profile

                if(getView() != null) {
                        getView().msgTV.setVisibility(View.GONE);
                        gameListAdapter = new GameListAdapter(getView(), games, bus);
                        getView().gameList.setAdapter(gameListAdapter);

                        getView().gameList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                        if (games.size() > position) {
                                                GameListEntity game = games.get(position);
                                                TrackingService.getInstance().sendTracking("406", "games",
                                                        Long.toString(game.getId()), game.getKeyword(), "", "");
                                                //position 0 is reserved for profile, don't use position 0
                                                if (game.getKeyword().equals("ice breaker")) {
                                                        Flow.get(getView()).set(new IceBreakerScreen());
                                                } else if (game.getKeyword().equals("scavenger hunt")) {
                                                        Flow.get(getView()).set(new ScavengerHuntScreen());
                                                } else if (game.getKeyword().equals("polling")) {
                                                        if (isMeAdmin)
                                                                Flow.get(getView()).set(new VotingScreen(0, game.getGameName()));
                                                        else
                                                                Flow.get(getView()).set(new VotingListScreen());
                                                } else if (game.getKeyword().equals("survey")) {
                                                        Flow.get(getView()).set(new SurveyScreen());
                                                } else if (game.getKeyword().equals("quiz")) {
                                                        Flow.get(getView()).set(new QuizScreen(5l));
                                                } else if (game.getKeyword().equals("demo")) {
                                                        Flow.get(getView()).set(new BeaconScreen());

                                                } else if (game.getKeyword().equals("coolfie")) {
                                                        Flow.get(getView()).set(new EVAPromotionScreen());
                                                } else if (game.getKeyword().equals("favourite")) {
                                                        Flow.get(getView()).set(new FavoriteScreen());
                                                } else if (game.getKeyword().equals("stump")) {
                                                        Flow.get(getView()).set(new StumpListScreen());
                                                }else if (game.getKeyword().equals("photo_bingo")) {
                                                        Flow.get(getView()).set(new BingoScreen());
                                                }

                                        }
                                }
                        });

                        if (games.size() > 1) {
                                getView().msgTV.setVisibility(View.GONE);
                        } else {
                                getView().msgTV.setVisibility(View.VISIBLE);
                        }
                }
        }

        @Subscribe
        public void updateNetwork(UpdateNetworkEvent event) {
                if (event.isConnected) {
                        if(GameService.getInstance().getGameSize() == 0 && isFirstTime){
                                UIHelper.getInstance().showProgressDialog(getView().getContext(), "Fetching games...", false);
                                GameService.getInstance().loadGameAPI(Long.toString(me.getUID()));
                                isFirstTime = false;
                        }
                }
                else{
                        loadGameData(false);
                }
        }

        @Subscribe
        public void unregisterGameReceiver(UnregisterGameEvent event){
                if(event.toUnregister) {
                        App.getInstance().stopNetworkMonitoringReceiver();
                }
                else{
                        App.getInstance().startNetworkMonitoringReceiver();
                }
        }


}
