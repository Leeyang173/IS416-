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

import java.util.ArrayList;
import java.util.List;

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
import sg.edu.smu.livelabs.mobicom.adapters.VotingListAdapter;
import sg.edu.smu.livelabs.mobicom.busEvents.UnregisterVotingEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.UpdateNetworkEvent;
import sg.edu.smu.livelabs.mobicom.flow.Layout;
import sg.edu.smu.livelabs.mobicom.models.User;
import sg.edu.smu.livelabs.mobicom.models.data.GameListEntity;
import sg.edu.smu.livelabs.mobicom.net.RestClient;
import sg.edu.smu.livelabs.mobicom.net.item.PollingItem;
import sg.edu.smu.livelabs.mobicom.net.response.PollingGetDetailsResponse;
//import sg.edu.smu.livelabs.mobicom.presenters.screen.VotingScreen;
import sg.edu.smu.livelabs.mobicom.presenters.screen.VotingScreen;
import sg.edu.smu.livelabs.mobicom.services.DatabaseService;
import sg.edu.smu.livelabs.mobicom.services.GameService;
import sg.edu.smu.livelabs.mobicom.services.TrackingService;
import sg.edu.smu.livelabs.mobicom.views.VotingListView;

/**
 * Created by smu on 22/2/16.
 */
@AutoScreen(
        component = @AutoComponent(dependencies = MainActivity.class, superinterfaces = AppDependencies.class),
        screenAnnotations = Layout.class
)
@DaggerScope(VotingListPresenter.class)
@Layout(R.layout.voting_client_list_view)
public class VotingListPresenter extends ViewPresenter<VotingListView>{

        private final RestClient restClient;
        private ActionBarOwner actionBarOwner;
        private Bus bus;
        private MainActivity mainActivity;
        public static final String NAME = "VotingListPresenter";

        private VotingListAdapter votingListAdapter;
        private int screenWidth;
        private int screenHeight;
        private  ConnectivityManager cm;
        private User me;
        private List<PollingItem> pollingItems;
        private boolean isFirstTIme = true;

        public VotingListPresenter(RestClient restClient, Bus bus, ActionBarOwner actionBarOwner, MainActivity mainActivity) {
                this.restClient = restClient;
                this.bus = bus;
                this.mainActivity =mainActivity;
                this.actionBarOwner = actionBarOwner;
                this.pollingItems = new ArrayList<>();
        }

        @Override
        protected void onLoad(Bundle savedInstanceState) {
                super.onLoad(savedInstanceState);
                Log.d(App.APP_TAG, " VotingListPresenter onload");

                GameListEntity game = GameService.getInstance().getGameByKeyword("polling");
                if(game != null &&  !game.getGameName().isEmpty()){
                        actionBarOwner.setConfig(new ActionBarOwner.Config(true, game.getGameName(), null));
                }
                else{
                        actionBarOwner.setConfig(new ActionBarOwner.Config(true, "Polling", null));
                }

                cm = (ConnectivityManager) getView().getContext().getSystemService(Context.CONNECTIVITY_SERVICE);

                mainActivity.setVisibleBottombar(View.GONE);

                me = DatabaseService.getInstance().getMe();

                WindowManager wm = (WindowManager) getView().getContext().getSystemService(Context.WINDOW_SERVICE);
                Display display = wm.getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);
                screenWidth = size.x;
                screenHeight = size.y;

                if(cm.getActiveNetworkInfo() != null) {
                        isFirstTIme = false;
                        loadCurrentPolling();
                }

                App.getInstance().startNetworkMonitoringReceiver();
        }

        @Override
        protected void onEnterScope(MortarScope scope) {
                super.onEnterScope(scope);
                bus.register(this);
                App.getInstance().setPrevious();
                App.getInstance().currentPresenter = NAME;

                if(App.getInstance().previousPresenter.equals(MorePresenter.NAME)){
                        App.getInstance().previousPresenter = "";
                }
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
        public void unregisterInternetReceiver(UnregisterVotingEvent event){
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
                        if(isFirstTIme) {
                                loadCurrentPolling();
                        }
                        getView().pollingList.setVisibility(View.VISIBLE);
                        getView().noInternetContainer.setVisibility(View.GONE);

                }
                else{
                        getView().pollingList.setVisibility(View.GONE);
                        getView().noInternetContainer.setVisibility(View.VISIBLE);
                        getView().internetTV.setVisibility(View.VISIBLE);
                        getView().msgTV.setVisibility(View.GONE);
                }
        }

        public void loadCurrentPolling(){
                final Handler mainHandler = new Handler(getView().getContext().getMainLooper());

                restClient.getPollingApi().getPollingList(Long.toString(me.getUID()))
                        .subscribeOn(Schedulers.io())
                        .subscribe(new Action1<PollingGetDetailsResponse>() {
                                @Override
                                public void call(final PollingGetDetailsResponse response) {
                                        if (response.status.equals("success")) {
                                                if (response.details != null && response.details.size() > 0) {

                                                        mainHandler.post(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                        if(getView() != null) {
                                                                                getView().pollingList.setVisibility(View.VISIBLE);
                                                                                getView().noInternetContainer.setVisibility(View.GONE);

                                                                                pollingItems.clear();
                                                                                pollingItems = response.details;
                                                                                votingListAdapter = new VotingListAdapter(getView().getContext(), pollingItems);
                                                                                getView().pollingList.setAdapter(votingListAdapter);

                                                                                getView().pollingList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                                                                        @Override
                                                                                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                                                                                                Flow.get(getView().getContext()).set(
                                                                                                        new VotingScreen(Long.parseLong(pollingItems.get(position).pollId),
                                                                                                                pollingItems.get(position).title));
                                                                                                TrackingService.getInstance().sendTracking("408", "games",
                                                                                                        "polling", pollingItems.get(position).pollId, "", "");
                                                                                        }
                                                                                });
                                                                        }
                                                                }
                                                        });
                                                }
                                                else{
                                                        mainHandler.post(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                        if(getView() != null) {
                                                                                getView().pollingList.setVisibility(View.GONE);
                                                                                getView().noInternetContainer.setVisibility(View.VISIBLE);
                                                                                getView().internetTV.setVisibility(View.GONE);
                                                                                getView().msgTV.setVisibility(View.VISIBLE);
                                                                        }
                                                                }
                                                        });
                                                }

                                        }
                                        else{
                                                mainHandler.post(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                                if (getView() != null) {
                                                                        getView().pollingList.setVisibility(View.GONE);
                                                                        getView().noInternetContainer.setVisibility(View.VISIBLE);
                                                                        getView().internetTV.setVisibility(View.GONE);
                                                                        getView().msgTV.setVisibility(View.VISIBLE);
                                                                }
                                                        }
                                                });
                                        }
                                }
                        }, new Action1<Throwable>() {
                                @Override
                                public void call(Throwable throwable) {
                                        Log.e("Mobisys: ", "cannot get friends", throwable);
                                }
                        });
        }
}
