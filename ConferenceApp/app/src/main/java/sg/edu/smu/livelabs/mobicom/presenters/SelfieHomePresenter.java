package sg.edu.smu.livelabs.mobicom.presenters;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import com.marshalchen.ultimaterecyclerview.UltimateRecyclerView;
import com.marshalchen.ultimaterecyclerview.stickyheadersrecyclerview.StickyRecyclerHeadersDecoration;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import autodagger.AutoComponent;
import automortar.AutoScreen;
import automortar.ScreenParam;
import flow.Flow;
import mortar.MortarScope;
import mortar.ViewPresenter;
import rx.functions.Action1;
import sg.edu.smu.livelabs.mobicom.AppDependencies;
import sg.edu.smu.livelabs.mobicom.DaggerScope;
import sg.edu.smu.livelabs.mobicom.MainActivity;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.UIHelper;
import sg.edu.smu.livelabs.mobicom.adapters.SelfieHomeAdapter;
import sg.edu.smu.livelabs.mobicom.busEvents.BadgeNotiEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.SelfieHomeEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.SelfieListEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.SelfieStatusEvent;
import sg.edu.smu.livelabs.mobicom.fileupload.UploadFileService;
import sg.edu.smu.livelabs.mobicom.flow.Layout;
import sg.edu.smu.livelabs.mobicom.net.RestClient;
import sg.edu.smu.livelabs.mobicom.net.item.Selfie;
import sg.edu.smu.livelabs.mobicom.net.response.SelfieLeaderboardResponse;
import sg.edu.smu.livelabs.mobicom.presenters.screen.SelfieCameraScreen;
import sg.edu.smu.livelabs.mobicom.services.EVAPromotionService;
import sg.edu.smu.livelabs.mobicom.services.MasterPointService;
import sg.edu.smu.livelabs.mobicom.views.SelfieHomeView;
import sg.edu.smu.livelabs.mobicom.views.SelfieLeaderBoardView;

/**
 * Created by smu on 26/10/15.
 */
@AutoScreen(
        component = @AutoComponent(dependencies = SelfiePresenter.class, superinterfaces = AppDependencies.class),
        screenAnnotations = Layout.class
)
@DaggerScope(SelfieHomePresenter.class)
@Layout(R.layout.selfie_home_view)
public class SelfieHomePresenter extends ViewPresenter<SelfieHomeView> implements SelfieHomeAdapter.SelfieHomeListener {
    private Bus bus;
    private SelfieHomeAdapter adapter;
    private Context context;
    private SelfieLeaderBoardView leaderBoard;
    private UltimateRecyclerView ultimateRecyclerView;
    private List<Selfie> selfies;
    private List<Selfie> tops;
    private boolean isLoadMore;
    private MainActivity mainActivity;
    private RestClient restClient;


    public SelfieHomePresenter(RestClient restClient, Bus bus, @ScreenParam MainActivity mainActivity){
        this.bus = bus;
        selfies = new ArrayList<>();
        this.mainActivity = mainActivity;
        this.restClient = restClient;
    }

    @Override
    protected void onEnterScope(MortarScope scope) {
        super.onEnterScope(scope);
        bus.register(this);
    }

    @Override
    protected void onExitScope() {
        super.onExitScope();
        bus.unregister(this);
    }

    @Override
    protected void onLoad(Bundle savedInstanceState) {
        super.onLoad(savedInstanceState);
        if (!hasView()){
            return;
        }
        this.context = getView().getContext();
        adapter = new SelfieHomeAdapter(context, this);
        ultimateRecyclerView = getView().imageList;

        View leaderboardView = LayoutInflater.from(context).inflate(R.layout.selfie_leaderboard, null);
        ultimateRecyclerView.setNormalHeader(leaderboardView);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        ultimateRecyclerView.setLayoutManager(linearLayoutManager);
        ultimateRecyclerView.setAdapter(adapter);
        ultimateRecyclerView.setEmptyView(R.layout.empty_view, R.id.no_image);
        StickyRecyclerHeadersDecoration headersDecor = new StickyRecyclerHeadersDecoration(adapter);
        ultimateRecyclerView.addItemDecoration(headersDecor);

        ultimateRecyclerView.reenableLoadmore();
        adapter.setCustomLoadMoreView(LayoutInflater.from(context)
                .inflate(R.layout.custom_bottom_progressbar, null));
        ultimateRecyclerView.setOnLoadMoreListener(new UltimateRecyclerView.OnLoadMoreListener() {
            @Override
            public void loadMore(int itemsCount, int maxLastVisiblePosition) {

                if (selfies.size() > 1) {
                    Selfie selfie = selfies.get(selfies.size() - 1);
                    Date date = selfie.createdTime;
                    EVAPromotionService.getInstance().getMorePhoto(date, false);
                }

            }
        });
        ultimateRecyclerView.setDefaultOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                System.out.println("refresh " + new Date());
                EVAPromotionService.getInstance().getMorePhoto(EVAPromotionService.getInstance().getLastUpdateTime(), false);
                EVAPromotionService.getInstance().getLeaderboard();
            }
        });

        ultimateRecyclerView.setHasFixedSize(false);
        leaderBoard = new SelfieLeaderBoardView(leaderboardView, context);
        leaderBoard.setListener(this);

        Resources r = context.getResources();
        final int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 360, r.getDisplayMetrics());
        leaderBoard.view.setLayoutParams(new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                px));
        ultimateRecyclerView.setOnParallaxScroll(new UltimateRecyclerView.OnParallaxScroll() {
            @Override
            public void onParallaxScroll(float percentage, float offset, View parallax) {

            }
        });


        ultimateRecyclerView.setRecylerViewBackgroundColor(getView().getResources().getColor(R.color.white));
//        Date date = new Date();
        Calendar dateCal = Calendar.getInstance();
        Date date = new Date(dateCal.getTime().getTime() + 10000);

        UIHelper.getInstance().showProgressDialog(context, "Loading...", false);
        EVAPromotionService.getInstance().isAPINull(restClient);
        EVAPromotionService.getInstance().getMorePhoto(date, true);
        EVAPromotionService.getInstance().getLeaderboard();

        getView().uploadPhotoRL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UploadFileService.getInstance().requestPhoto(mainActivity, new Action1<String>() {
                    @Override
                    public void call(String filepath) {
                        Selfie selfie = new Selfie();
                        selfie.imageId = filepath;
                        selfie.id = null;
                        Flow.get(context).set(new SelfieCameraScreen(selfie));

                    }
                });
            }
        });
    }

    @Override
    public void likeClick(Selfie selfie, boolean isTopPhoto) {
        UIHelper.getInstance().showProgressDialog(context, "Processing...", false);
        if ("yes".equals(selfie.likeStatus)){
//            EVAPromotionService.getInstance().unlike(selfie, isTopPhoto);
        } else {
//            EVAPromotionService.getInstance().like(selfie, isTopPhoto);
        }
    }

    @Override
    public void openListView(int currentselfie) {
        SelfieListEvent event = new SelfieListEvent();
        event.previousPage = SelfiePresenter.HOME_TAB;
        event.current = currentselfie;
        event.selfies = selfies;
        event.isProfilePhoto = false;
        event.isLoadMore = isLoadMore;
        bus.post(event);
        if (currentselfie <= selfies.size()){
            Selfie selfie = selfies.get(currentselfie);
            String cmpId = EVAPromotionService.getInstance().currentPromotion.id;
//            EVAPromotionService.getInstance().selfieTracking("604", cmpId, selfie.id, String.valueOf(selfie.userId));
        }
    }

    public void openTopListView(int currentselfie) {
        if (tops == null || tops.size() == 0) return;
        SelfieListEvent event = new SelfieListEvent();
        event.previousPage = SelfiePresenter.HOME_TAB;
        event.current = currentselfie;
        event.selfies = tops;

        //this part need to combine the whole selfie list with the top selfie, but we can't have duplicate, so we need to remove those
        List<Selfie> tmp2 = new ArrayList<>();
        List<Selfie> thingsToBeRemove = new ArrayList<Selfie>();
        tmp2.addAll(selfies);
        for(Selfie t: tops) {
            for(Selfie s: tmp2){
                if (s.id.equals(t.id)) {
                    thingsToBeRemove.add(s);
//                    tmp2.remove(s);
                }
            }
        }

        tmp2.removeAll(thingsToBeRemove);

        event.selfies.addAll(tmp2);
        event.isProfilePhoto = false;
        event.isLoadMore = isLoadMore;//false;
        bus.post(event);
        if (currentselfie <= tops.size()){
            Selfie selfie = tops.get(currentselfie);
            String cmpId = EVAPromotionService.getInstance().currentPromotion.id;
//            EVAPromotionService.getInstance().selfieTracking("604", cmpId, selfie.id, String.valueOf(selfie.userId));
        }
    }

    @Subscribe
    public void setRecentPhotos(SelfieHomeEvent event){
        if (event.selfies != null && event.selfies.size() > 0){
            isLoadMore = event.isNext;
            if (!event.isNext){
                ultimateRecyclerView.disableLoadmore();
                adapter.notifyDataSetChanged();
            } else {
                ultimateRecyclerView.reenableLoadmore();
            }
            if (event.isFirst){
                this.selfies.clear();
                this.selfies.addAll(event.selfies);
                adapter.setData(event.selfies);
            } else {
                List<Selfie> sTmp = new ArrayList<>();
                for(Selfie s: event.selfies){
                    boolean isExist = false;
                    int position = 0;
                    for(Selfie selfie: this.selfies){
                        if(selfie.id.equals(s.id)){
                            isExist = true;
                            break;
                        }
                        position++;
                    }

                    if(!isExist) {
//                        this.selfies.add(0, s); //add to the front
                    }
                    else{
                        this.selfies.remove(position);
                        this.selfies.add(position, s);
                        sTmp.add(s);
                    }
                }

                event.selfies.removeAll(sTmp); //remove those that are already present
                this.selfies.addAll(event.selfies) ;
                adapter.insertData(event.selfies);
//
//
//                List<Selfie> selfiesToRemove = new ArrayList<>();
//                for(DeletedSelfie d: event.selfiesToRemove){
//                    for(Selfie s: this.selfies){
//                        if(s.id.equals(d.id)){
//                            selfiesToRemove.add(s);
//                            break;
//                        }
//                    }
//                }
//                this.selfies.removeAll(selfiesToRemove);
//                event.selfies.removeAll(sTmp); //remove those that are already present
////                this.selfies.addAll(event.selfies) ;
//                if(event.selfiesToRemove.size() > 0) {
//                    adapter.reset();
//                    adapter.setData(this.selfies);
//                }
//                else{
//                    adapter.insertData(event.selfies);
//                }
            }
        } else {
            if (selfies == null || selfies.isEmpty()){
                ultimateRecyclerView.disableLoadmore();
                adapter.notifyDataSetChanged();
                ultimateRecyclerView.setVisibility(View.INVISIBLE);
                getView().noImage.setVisibility(View.VISIBLE);
                getView().noImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        UploadFileService.getInstance().requestPhoto(mainActivity, new Action1<String>() {
                            @Override
                            public void call(String filepath) {
                                Selfie selfie = new Selfie();
                                selfie.imageId = filepath;
                                selfie.id = null;
//                                Flow.get(context).set(new SelfieCameraScreen(selfie));

                            }
                        });
                    }
                });
            }
        }
        UIHelper.getInstance().dismissProgressDialog();
    }

    @Subscribe
    public void setTopPhotos(SelfieLeaderboardResponse event){
        if("success".equals(event.status)){
            tops = event.details.get(0).selfies;
            if (tops == null || tops.size() == 0){
                final int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0, context.getResources().getDisplayMetrics());
                leaderBoard.view.setLayoutParams(new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT,
                        px));
            } else {
                if("active".equals(event.details.get(0).iconStatus)){
                    leaderBoard.setData(event.details.get(0), true);
                } else {
                    leaderBoard.setData(event.details.get(0), false);
                }
            }

        } else{
            final int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0, context.getResources().getDisplayMetrics());
            leaderBoard.view.setLayoutParams( new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    px));
        }

        ultimateRecyclerView.refreshDrawableState();
        ultimateRecyclerView.setRefreshing(false);
        ultimateRecyclerView.scrollVerticallyToPosition(0);
        UIHelper.getInstance().dismissProgressDialog();
    }

    @Subscribe
    public void updateSelfieStatus(SelfieStatusEvent event){
        if (event.isSuccess){
            if (event.isTopPhoto){
                EVAPromotionService.getInstance().getLeaderboard();
            } else {
                adapter.notifyDataSetChanged();
                UIHelper.getInstance().dismissProgressDialog();
            }
        } else {
            UIHelper.getInstance().dismissProgressDialog();
            UIHelper.getInstance().showAlert(context, event.details);
        }

    }

    @Subscribe
    public void badgeNotiEvent(BadgeNotiEvent event){
        MasterPointService.getInstance().getBadgesAPI();
        MasterPointService.getInstance().showToolTips(getView().imageList, event.badgeName);
    }
}
