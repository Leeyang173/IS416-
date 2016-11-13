package sg.edu.smu.livelabs.mobicom.presenters;

import android.os.Bundle;
import android.util.Log;
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
import sg.edu.smu.livelabs.mobicom.adapters.EVAPromotionAdapter;
import sg.edu.smu.livelabs.mobicom.flow.Layout;
import sg.edu.smu.livelabs.mobicom.models.data.GameListEntity;
import sg.edu.smu.livelabs.mobicom.net.response.EVAPromotionResponse;
import sg.edu.smu.livelabs.mobicom.services.EVAPromotionService;
import sg.edu.smu.livelabs.mobicom.services.GameService;
import sg.edu.smu.livelabs.mobicom.views.EVAPromotionView;

/**
 * Created by smu on 27/10/15.
 */
@AutoScreen(
        component = @AutoComponent(dependencies = MainActivity.class, superinterfaces = AppDependencies.class),
        screenAnnotations = Layout.class
)
@DaggerScope(EVAPromotionPresenter.class)
@Layout(R.layout.eva_promotion_view)
public class EVAPromotionPresenter extends ViewPresenter<EVAPromotionView>{

    public static String NAME = "EvaPromotionPresenter";
    private ActionBarOwner actionBarOwner;
    private Bus bus;
    private EVAPromotionAdapter adapter;
    private MainActivity activity;

    public EVAPromotionPresenter(Bus bus, ActionBarOwner actionBarOwner, MainActivity activity){
        this.bus = bus;
        this.actionBarOwner = actionBarOwner;
        this.activity = activity;
    }

    @Override
    protected void onLoad(Bundle savedInstanceState) {
        super.onLoad(savedInstanceState);

        //Mimimum acceptable free memory you think your app needs
        long minRunningMemory = 20000000; //20MB

        Log.d("AAA", "maxMemory : " + Runtime.getRuntime().maxMemory());
        Log.d("AAA", "totalMemory : " + Runtime.getRuntime().totalMemory());
        Log.d("AAA", "freeMemory : " + Runtime.getRuntime().freeMemory());
        Runtime runtime = Runtime.getRuntime();
        if(runtime.freeMemory()<minRunningMemory)
            System.gc();

        GameListEntity game = GameService.getInstance().getGameByKeyword("coolfie");
        if(game != null &&  !game.getGameName().isEmpty()){
            actionBarOwner.setConfig(new ActionBarOwner.Config(true, game.getGameName(), null));
        }
        else{
            actionBarOwner.setConfig(new ActionBarOwner.Config(true, "Memories", null));
        }

        if(game != null) {
            if (game.getDescription() != null && !game.getDescription().isEmpty())
                getView().descriptionTV.setText(game.getDescription());
        }

        adapter = new EVAPromotionAdapter(getView().getContext());
        getView().listView.setAdapter(adapter);
        UIHelper.getInstance().showProgressDialog(getView().getContext(), "Loading...", true);
        EVAPromotionService.getInstance().getEVAPromotions();
    }

    @Override
    protected void onEnterScope(MortarScope scope) {
        super.onEnterScope(scope);
        bus.register(this);
        App.getInstance().setPrevious();
        App.getInstance().currentPresenter = NAME;
        activity.setVisibleBottombar(View.GONE);
    }

    @Override
    protected void onExitScope() {
        super.onExitScope();
        bus.unregister(this);
        if(!GameService.getInstance().fromGames(App.getInstance().previousPresenter)){
            activity.setVisibleBottombar(View.VISIBLE);
        }
        if (NAME.equals(App.getInstance().currentPresenter)){
            App.getInstance().currentPresenter = "";
        }
    }

    @Subscribe
    public void promotionsResult(EVAPromotionResponse promotionResponse){
        UIHelper.getInstance().dismissProgressDialog();

        if (promotionResponse != null) {
            if ("success".equals(promotionResponse.status) && promotionResponse.promotions.size() > 0){

                getView().noResult.setVisibility(View.INVISIBLE);
                getView().listView.setVisibility(View.VISIBLE);
                getView().chooseItem.setVisibility(View.VISIBLE);
                adapter.setPromotions(promotionResponse.promotions);
            } else {
                getView().noResult.setVisibility(View.VISIBLE);
                getView().listView.setVisibility(View.INVISIBLE);
                getView().chooseItem.setVisibility(View.INVISIBLE);
            }
        }

    }
}

