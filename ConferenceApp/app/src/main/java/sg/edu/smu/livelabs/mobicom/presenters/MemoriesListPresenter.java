package sg.edu.smu.livelabs.mobicom.presenters;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;

import com.marshalchen.ultimaterecyclerview.UltimateRecyclerView;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import sg.edu.smu.livelabs.mobicom.adapters.MemoriesListViewAdapter;
import sg.edu.smu.livelabs.mobicom.busEvents.BadgeNotiEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.MemoriesHomeEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.MemoriesListEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.MemoriesResetHomeEvent;
import sg.edu.smu.livelabs.mobicom.fileupload.UploadFileService;
import sg.edu.smu.livelabs.mobicom.flow.Layout;
import sg.edu.smu.livelabs.mobicom.models.User;
import sg.edu.smu.livelabs.mobicom.net.item.MemoriesItem;
//import sg.edu.smu.livelabs.mobicom.presenters.screen.MemoriesCameraScreen;
//import sg.edu.smu.livelabs.mobicom.presenters.screen.MemoriesFullScreenScreen;
import sg.edu.smu.livelabs.mobicom.presenters.screen.MemoriesCameraScreen;
import sg.edu.smu.livelabs.mobicom.presenters.screen.MemoriesFullScreenScreen;
import sg.edu.smu.livelabs.mobicom.services.DatabaseService;
import sg.edu.smu.livelabs.mobicom.services.MasterPointService;
import sg.edu.smu.livelabs.mobicom.services.MemoriesService;
import sg.edu.smu.livelabs.mobicom.services.ScreenService;
import sg.edu.smu.livelabs.mobicom.views.MemoriesListView;

/**
 * Created by smu on 3/11/15.
 */
@AutoScreen(
        component = @AutoComponent(dependencies = MemoriesMainPresenter.class, superinterfaces = AppDependencies.class),
        screenAnnotations = Layout.class
)
@DaggerScope(MemoriesListPresenter.class)
@Layout(R.layout.memories_list_view)
public class MemoriesListPresenter extends ViewPresenter<MemoriesListView> {
    public static int previousPage;
    private MemoriesListViewAdapter adapter;
    private Context context;
    private Bus bus;
    private ScreenService screenService;
    private boolean isProfilePhoto;
    private boolean isLoadMore;
    private int deletingItemIndex;
    private MainActivity mainActivity;
    private String imagesId ="0";
    private SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy");

    public MemoriesListPresenter(Bus bus, @ScreenParam ScreenService screenService, @ScreenParam MainActivity mainActivity){
        this.bus = bus;
        this.screenService = screenService;
        this.mainActivity = mainActivity;

    }
    @Override
    protected void onLoad(Bundle savedInstanceState) {
        super.onLoad(savedInstanceState);
        if (!hasView()) return;

        getView().dateTitle.setText(sdf.format(MemoriesService.getInstance().currentSelectedDate));

        context = getView().getContext();
        adapter = new MemoriesListViewAdapter(context, this, getView().ultimateRecyclerView);
        adapter.setData(new ArrayList<MemoriesItem>());
        UltimateRecyclerView ultimateRecyclerView = getView().ultimateRecyclerView;
        ultimateRecyclerView.setHasFixedSize(false);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        ultimateRecyclerView.setLayoutManager(linearLayoutManager);
        ultimateRecyclerView.setAdapter(adapter);
        ultimateRecyclerView.setEmptyView(R.layout.empty_view, R.id.no_image);
//        StickyRecyclerHeadersDecoration headersDecor = new StickyRecyclerHeadersDecoration(adapter);

//        ultimateRecyclerView.addItemDecoration(headersDecor);

        ultimateRecyclerView.reenableLoadmore();
        adapter.setCustomLoadMoreView(LayoutInflater.from(context)
                .inflate(R.layout.custom_bottom_progressbar, null));
        ultimateRecyclerView.setOnLoadMoreListener(new UltimateRecyclerView.OnLoadMoreListener() {
            @Override
            public void loadMore(int itemsCount, int maxLastVisiblePosition) {
                if (adapter.getAdapterItemCount() > 1) {
                    MemoriesItem selfie = adapter.getLastItem();
                    if (selfie != null) {
                        Date date = selfie.insertTime;
                        MemoriesService.getInstance().getMorePhoto(date, false, imagesId);
                    }
                }

            }
        });

        ultimateRecyclerView.setDefaultOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                System.out.println("List view refreshing " + new Date());
                MemoriesService.getInstance().getMorePhoto(MemoriesService.getInstance().getLastUpdateTime(), false, imagesId);
            }
        });


        getView().uploadPhotoRL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UploadFileService.getInstance().requestPhoto(mainActivity, new Action1<String>() {
                    @Override
                    public void call(String filepath) {
                        MemoriesItem image = new MemoriesItem();
                        image.image = filepath;
                        image.id = null;
                        Flow.get(context).set(new MemoriesCameraScreen(image));
                    }
                });
            }
        });
        mainActivity.setVisibleBottombar(View.GONE);
    }

    @Override
    protected void onEnterScope(MortarScope scope) {
        super.onEnterScope(scope);
        bus.register(this);
        mainActivity.setVisibleBottombar(View.GONE);
    }

    @Override
    protected void onExitScope() {
        super.onExitScope();
        bus.unregister(this);
    }

    @Subscribe
    public void goToSelfieListView(MemoriesListEvent event){
        if (event.hasExecuted) return;
        event.hasExecuted = true;
        isProfilePhoto = event.isProfilePhoto;
        isLoadMore = event.isLoadMore;
        if (!isLoadMore){
            getView().ultimateRecyclerView.disableLoadmore();
//            getView().ultimateRecyclerView.setDefaultOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
//                @Override
//                public void onRefresh() {
//                    getView().ultimateRecyclerView.setRefreshing(false);
//                }
//            });

            getView().ultimateRecyclerView.setRefreshing(false);
        }
        previousPage = event.previousPage;
        adapter.setData(event.images);
        imagesId = MemoriesService.getInstance().getEventIds(adapter.getAllItems());
        getView().ultimateRecyclerView.scrollVerticallyToPosition(event.current);
    }

    @Subscribe
    public void setRecentPhotos(MemoriesHomeEvent event){
        if(event != null){
            if(!event.canUplaod)
                getView().uploadPhotoRL.setVisibility(View.GONE);
            else
                getView().uploadPhotoRL.setVisibility(View.VISIBLE);
        }

        getView().ultimateRecyclerView.setRefreshing(false);
        if (event.images != null){
            if (event.isNext){
                isLoadMore = true;
                getView().ultimateRecyclerView.reenableLoadmore();
            } else{
                isLoadMore = false;
                getView().ultimateRecyclerView.disableLoadmore();
                adapter.notifyDataSetChanged();
            }
            if (event.isFirst){
                adapter.setData(event.images);
            } else {
                User me = DatabaseService.getInstance().getMe();
                List<MemoriesItem> selfiesEvent = event.images;
                List<MemoriesItem> notMyPhoto = new ArrayList<>();
                if(isProfilePhoto){
                    for(MemoriesItem s: selfiesEvent){
//                        if(s.userId != me.getUID()){
                            notMyPhoto.add(s);
//                        }
                    }

                    selfiesEvent.removeAll(notMyPhoto);
                }

                adapter.insertData(selfiesEvent);
            }
        }
        imagesId = MemoriesService.getInstance().getEventIds(adapter.getAllItems());
        UIHelper.getInstance().dismissProgressDialog();
    }

    @Subscribe
    public void resetPhotos(MemoriesResetHomeEvent event){
        getView().dateTitle.setText(sdf.format(event.date));
    }

    public void goToFullScreen(int currentPosition) {
        cacheData(adapter.getAllItems(), currentPosition);
        Flow.get(context).set(new MemoriesFullScreenScreen(adapter.getAllItems(), currentPosition, isProfilePhoto));
    }

    private void cacheData(List<MemoriesItem> images, int current){
        MemoriesListEvent event = new MemoriesListEvent();
        event.previousPage = previousPage;
        event.images = new ArrayList<>(images);
        event.current = current;
        event.isProfilePhoto = isProfilePhoto;
        event.isLoadMore = isLoadMore;
        screenService.push(MemoriesListPresenter.class, event);
    }

    @Subscribe
    public void badgeNotiEvent(BadgeNotiEvent event){
        MasterPointService.getInstance().getBadgesAPI();
        MasterPointService.getInstance().showToolTips(getView().ultimateRecyclerView, event.badgeName);
    }
}
