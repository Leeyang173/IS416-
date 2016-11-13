package sg.edu.smu.livelabs.mobicom.presenters;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.GridView;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.List;

import autodagger.AutoComponent;
import automortar.AutoScreen;
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
import sg.edu.smu.livelabs.mobicom.adapters.IceBreakerFriendsListAdapter;
import sg.edu.smu.livelabs.mobicom.adapters.IceBreakerLeaderBoardGridAdapter;
import sg.edu.smu.livelabs.mobicom.busEvents.UnregisterIceBreakerEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.UpdateNetworkEvent;
import sg.edu.smu.livelabs.mobicom.flow.Layout;
import sg.edu.smu.livelabs.mobicom.models.User;
import sg.edu.smu.livelabs.mobicom.models.data.GameListEntity;
import sg.edu.smu.livelabs.mobicom.models.data.IceBreakerFriendsEntity;
import sg.edu.smu.livelabs.mobicom.models.data.IceBreakerLeaderBoardEntity;
import sg.edu.smu.livelabs.mobicom.net.RestClient;
import sg.edu.smu.livelabs.mobicom.net.response.IceBreakerGetFriendListResponse;
import sg.edu.smu.livelabs.mobicom.net.response.IceBreakerLeaderBoardResponse;
import sg.edu.smu.livelabs.mobicom.services.DatabaseService;
import sg.edu.smu.livelabs.mobicom.services.GameService;
import sg.edu.smu.livelabs.mobicom.services.IceBreakerService;
import sg.edu.smu.livelabs.mobicom.views.IceBreakerLeaderBoardView;

/**
 * Created by smu on 22/2/16.
 */
@AutoScreen(
        component = @AutoComponent(dependencies = MainActivity.class, superinterfaces = AppDependencies.class),
        screenAnnotations = Layout.class
)
@DaggerScope(IceBreakerLeaderBoardPresenter.class)
@Layout(R.layout.icebreaker_leaderboard_view)
public class IceBreakerLeaderBoardPresenter extends ViewPresenter<IceBreakerLeaderBoardView>{

        private final RestClient restClient;
        private ActionBarOwner actionBarOwner;
        private Bus bus;
        private MainActivity mainActivity;
        public static final String NAME = "IceBreakerLeaderBoardPresenter";

        private IceBreakerFriendsListAdapter iceBreakerFriendsListAdapter;
        private IceBreakerLeaderBoardGridAdapter iceBreakerLeaderBoardGridAdapter;

        private int screenWidth;
        private int screenHeight;
        private  ConnectivityManager cm;
        private User me;
        private List<IceBreakerFriendsEntity> friends;
        private List<IceBreakerLeaderBoardEntity> leaderBoard;
        private int loadLeaderboardTryAgain = 0;
        private int loadFriendTryAgain = 0;
        private boolean isFirstTime = true;

        public IceBreakerLeaderBoardPresenter(RestClient restClient, Bus bus, ActionBarOwner actionBarOwner, MainActivity mainActivity) {
                this.restClient = restClient;
                this.bus = bus;
                this.mainActivity =mainActivity;
                this.actionBarOwner = actionBarOwner;
        }

        @Override
        protected void onLoad(Bundle savedInstanceState) {
                super.onLoad(savedInstanceState);
                Log.d(App.APP_TAG, " IceBreakerLeaderBoardPresenter onload");

                GameListEntity game = GameService.getInstance().getGameByKeyword("ice breaker");
                if(game != null &&  !game.getGameName().isEmpty()){
                        actionBarOwner.setConfig(new ActionBarOwner.Config(true, game.getGameName(), null));
                }
                else{
                        actionBarOwner.setConfig(new ActionBarOwner.Config(true, "The Talent Firm", null));
                }

                WindowManager wm = (WindowManager) getView().getContext().getSystemService(Context.WINDOW_SERVICE);
                Display display = wm.getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);
                screenWidth = size.x;
                screenHeight = size.y;

                me = DatabaseService.getInstance().getMe();
                cm = (ConnectivityManager) getView().getContext().getSystemService(Context.CONNECTIVITY_SERVICE);

                if(cm.getActiveNetworkInfo() != null){
                        loadFriendList();
                        loadLeaderBoard();
                        isFirstTime = false;
                }
                else{ //this will load any existing data from the phone if there is no internet connection
                        friends = IceBreakerService.getInstance().getAllFriends();
                        leaderBoard = IceBreakerService.getInstance().getLeaderBoard();

                        if(friends != null && friends.size() > 0){ //if null, wait for the API to load
                                iceBreakerFriendsListAdapter = new IceBreakerFriendsListAdapter(getView(),
                                        friends, bus, screenWidth, screenHeight, mainActivity, cm);
                                getView().yourFriendsLV.setAdapter(iceBreakerFriendsListAdapter);
                                getView().yourFriendTV.setText("Your Friends: " + friends.size());

                                if(friends.size() == 0){
                                        getView().yourFriendsLV.setVisibility(View.GONE);
                                        getView().noFriendTV.setVisibility(View.VISIBLE);
                                }
                        }

                        if(leaderBoard != null && leaderBoard.size() > 0){
                                iceBreakerLeaderBoardGridAdapter = new IceBreakerLeaderBoardGridAdapter(getView().getContext(),
                                        leaderBoard, mainActivity, cm);

                                getView().leaderboardGV.setAdapter(iceBreakerLeaderBoardGridAdapter);

                                if(leaderBoard.size() == 0){
                                        getView().leaderboardGV.setVisibility(View.GONE);
                                        getView().noLeaderBoardTV.setVisibility(View.VISIBLE);
                                }
                        }

                }



                App.getInstance().startNetworkMonitoringReceiver();
        }


        @Override
        protected void onEnterScope(MortarScope scope) {
                super.onEnterScope(scope);
                bus.register(this);
                App.getInstance().currentPresenter = NAME;
                mainActivity.setVisibleBottombar(View.GONE);
        }

        @Override
        protected void onExitScope() {
                super.onExitScope();
                GameService.getInstance().closeDialog();
                bus.unregister(this);
                if (NAME.equals(App.getInstance().currentPresenter)){
                        App.getInstance().currentPresenter = "";
                }

        }

        @Subscribe
        public void unregisterInternetReceiver(UnregisterIceBreakerEvent event){
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
                                loadFriendList();
                                loadLeaderBoard();
                                isFirstTime = false;
                        }
                }
        }


        //this function call the server to get the list of friends that user had made from ice breaker
        private void loadFriendList(){
                final Handler mainHandler = new Handler(getView().getContext().getMainLooper());

                restClient.getIceBreakerApi().getFriendList(Long.toString(me.getUID()))
                        .subscribeOn(Schedulers.io())
                        .subscribe(new Action1<IceBreakerGetFriendListResponse>() {
                                @Override
                                public void call(final IceBreakerGetFriendListResponse iceBreakerGetFriendListResponse) {
                                        if (iceBreakerGetFriendListResponse.status.equals("success")) {
                                                if (iceBreakerGetFriendListResponse.friends != null) {
                                                        friends = IceBreakerService.getInstance().updateFriendList(iceBreakerGetFriendListResponse.friends); //update to DB for viewing when no internet

                                                        mainHandler.post(new Runnable() {
                                                                @Override
                                                                public void run() {
//                                                                        List<IceBreakerFriendsEntity> iceBreakerFriendsEntities = IceBreakerService.getInstance().getAllFriends();
                                                                        if(getView() != null){
                                                                                if (friends.size() > 0 ) {
                                                                                        if(getView().yourFriendsLV.getAdapter() == null) {
                                                                                                iceBreakerFriendsListAdapter = new IceBreakerFriendsListAdapter(getView(), friends, bus
                                                                                                        , screenWidth, screenHeight, mainActivity, cm);
                                                                                                getView().yourFriendsLV.setAdapter(iceBreakerFriendsListAdapter);
                                                                                                getView().yourFriendTV.setText("Your Friends: " + friends.size());
                                                                                        }
                                                                                        else{
                                                                                                iceBreakerFriendsListAdapter.updates(friends);
                                                                                        }
                                                                                }

                                                                                if(friends.size() == 0){
                                                                                        getView().yourFriendsLV.setVisibility(View.GONE);
                                                                                        getView().noFriendTV.setVisibility(View.VISIBLE);
                                                                                }

                                                                                SharedPreferences preferences = getView().getContext()
                                                                                        .getSharedPreferences("MOBICOM_ICE_BREAKER_TOTAL_USER", Context.MODE_PRIVATE);
                                                                                preferences
                                                                                        .edit()
                                                                                        .clear()
                                                                                        .putInt("total_users", iceBreakerGetFriendListResponse.total_users)
                                                                                        .commit();


                                                                        }
                                                                }
                                                        });
                                                }
                                        }
                                }
                        }, new Action1<Throwable>() {
                                @Override
                                public void call(Throwable throwable) {
                                        Log.e("Mobisys: ", "cannot get friends", throwable);
                                        if(loadFriendTryAgain < 3){ //try again

                                                try {
                                                        Handler handler = new Handler();
                                                        handler.postDelayed(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                        loadFriendTryAgain++;
                                                                        loadFriendList();
                                                                }
                                                        }, 1000);
                                                }
                                                catch (Throwable e){
                                                        if(getView() == null ) return;
                                                        UIHelper.getInstance().showAlert(getView().getContext(), "Seems like something is wrong, Please try again later.");
                                                        Log.d("AAA", "IceBreakerLeaderBoard:loadFriendList:"+e.toString());
                                                }
                                        }
                                }
                        });
        }


        private void loadLeaderBoard(){
                final Handler mainHandler = new Handler(getView().getContext().getMainLooper());

                restClient.getIceBreakerApi().getLeaderBoard(Long.toString(me.getUID()), "3")
                        .subscribeOn(Schedulers.io())
                        .subscribe(new Action1<IceBreakerLeaderBoardResponse>() {
                                @Override
                                public void call(final IceBreakerLeaderBoardResponse response) {
                                        if (response.status.equals("success")) {
                                                if (response.details != null) {
                                                        loadLeaderboardTryAgain = 0;
                                                        leaderBoard = IceBreakerService.getInstance().updateleaderBoard(response.details); //update to DB for viewing when no internet

                                                        mainHandler.post(new Runnable() { //TODO need to check whether will there be a lag that this will execute first
                                                                @Override
                                                                public void run() {
//                                                                        List<IceBreakerLeaderBoardEntity> leaderBoard = IceBreakerService.getInstance().getLeaderBoard();

                                                                        if(getView() != null && leaderBoard != null){
                                                                                if((iceBreakerLeaderBoardGridAdapter == null || getView().leaderboardGV.getAdapter() == null) && leaderBoard.size() > 0) {
                                                                                        iceBreakerLeaderBoardGridAdapter = new IceBreakerLeaderBoardGridAdapter(getView().getContext(),
                                                                                                leaderBoard, mainActivity, cm);

                                                                                        getView().leaderboardGV.setAdapter(iceBreakerLeaderBoardGridAdapter);
                                                                                }
                                                                                else{

                                                                                        if(iceBreakerLeaderBoardGridAdapter != null && leaderBoard != null) {
                                                                                                if(iceBreakerLeaderBoardGridAdapter == null)
                                                                                                                System.out.println("iceBreakerLeaderBoardGridAdapter is null");

                                                                                                if(leaderBoard == null)
                                                                                                        System.out.println("leaderBoard is null");

                                                                                                iceBreakerLeaderBoardGridAdapter.updates(leaderBoard);
                                                                                        }
                                                                                        else{
                                                                                                if(leaderBoard.size() > 0) {
                                                                                                        iceBreakerLeaderBoardGridAdapter = new IceBreakerLeaderBoardGridAdapter(getView().getContext(),
                                                                                                                leaderBoard, mainActivity, cm);
                                                                                                        getView().leaderboardGV.setAdapter(iceBreakerLeaderBoardGridAdapter);
                                                                                                }
                                                                                        }
                                                                                }

                                                                                if(leaderBoard.size() == 0){
                                                                                        getView().leaderboardGV.setVisibility(View.GONE);
                                                                                        getView().noLeaderBoardTV.setVisibility(View.VISIBLE);
                                                                                }
                                                                                else{
                                                                                        getView().leaderboardGV.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                                                                                                @Override
                                                                                                public void onGlobalLayout() {
                                                                                                        if(getView() != null) {
//                                                                                                                resizeGridView(getView().leaderboardGV, iceBreakerLeaderBoardGridAdapter.getCount(), 3);
                                                                                                        }
                                                                                                }
                                                                                        });
                                                                                }


                                                                        }
                                                                }
                                                        });
                                                }
                                        }
                                }
                        }, new Action1<Throwable>() {
                                @Override
                                public void call(Throwable throwable) {
                                        Log.e("Mobisys: ", "cannot get ice breaker leader board", throwable);
                                        if(loadLeaderboardTryAgain < 3){ //try again

                                                try{
                                                        Handler handler = new Handler();
                                                        handler.postDelayed(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                        loadLeaderboardTryAgain++;
                                                                        loadLeaderBoard();
                                                                }
                                                        }, 1000);
                                                }
                                                catch (Throwable e){
                                                        Log.d("AAA", "IceBreakerLeaderBoard:loadLeaderBoard:"+e.toString());
                                                        if(getView() == null) return;
                                                        UIHelper.getInstance().showAlert(getView().getContext(), "Seems like something is wrong, Please try again later.");

                                                }

                                        }
                                }
                        });
        }

        private void resizeGridView(GridView gridView, int items, int columns) {
                if(gridView != null) {
                        ViewGroup.LayoutParams params = gridView.getLayoutParams();
                        int maxHeight = 0;
                        for (int i = 0; i < items; i++) {
                                if (gridView.getChildAt(i) != null && maxHeight < gridView.getChildAt(i).getHeight()) {
                                        maxHeight = gridView.getChildAt(i).getHeight();
                                }
                        }

                        int oneRowHeight = gridView.getHeight();
                        int rows = (int) (items / columns);
                        params.height = (maxHeight * rows) + 30;
                        gridView.setLayoutParams(params);
                }
        }

}
