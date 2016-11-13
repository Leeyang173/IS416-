package sg.edu.smu.livelabs.mobicom.presenters;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import autodagger.AutoComponent;
import automortar.AutoScreen;
import automortar.ScreenParam;
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
import sg.edu.smu.livelabs.mobicom.adapters.FavoriteItemGridAdapter;
import sg.edu.smu.livelabs.mobicom.adapters.FavoriteTopAdapter;
import sg.edu.smu.livelabs.mobicom.busEvents.BadgeNotiEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.UnregisterFavoriteEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.UpdateNetworkEvent;
import sg.edu.smu.livelabs.mobicom.flow.Layout;
import sg.edu.smu.livelabs.mobicom.models.User;
import sg.edu.smu.livelabs.mobicom.net.RestClient;
import sg.edu.smu.livelabs.mobicom.net.item.FavoriteItem;
import sg.edu.smu.livelabs.mobicom.net.response.FavoriteDetailsResponse;
import sg.edu.smu.livelabs.mobicom.net.response.FavoriteLeaderboardResponse;
import sg.edu.smu.livelabs.mobicom.net.response.FavoriteVoteResponse;
import sg.edu.smu.livelabs.mobicom.services.DatabaseService;
import sg.edu.smu.livelabs.mobicom.services.GameService;
import sg.edu.smu.livelabs.mobicom.services.MasterPointService;
import sg.edu.smu.livelabs.mobicom.services.TrackingService;
import sg.edu.smu.livelabs.mobicom.views.FavoriteItemView;

/**
 * Created by smu on 26/10/15.
 */
@AutoScreen(
        component = @AutoComponent(dependencies = MainActivity.class, superinterfaces = AppDependencies.class),
        screenAnnotations = Layout.class
)
@DaggerScope(FavoriteItemPresenter.class)
@Layout(R.layout.favorite_item_view)
public class FavoriteItemPresenter extends ViewPresenter<FavoriteItemView>  {
    public static final String NAME = "FavoriteItemPresenter";
    private ActionBarOwner actionBarOwner;
    private Bus bus;
    private Context context;
    private MainActivity mainActivity;
    private List<FavoriteItem> favoriteItems;
    private List<FavoriteItem> favoriteTopItems;
    private User me;
    private FavoriteItemGridAdapter adapter;
    private RestClient restClient;

    private int screenWidth;
    private int screenHeigth;
    private int numberOfVOteLeft = 20;
    private Dialog dialog;

    private long id;
//    private FavoriteTopAdapter favoriteTopAdapter;
    private ConnectivityManager cm;
    private Boolean gridViewResized = false;
    private ActionBarOwner.Config config;
    private boolean isFirstTime = true;

    public MaterialDialog progressBar;

    public FavoriteItemPresenter(Bus bus, ActionBarOwner actionBarOwner, MainActivity mainActivity, RestClient restClient
            , @ScreenParam long id){
        this.bus = bus;
        this.actionBarOwner = actionBarOwner;
        this.mainActivity = mainActivity;
        favoriteItems = new ArrayList<>();
        favoriteTopItems = new ArrayList<>();
        this.restClient = restClient;
        this.id = id;

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
        if(dialog != null && dialog.isShowing()){
            dialog.dismiss();
        }

        bus.unregister(this);

        actionBarOwner.setConfig(config);

        if(!GameService.getInstance().fromGames(App.getInstance().previousPresenter)){
            mainActivity.setVisibleBottombar(View.VISIBLE);
        }
        if (NAME.equals(App.getInstance().currentPresenter)){
            App.getInstance().currentPresenter = "";
        }
    }

    @Override
    protected void onLoad(Bundle savedInstanceState) {
        super.onLoad(savedInstanceState);

        config = actionBarOwner.getConfig();

        progressBar = new MaterialDialog.Builder(getView().getContext())
                .title(getView().getContext().getString(R.string.app_name))
                .content("Loading...")
                .progress(true, 0)
                .cancelable(false)
                .show();


        WindowManager wm = (WindowManager) getView().getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screenWidth = size.x;
        screenHeigth = size.y;

        me = DatabaseService.getInstance().getMe();
        cm = (ConnectivityManager) getView().getContext().getSystemService(Context.CONNECTIVITY_SERVICE);



//        favoriteTopAdapter = new FavoriteTopAdapter(getView().getContext(), mainActivity);
        if(cm.getActiveNetworkInfo() != null) {
            getFavoriteDetails();
            isFirstTime = false;
//            getLeaderBoard();
            getView().noResult.setVisibility(View.GONE);
        }

        getView().titleTV.setText("Cast your votes here");

        getView().topIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog();
            }
        });

//        getView().containerLL.fullScroll(ScrollView.FOCUS_UP);

        App.getInstance().startNetworkMonitoringReceiver();
    }

    //on vote
    public interface onVoteClickListener {
        public abstract void onBtnClick(int position);
    }

    public View getViewByPosition(int pos, GridView gridView) {
        final int firstListItemPosition = gridView.getFirstVisiblePosition();
        final int lastListItemPosition = firstListItemPosition + gridView.getChildCount() - 1;

        if (pos < firstListItemPosition || pos > lastListItemPosition ) {
            return gridView.getAdapter().getView(pos, null, gridView);
        } else {
            final int childIndex = pos - firstListItemPosition;
            return gridView.getChildAt(childIndex);
        }
    }


    public void showDialog(){

        dialog = new Dialog(getView().getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_box_favorite_top);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        Button closeBtn = (Button) dialog.findViewById(R.id.closeBtn);
        ListView topList = (ListView) dialog.findViewById(R.id.top_list);
        TextView msgTV = (TextView) dialog.findViewById(R.id.message);

//        favoriteTopAdapter.setItems(favoriteTopItems);
//        topList.setAdapter(favoriteTopAdapter);

        getLeaderBoard(topList, msgTV);

        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    public void getFavoriteDetails() {
        final Handler mainHandler = new Handler(getView().getContext().getMainLooper());
        String userId = String.valueOf(DatabaseService.getInstance().getMe().getUID());
        restClient.getFavoriteApi().getFavoriteDetails(userId, Long.toString(id))
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<FavoriteDetailsResponse>() {
                    @Override
                    public void call(final FavoriteDetailsResponse response) {
                        if (response.status.equals("success")) {
                            if (response.items != null && response.items.itemList != null && response.items.itemList.size() > 0) {
                                mainHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (getView() != null) {
                                            actionBarOwner.setConfig(new ActionBarOwner.Config(true, response.items.comp_title, null));

                                            favoriteItems = response.items.itemList;
                                            Collections.sort(favoriteItems, new Comparator<FavoriteItem>() {
                                                public int compare(FavoriteItem p1, FavoriteItem p2) {
                                                    return p1.name.compareTo(p2.name);
                                                }
                                            });

                                            adapter = new FavoriteItemGridAdapter(getView(), favoriteItems, me, 30, screenWidth, screenHeigth
                                                    , response.items.getMyVotedCandidates(), new onVoteClickListener() {
                                                @Override
                                                public void onBtnClick(int position) {

                                                    View view = getViewByPosition(position, getView().itemGV);

                                                    FavoriteItemGridAdapter.ViewHolder v = ((FavoriteItemGridAdapter.ViewHolder) view.getTag());
                                                    LinearLayout container =  v.container;

//                                                    TextView likesTV = (TextView) view.findViewById(R.id.likes);
                                                    FavoriteItem item = adapter.getItem(position);
                                                    if (!item.isLiked) {
                                                        container.setBackgroundResource(R.drawable.round_rect_shape_light_orange);
                                                        v.favorite.isLiked = true;
                                                        FavoriteItem f = adapter.addLike(position, view);
//                                                        likesTV.setText("Votes: " + f.count);
                                                        vote(f.id);
                                                    } else {
                                                        container.setBackgroundResource(R.drawable.round_rect_shape);

                                                        FavoriteItem f = adapter.deductLike(position, view);
//                                                        likesTV.setText("Votes: " + f.count);
                                                        unvote(f.id);
//
                                                    }
                                                }
                                            });

                                            getView().itemGV.setAdapter(adapter);

                                        }

                                        progressBar.dismiss();
                                    }
                                });

                            }
                            else{
                                progressBar.dismiss();
                            }

                        }
                        else{
                            progressBar.dismiss();
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        progressBar.dismiss();
                        Log.e("Mobisys: ", "cannot get favorite list", throwable);
                    }
                });
    }

    public void vote(String candidateId){
        String userId = String.valueOf(DatabaseService.getInstance().getMe().getUID());

        restClient.getFavoriteApi().vote(userId, Long.toString(id), candidateId)
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<FavoriteVoteResponse>() {
                    @Override
                    public void call(final FavoriteVoteResponse response) {
                        if (response.status.equals("success")) {
                            if(getView() != null){
//                                MasterPointService.getInstance().addPoint(MasterPointService.getInstance().FAVORITE, getView().containerLL);
                            }

                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.e("Mobisys: ", "cannot vote favorite", throwable);
                    }
                });

        TrackingService.getInstance().sendTracking("422", "games",
                "fav_comp", Long.toString(id), candidateId, "vote");
    }

    public void unvote(String candidateId){
        String userId = String.valueOf(DatabaseService.getInstance().getMe().getUID());

        restClient.getFavoriteApi().unvote(userId, Long.toString(id), candidateId)
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<FavoriteVoteResponse>() {
                    @Override
                    public void call(final FavoriteVoteResponse response) {
                        if (response.status.equals("success")) {
//                            MasterPointService.getInstance().deductPoint(MasterPointService.getInstance().FAVORITE);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.e("Mobisys: ", "cannot unvote favorite", throwable);
                    }
                });

        TrackingService.getInstance().sendTracking("423", "games",
                "fav_comp", Long.toString(id), candidateId, "unvote");
    }

    public void getLeaderBoard(final ListView topList, final TextView msgTV){
        final Handler mainHandler = new Handler(getView().getContext().getMainLooper());
        String userId = String.valueOf(DatabaseService.getInstance().getMe().getUID());

        restClient.getFavoriteApi().getLeaderboard(userId, Long.toString(id))
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<FavoriteLeaderboardResponse>() {
                    @Override
                    public void call(final FavoriteLeaderboardResponse response) {
                        if (response.status.equals("success")) {
                            if (response.items != null && response.items.size() > 0) {
                                mainHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        favoriteTopItems.clear();
                                        favoriteTopItems = response.items;

                                        Collections.sort(favoriteTopItems, new Comparator<FavoriteItem>() {
                                            public int compare(FavoriteItem p1, FavoriteItem p2) {
                                                return ((Integer)p2.count).compareTo(p1.count);
                                            }
                                        });

//                                        if(favoriteTopAdapter != null){
//                                            favoriteTopAdapter.setItems(favoriteTopItems);
//                                        }
//                                        else{
                                        FavoriteTopAdapter favoriteTopAdapter = new FavoriteTopAdapter(getView().getContext(), mainActivity, favoriteTopItems);
                                        topList.setAdapter(favoriteTopAdapter);
                                        msgTV.setVisibility(View.GONE);
//                                        }
                                    }
                                });

                            }

                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.e("Mobisys: ", "cannot favorite leader board", throwable);
                    }
                });
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
                getFavoriteDetails();
                isFirstTime = false;
            }
//            getLeaderBoard();
            getView().noResult.setVisibility(View.GONE);
            getView().containerLL.setVisibility(View.VISIBLE);
        }
        else{
            getView().noResult.setVisibility(View.VISIBLE);
            getView().containerLL.setVisibility(View.GONE);
        }
    }

    @Subscribe
    public void badgeNotiEvent(BadgeNotiEvent event){
        MasterPointService.getInstance().getBadgesAPI();
        MasterPointService.getInstance().showToolTips(getView().containerLL, event.badgeName);
    }

    private void resizeGridView(GridView gridView, int items, int columns) {
        ViewGroup.LayoutParams params = gridView.getLayoutParams();
        int maxHeight = 0;
        for(int i=0; i <items; i++){
            if(maxHeight < gridView.getChildAt(i).getHeight()){
                maxHeight = gridView.getChildAt(i).getHeight();
//                System.out.println("Max height:??? " + maxHeight);
            }
        }

        AbsListView.LayoutParams layoutParams = new AbsListView.LayoutParams(AbsListView.LayoutParams.WRAP_CONTENT, maxHeight);
        for(int i=0; i <items; i++){
            gridView.getChildAt(i).setLayoutParams(layoutParams);
        }


        int oneRowHeight = gridView.getHeight();
        int rows = (int)Math.ceil((double)items / (double)columns) ;
        params.height = (maxHeight * rows) + (maxHeight);
//        System.out.println("Result ??? " + rows + " " + params.height );
        gridView.setLayoutParams(params);
    }
}
