package sg.edu.smu.livelabs.mobicom.presenters;

import android.content.Context;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import autodagger.AutoComponent;
import automortar.AutoScreen;
import flow.Flow;
import mortar.MortarScope;
import mortar.ViewPresenter;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import sg.edu.smu.livelabs.mobicom.ActionBarOwner;
import sg.edu.smu.livelabs.mobicom.App;
import sg.edu.smu.livelabs.mobicom.AppDependencies;
import sg.edu.smu.livelabs.mobicom.DaggerScope;
import sg.edu.smu.livelabs.mobicom.MainActivity;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.UIHelper;
import sg.edu.smu.livelabs.mobicom.adapters.StumpListAdapter;
import sg.edu.smu.livelabs.mobicom.busEvents.UpdateNetworkEvent;
import sg.edu.smu.livelabs.mobicom.flow.Layout;
import sg.edu.smu.livelabs.mobicom.models.User;
import sg.edu.smu.livelabs.mobicom.models.data.GameListEntity;
import sg.edu.smu.livelabs.mobicom.net.RestClient;
import sg.edu.smu.livelabs.mobicom.net.item.StumpItem;
import sg.edu.smu.livelabs.mobicom.net.response.StumpResponse;
import sg.edu.smu.livelabs.mobicom.presenters.screen.StumpScreen;
import sg.edu.smu.livelabs.mobicom.services.DatabaseService;
import sg.edu.smu.livelabs.mobicom.services.GameService;
import sg.edu.smu.livelabs.mobicom.services.TrackingService;
import sg.edu.smu.livelabs.mobicom.views.StumpListView;

/**
 * Created by smu on 22/2/16.
 */
@AutoScreen(
        component = @AutoComponent(dependencies = MainActivity.class, superinterfaces = AppDependencies.class),
        screenAnnotations = Layout.class
)
@DaggerScope(StumpListPresenter.class)
@Layout(R.layout.stump_list_view)
public class StumpListPresenter extends ViewPresenter<StumpListView>{

        private ActionBarOwner actionBarOwner;
        private final RestClient restClient;
        private Bus bus;
        public static String NAME = "StumpListPresenter";

        private User me;
        private  ConnectivityManager cm;
        private MainActivity mainActivity;
        private boolean isFirstTime;

        private int screenWidth;
        private int screenHeight;
        private StumpListAdapter stumpListAdapter;


        /**
         *
         * @param restClient
         * @param bus
         * @param actionBarOwner
         */
        public StumpListPresenter(RestClient restClient, Bus bus, ActionBarOwner actionBarOwner, MainActivity mainActivity) {
                this.restClient = restClient;
                this.bus = bus;
                this.actionBarOwner = actionBarOwner;
                this.mainActivity = mainActivity;
                this.isFirstTime = true;
        }

        @Override
        protected void onLoad(Bundle savedInstanceState) {
                super.onLoad(savedInstanceState);
                Log.d(App.APP_TAG, " StumpListPresenter onload");

                GameListEntity game = GameService.getInstance().getGameByKeyword("stump");
                if(game != null &&  !game.getGameName().isEmpty()){
                        actionBarOwner.setConfig(new ActionBarOwner.Config(true, game.getGameName(), null));
                }
                else{
                        actionBarOwner.setConfig(new ActionBarOwner.Config(true, "Mobi Quest", null));
                }

                me = DatabaseService.getInstance().getMe();

                if(game != null) {
                        if (game.getDescription() != null && !game.getDescription().isEmpty())
                                getView().description.setText(game.getDescription());
                }

                cm = (ConnectivityManager) getView().getContext().getSystemService(Context.CONNECTIVITY_SERVICE);

                WindowManager wm = (WindowManager) getView().getContext().getSystemService(Context.WINDOW_SERVICE);
                Display display = wm.getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);
                screenWidth = size.x;
                screenHeight = size.y;

                if(cm.getActiveNetworkInfo() != null) {
                        loadStumpList();
                        isFirstTime = false;
                }
                else{
                        getView().noResultTV.setVisibility(View.VISIBLE);
                        getView().stumpLV.setVisibility(View.GONE);
                }

//                //Mimimum acceptable free memory you think your app needs
//                long minRunningMemory = 20000000; //20MB
//
//                Runtime runtime = Runtime.getRuntime();
//                if(runtime.freeMemory()<minRunningMemory)
//                        System.gc();
        }


        @Override
        protected void onEnterScope(MortarScope scope) {
                super.onEnterScope(scope);
                bus.register(this);
                App.getInstance().setPrevious();
                App.getInstance().currentPresenter = NAME;
                mainActivity.setVisibleBottombar(View.GONE);
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

        public void loadStumpList(){
                UIHelper.getInstance().showProgressDialog(getView().getContext(), "Loading Stump...", false);
                final Handler mainHandler = new Handler(getView().getContext().getMainLooper());
                restClient.getStumpApi().getStumps(Long.toString(me.getUID()))
                        .subscribeOn(Schedulers.io())
                        .subscribe(new Action1<StumpResponse>() {
                                @Override
                                public void call(final StumpResponse response) {

                                        if(response.status.equals("success")) {
                                                mainHandler.post(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                                if(response.details != null && response.details.size() > 0) {
                                                                        stumpListAdapter = new StumpListAdapter(getView().getContext(), response.details);
                                                                        getView().stumpLV.setAdapter(stumpListAdapter);
                                                                }

                                                                getView().stumpLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                                                        @Override
                                                                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                                                                StumpItem stump = stumpListAdapter.getItem(position);
                                                                                TrackingService.getInstance().sendTracking("424", "games",
                                                                                        "stump", stump.id, "", "");
                                                                                Flow.get(getView().getContext()).set(new StumpScreen(stump));
                                                                        }
                                                                });


                                                                UIHelper.getInstance().dismissProgressDialog();
                                                        }
                                                });
                                        }
                                }
                        }, new Action1<Throwable>() {
                                @Override
                                public void call(Throwable throwable) {
                                        Log.e("Mobisys: ", "cannot get stump list", throwable);

                                        mainHandler.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                        UIHelper.getInstance().dismissProgressDialog();
                                                }
                                        });
                                }
                        });
        }


        @Subscribe
        public void updateNetwork(UpdateNetworkEvent event) {
                if (event.isConnected) {
                        if(isFirstTime) {
                                loadStumpList();
                                isFirstTime = false;
                        }

                        getView().noResultTV.setVisibility(View.GONE);
                        getView().stumpLV.setVisibility(View.VISIBLE);
                }
                else{
                        getView().noResultTV.setVisibility(View.VISIBLE);
                        getView().stumpLV.setVisibility(View.GONE);
                }
        }

}
