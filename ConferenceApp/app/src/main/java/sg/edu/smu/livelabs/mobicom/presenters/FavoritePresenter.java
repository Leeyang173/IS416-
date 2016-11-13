package sg.edu.smu.livelabs.mobicom.presenters;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.View;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import autodagger.AutoComponent;
import automortar.AutoScreen;
import mortar.MortarScope;
import mortar.ViewPresenter;
import sg.edu.smu.livelabs.mobicom.ActionBarOwner;
import sg.edu.smu.livelabs.mobicom.App;
import sg.edu.smu.livelabs.mobicom.AppDependencies;
import sg.edu.smu.livelabs.mobicom.DaggerScope;
import sg.edu.smu.livelabs.mobicom.MainActivity;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.UIHelper;
import sg.edu.smu.livelabs.mobicom.adapters.FavoriteAdapter;
import sg.edu.smu.livelabs.mobicom.busEvents.FavoriteEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.UnregisterFavoriteEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.UpdateNetworkEvent;
import sg.edu.smu.livelabs.mobicom.flow.Layout;
import sg.edu.smu.livelabs.mobicom.models.User;
import sg.edu.smu.livelabs.mobicom.models.data.GameListEntity;
import sg.edu.smu.livelabs.mobicom.net.RestClient;
import sg.edu.smu.livelabs.mobicom.services.DatabaseService;
import sg.edu.smu.livelabs.mobicom.services.FavoriteService;
import sg.edu.smu.livelabs.mobicom.services.GameService;
import sg.edu.smu.livelabs.mobicom.views.FavoriteView;

/**
 * Created by johnlee on 27/10/15.
 */
@AutoScreen(
        component = @AutoComponent(dependencies = MainActivity.class, superinterfaces = AppDependencies.class),
        screenAnnotations = Layout.class
)
@DaggerScope(FavoritePresenter.class)
@Layout(R.layout.favorite_view)
public class FavoritePresenter extends ViewPresenter<FavoriteView>{

    //this class handles the list of available favorite session

    private ActionBarOwner actionBarOwner;
    private Bus bus;
    private FavoriteAdapter adapter;
    private MainActivity mainActivity;
    private RestClient restClient;
    private User me;
    public static final String NAME = "FavoritePresenter";
    private boolean isFirstTime;
    private  ConnectivityManager cm;

    public FavoritePresenter(Bus bus, ActionBarOwner actionBarOwner, RestClient restClient, MainActivity mainActivity){
        this.bus = bus;
        this.actionBarOwner = actionBarOwner;
        this.mainActivity = mainActivity;
        this.restClient = restClient;
        this.isFirstTime = true;
    }

    @Override
    protected void onLoad(Bundle savedInstanceState) {
        super.onLoad(savedInstanceState);
        GameListEntity game = GameService.getInstance().getGameByKeyword("favourite");
        if(game != null &&  !game.getGameName().isEmpty()){
            actionBarOwner.setConfig(new ActionBarOwner.Config(true, game.getGameName(), null));
        }
        else{
            actionBarOwner.setConfig(new ActionBarOwner.Config(true, "Awards", null));
        }

        adapter = new FavoriteAdapter(getView().getContext(), mainActivity);
        getView().listView.setAdapter(adapter);

//        GameListEntity favorite = GameService.getInstance().getGame(8);
        if(game != null) {
            if (game.getDescription() != null && !game.getDescription().isEmpty())
                getView().storyTV.setText(game.getDescription());
        }

        cm = (ConnectivityManager) getView().getContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        if(cm.getActiveNetworkInfo() != null) {
            UIHelper.getInstance().showProgressDialog(getView().getContext(), "Loading Favorite List", false);
            FavoriteService.getInstance().getFavoriteList();
            isFirstTime = false;
        }

        me = DatabaseService.getInstance().getMe();

        getView().noResult.setVisibility(View.GONE);

        App.getInstance().startNetworkMonitoringReceiver();
    }

    @Override
    protected void onEnterScope(MortarScope scope) {
        super.onEnterScope(scope);
        bus.register(this);
        mainActivity.setVisibleBottombar(View.GONE);
        App.getInstance().setPrevious();
        App.getInstance().currentPresenter = NAME;
    }

    @Override
    protected void onExitScope() {
        super.onExitScope();
        bus.unregister(this);
        if(!GameService.getInstance().fromGames(App.getInstance().previousPresenter)){
            mainActivity.setVisibleBottombar(View.VISIBLE);
        }
        if (NAME.equals(App.getInstance().currentPresenter)){
            App.getInstance().currentPresenter = "";
        }
    }


    @Subscribe
    public void favoriteResult(FavoriteEvent event){
        UIHelper.getInstance().dismissProgressDialog();
        if ("success".equals(event.status) && event.items.size() > 0){
            getView().noResult.setVisibility(View.INVISIBLE);
            getView().listView.setVisibility(View.VISIBLE);
            adapter.setItems(event.items);
        } else {
            getView().noResult.setVisibility(View.VISIBLE);
            getView().listView.setVisibility(View.INVISIBLE);
        }
    }

    @Subscribe
    public void unregisterInternetReceiver(UnregisterFavoriteEvent event){
        if(event.toUnregister) {
            App.getInstance().stopNetworkMonitoringReceiver();
        }
        else{
            App.getInstance().startNetworkMonitoringReceiver();
        }
    }

    @Subscribe
    public void updateNetwork(UpdateNetworkEvent event){
        if(event.isConnected){
            if(isFirstTime) {
                FavoriteService.getInstance().getFavoriteList();
                isFirstTime = false;
            }
            getView().noResult.setVisibility(View.GONE);
            getView().listView.setVisibility(View.VISIBLE);
        }
        else{
            getView().noResult.setVisibility(View.VISIBLE);
            getView().listView.setVisibility(View.GONE);
        }
    }
}

