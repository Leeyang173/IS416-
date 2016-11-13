package sg.edu.smu.livelabs.mobicom.presenters;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;

import com.marshalchen.ultimaterecyclerview.UltimateRecyclerView;
import com.marshalchen.ultimaterecyclerview.stickyheadersrecyclerview.StickyRecyclerHeadersDecoration;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.text.SimpleDateFormat;
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
import sg.edu.smu.livelabs.mobicom.adapters.MemoriesHomeAdapter;
import sg.edu.smu.livelabs.mobicom.busEvents.BadgeNotiEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.MemoriesHomeEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.MemoriesListEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.MemoriesResetHomeEvent;
import sg.edu.smu.livelabs.mobicom.fileupload.UploadFileService;
import sg.edu.smu.livelabs.mobicom.flow.Layout;
import sg.edu.smu.livelabs.mobicom.net.item.MemoriesItem;
//import sg.edu.smu.livelabs.mobicom.presenters.screen.MemoriesCameraScreen;
import sg.edu.smu.livelabs.mobicom.presenters.screen.MemoriesCameraScreen;
import sg.edu.smu.livelabs.mobicom.services.MasterPointService;
import sg.edu.smu.livelabs.mobicom.services.MemoriesService;
import sg.edu.smu.livelabs.mobicom.views.MemoriesHomeView;

/**
 * Created by smu on 26/10/15.
 */
@AutoScreen(
        component = @AutoComponent(dependencies = MemoriesMainPresenter.class, superinterfaces = AppDependencies.class),
        screenAnnotations = Layout.class
)
@DaggerScope(MemoriesHomePresenter.class)
@Layout(R.layout.memories_home_view)
public class MemoriesHomePresenter extends ViewPresenter<MemoriesHomeView> implements MemoriesHomeAdapter.MemoriesHomeListener {
    private Bus bus;
    private MemoriesHomeAdapter adapter;
    private Context context;
    private UltimateRecyclerView ultimateRecyclerView;
    private List<MemoriesItem> images;
    private boolean isLoadMore;
    private MainActivity mainActivity;
    private String imagesId = "0";
    private SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy");

    public MemoriesHomePresenter(Bus bus, @ScreenParam MainActivity mainActivity){
        this.bus = bus;
        images = new ArrayList<>();
        this.mainActivity = mainActivity;
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
        adapter = new MemoriesHomeAdapter(context, this);
        ultimateRecyclerView = getView().imageList;

        if(MemoriesService.getInstance().currentSelectedDate == null){
            Calendar c = Calendar.getInstance();
            c.set(Calendar.YEAR, 2016);
            c.set(Calendar.MONTH, Calendar.OCTOBER);
            c.set(Calendar.DAY_OF_MONTH, 3);
            MemoriesService.getInstance().currentSelectedDate = c.getTime(); //reset to the first date of conference
            MemoriesService.getInstance().startDate = c.getTime();
        }

        getView().dateTitle.setText(sdf.format(MemoriesService.getInstance().currentSelectedDate));

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

                if (images.size() > 1) {
                    MemoriesItem image = images.get(images.size() - 1);
                    Date date = image.insertTime;
                    MemoriesService.getInstance().getMorePhoto(date, false, imagesId);
                }

            }
        });
        ultimateRecyclerView.setDefaultOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                MemoriesService.getInstance().getMorePhoto(MemoriesService.getInstance().getLastUpdateTime(), false, imagesId);
            }
        });

        ultimateRecyclerView.setHasFixedSize(false);

        ultimateRecyclerView.setOnParallaxScroll(new UltimateRecyclerView.OnParallaxScroll() {
            @Override
            public void onParallaxScroll(float percentage, float offset, View parallax) {

            }
        });


        ultimateRecyclerView.setRecylerViewBackgroundColor(getView().getResources().getColor(R.color.white));
        Calendar dateCal = Calendar.getInstance();
        dateCal.set(Calendar.YEAR, 2016);
        dateCal.set(Calendar.MONTH, Calendar.OCTOBER);
        dateCal.set(Calendar.DAY_OF_MONTH, 3);
        Date date = new Date(dateCal.getTime().getTime());

        UIHelper.getInstance().showProgressDialog(context, "Loading...", false);
        MemoriesService.getInstance().getMorePhoto(date, true, imagesId);

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
    }


    @Override
    public void likeClick(MemoriesItem image, boolean isTopPhoto) {

    }

    @Override
    public void openListView(int currentselfie) {
        MemoriesListEvent event = new MemoriesListEvent();
        event.previousPage = MemoriesMainPresenter.HOME_TAB;
        event.current = currentselfie;
        event.images = images;
        event.isProfilePhoto = false;
        event.isLoadMore = isLoadMore;
        bus.post(event);
        if (currentselfie <= images.size()){
            MemoriesItem image = images.get(currentselfie);
//            String cmpId = MemoriesService.getInstance().currentPromotion.id;
        }
    }


    @Subscribe
    public void setRecentPhotos(MemoriesHomeEvent event){
        if(event != null){
            if(!event.canUplaod)
                getView().uploadPhotoRL.setVisibility(View.GONE);
            else
                getView().uploadPhotoRL.setVisibility(View.VISIBLE);
        }

        ultimateRecyclerView.setRefreshing(false);
        if (event.images != null && event.images.size() > 0){
            ultimateRecyclerView.setVisibility(View.VISIBLE);
            getView().noImage.setVisibility(View.INVISIBLE);
            isLoadMore = event.isNext;
            if (!event.isNext){
                ultimateRecyclerView.disableLoadmore();
                adapter.notifyDataSetChanged();
            } else {
                ultimateRecyclerView.reenableLoadmore();
            }
            if (event.isFirst){
                this.images.clear();
                this.images.addAll(event.images);
                adapter.setData(event.images);
            } else {
                List<MemoriesItem> sTmp = new ArrayList<>();
                for(MemoriesItem s: event.images){
                    boolean isExist = false;
                    int position = 0;
                    for(MemoriesItem image: this.images){
                        if(image.id.equals(s.id)){
                            isExist = true;
                            break;
                        }
                        position++;
                    }

                    if(!isExist) {
//                        this.images.add(0,s); //add to the front
                    }
                    else{
                        this.images.remove(position);
                        this.images.add(position, s);
                        sTmp.add(s);
                    }
                }

                event.images.removeAll(sTmp); //remove those that are already present
                this.images.addAll(event.images) ;
                adapter.insertData(event.images);

            }
        } else {
            if (images == null || images.isEmpty()){
                ultimateRecyclerView.disableLoadmore();
                adapter.notifyDataSetChanged();
                ultimateRecyclerView.setVisibility(View.INVISIBLE);
                getView().noImage.setVisibility(View.VISIBLE);
            }
        }
        imagesId = MemoriesService.getInstance().getEventIds(images);
        UIHelper.getInstance().dismissProgressDialog();
    }


    @Subscribe
    public void resetPhotos(MemoriesResetHomeEvent event){
        images.clear();
        adapter.reset();
        Date date = new Date();
        date = new Date(date.getTime() + 10000);

        imagesId = "0";
        UIHelper.getInstance().showProgressDialog(context, "Loading...", false);
        MemoriesService.getInstance().getMorePhoto(date, true, imagesId);

        getView().dateTitle.setText(sdf.format(event.date));
    }

    @Subscribe
    public void badgeNotiEvent(BadgeNotiEvent event){
        MasterPointService.getInstance().getBadgesAPI();
        MasterPointService.getInstance().showToolTips(getView().imageList, event.badgeName);
    }
}
