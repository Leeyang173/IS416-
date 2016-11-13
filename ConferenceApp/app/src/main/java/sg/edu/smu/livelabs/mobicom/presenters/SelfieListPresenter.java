package sg.edu.smu.livelabs.mobicom.presenters;

import android.Manifest;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerView;
import com.marshalchen.ultimaterecyclerview.stickyheadersrecyclerview.StickyRecyclerHeadersDecoration;
import com.marshalchen.ultimaterecyclerview.stickyheadersrecyclerview.StickyRecyclerHeadersTouchListener;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import autodagger.AutoComponent;
import automortar.AutoScreen;
import automortar.ScreenParam;
import flow.Flow;
import mortar.MortarScope;
import mortar.ViewPresenter;
import rx.functions.Action0;
import rx.functions.Action1;
import sg.edu.smu.livelabs.mobicom.AppDependencies;
import sg.edu.smu.livelabs.mobicom.DaggerScope;
import sg.edu.smu.livelabs.mobicom.MainActivity;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.UIHelper;
import sg.edu.smu.livelabs.mobicom.adapters.SelfieListViewAdapter;
import sg.edu.smu.livelabs.mobicom.busEvents.BadgeNotiEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.SelfieHomeEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.SelfieListEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.SelfieProfileEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.SelfieStatusEvent;
import sg.edu.smu.livelabs.mobicom.fileupload.UploadFileService;
import sg.edu.smu.livelabs.mobicom.flow.Layout;
import sg.edu.smu.livelabs.mobicom.models.User;
import sg.edu.smu.livelabs.mobicom.net.item.Selfie;
import sg.edu.smu.livelabs.mobicom.net.response.SimpleResponse;
import sg.edu.smu.livelabs.mobicom.presenters.screen.SelfieCameraScreen;
import sg.edu.smu.livelabs.mobicom.presenters.screen.SelfieFullScreenScreen;
import sg.edu.smu.livelabs.mobicom.services.DatabaseService;
import sg.edu.smu.livelabs.mobicom.services.EVAPromotionService;
import sg.edu.smu.livelabs.mobicom.services.MasterPointService;
import sg.edu.smu.livelabs.mobicom.services.ScreenService;
import sg.edu.smu.livelabs.mobicom.services.TrackingService;
import sg.edu.smu.livelabs.mobicom.views.SelfieListView;

/**
 * Created by smu on 3/11/15.
 */
@AutoScreen(
        component = @AutoComponent(dependencies = SelfiePresenter.class, superinterfaces = AppDependencies.class),
        screenAnnotations = Layout.class
)
@DaggerScope(SelfieListPresenter.class)
@Layout(R.layout.selfie_list_view)
public class SelfieListPresenter extends ViewPresenter<SelfieListView> {
    public static int previousPage;
    private SelfieListViewAdapter adapter;
    private Context context;
    private Bus bus;
    private ScreenService screenService;
    private boolean isProfilePhoto;
    private boolean isLoadMore;
    private int deletingItemIndex;
    private MainActivity mainActivity;
    public SelfieListPresenter(Bus bus, @ScreenParam ScreenService screenService, @ScreenParam MainActivity mainActivity){
        this.bus = bus;
        this.screenService = screenService;
        this.mainActivity = mainActivity;

    }
    @Override
    protected void onLoad(Bundle savedInstanceState) {
        super.onLoad(savedInstanceState);
        if (!hasView()) return;

        context = getView().getContext();
        adapter = new SelfieListViewAdapter(context, this, getView().ultimateRecyclerView);
        adapter.setData(new ArrayList<Selfie>());
        UltimateRecyclerView ultimateRecyclerView = getView().ultimateRecyclerView;
        ultimateRecyclerView.setHasFixedSize(false);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        ultimateRecyclerView.setLayoutManager(linearLayoutManager);
        ultimateRecyclerView.setAdapter(adapter);
        ultimateRecyclerView.setEmptyView(R.layout.empty_view, R.id.no_image);
        StickyRecyclerHeadersDecoration headersDecor = new StickyRecyclerHeadersDecoration(adapter);

        ultimateRecyclerView.addItemDecoration(headersDecor);

        StickyRecyclerHeadersTouchListener touchListener =
                new StickyRecyclerHeadersTouchListener(ultimateRecyclerView.mRecyclerView, headersDecor);

        touchListener.setOnHeaderClickListener(
                new StickyRecyclerHeadersTouchListener.OnHeaderClickListener() {
                    @Override
                    public void onHeaderClick(View header, int position, long headerId) {
//                        System.out.println("on header click" +position);

                        if (isProfilePhoto) {
                            bus.post(new SelfieProfileEvent(null, -1, null, false, -1));
                            return;
                        }

                        Selfie selfie = adapter.getItem(position);
                        UIHelper.getInstance().showProgressDialog(context, "Loading...", false);
                        User user = new User();
                        user.setUID(selfie.userId);
                        user.setEmail(selfie.email);
                        user.setName(selfie.username);
                        user.setAvatar(selfie.userAvatar);
                        cacheData(adapter.getAllItems(), position);
                        EVAPromotionService.getInstance().getUserPhotos(user, SelfiePresenter.LIST_TAB);
                    }
                });
        ultimateRecyclerView.addOnItemTouchListener(touchListener);


        ultimateRecyclerView.reenableLoadmore();
        adapter.setCustomLoadMoreView(LayoutInflater.from(context)
                .inflate(R.layout.custom_bottom_progressbar, null));
        ultimateRecyclerView.setOnLoadMoreListener(new UltimateRecyclerView.OnLoadMoreListener() {
            @Override
            public void loadMore(int itemsCount, int maxLastVisiblePosition) {
                if (adapter.getAdapterItemCount() > 1) {
                    Selfie selfie = adapter.getLastItem();
                    if (selfie != null) {
                        Date date = selfie.createdTime;
                        EVAPromotionService.getInstance().getMorePhoto(date, false);
                    }
                }

            }
        });

        ultimateRecyclerView.setDefaultOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                System.out.println("List view refreshing " + new Date());
                EVAPromotionService.getInstance().getMorePhoto(EVAPromotionService.getInstance().getLastUpdateTime(), false);
            }
        });


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
    public void goToSelfieListView(SelfieListEvent event){
        System.out.println("Calling this goToSelfieListView... " + event.isLoadMore);
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
        adapter.setData(event.selfies);
        getView().ultimateRecyclerView.scrollVerticallyToPosition(event.current);
    }

    @Subscribe
    public void setRecentPhotos(SelfieHomeEvent event){
        getView().ultimateRecyclerView.setRefreshing(false);
        if (event.selfies != null){
            if (event.isNext){
                isLoadMore = true;
                getView().ultimateRecyclerView.reenableLoadmore();
            } else{
                isLoadMore = false;
                getView().ultimateRecyclerView.disableLoadmore();
                adapter.notifyDataSetChanged();
            }
            if (event.isFirst){
                adapter.setData(event.selfies);
            } else {
                User me = DatabaseService.getInstance().getMe();
                List<Selfie> selfiesEvent = event.selfies;
                List<Selfie> notMyPhoto = new ArrayList<>();
                if(isProfilePhoto){
                    for(Selfie s: selfiesEvent){
                        if(s.userId != me.getUID()){
                            notMyPhoto.add(s);
                        }
                    }

                    selfiesEvent.removeAll(notMyPhoto);
                }

                adapter.insertData(selfiesEvent);
            }
        }
        UIHelper.getInstance().dismissProgressDialog();
    }

    @Subscribe
    public void updatePhotoStatus(SelfieStatusEvent event){
        if (event.isSuccess){
            adapter.notifyDataSetChanged();
            UIHelper.getInstance().dismissProgressDialog();
        } else {
            UIHelper.getInstance().dismissProgressDialog();
            UIHelper.getInstance().showAlert(context, event.details);
        }
    }

    public void goToFullScreen(int currentPosition) {
        cacheData(adapter.getAllItems(), currentPosition);
        Flow.get(context).set(new SelfieFullScreenScreen(adapter.getAllItems(), currentPosition, isProfilePhoto));
    }

    public void goToLikerScreen(int currentPosition) {
        cacheData(adapter.getAllItems(), currentPosition);
        UIHelper.getInstance().showProgressDialog(context, "Loading...", false);
        EVAPromotionService.getInstance().getLikesPhotoDetail(adapter.getItem(currentPosition));
    }

    private void cacheData(List<Selfie> selfies, int current){
        SelfieListEvent event = new SelfieListEvent();
        event.previousPage = previousPage;
        event.selfies = new ArrayList<>(selfies);
        event.current = current;
        event.isProfilePhoto = isProfilePhoto;
        event.isLoadMore = isLoadMore;
        screenService.push(SelfieListPresenter.class, event);
    }

    public void editSelfie(final Selfie selfie, final int postion, final ImageView image) {
        String[] menuString;
        User user = DatabaseService.getInstance().getMe();
        if (user != null && selfie.userId == user.getUID()){
            menuString = new String[]{"Set as avatar", "Download photo", "Edit caption", "Delete"};
        } else {
            menuString = new String[]{"Set as avatar", "Download photo"};
        }
        final String cmpId = EVAPromotionService.getInstance().currentPromotion.id;
        new MaterialDialog.Builder(context)
                .items(menuString)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                        final Resources rs = context.getResources();
                        switch (which) {
                            case 0:
                                EVAPromotionService.getInstance().setUserAvatar(selfie.imageId);
                                UIHelper.getInstance().showAlert(context, rs.getString(R.string.avatar_updated));
                                TrackingService.getInstance().sendTracking("418", "games",
                                        "coolfie", EVAPromotionService.getInstance().currentPromotion.id, selfie.id, "avatar");
//                                EVAPromotionService.getInstance().selfieTracking("605", cmpId, selfie.id, "avatar");
                                break;
                            case 1:
                                mainActivity.checkAndRequirePermissions(
                                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                        new Action1<String>() {

                                            @Override
                                            public void call(String s) {
                                                UIHelper.getInstance().showConfirmAlert(context, context.getString(R.string.app_name), rs.getString(R.string.confirm_saving_photo), "Yes", "No", new Action0() {
                                                    @Override
                                                    public void call() {
                                                        String title;
                                                        if (selfie.description != null) {
                                                            title = context.getString(R.string.app_name) + "_" + selfie.description.replace(" ", "_");
                                                        } else {
                                                            title = context.getString(R.string.app_name);
                                                        }
                                                        try {
                                                            UploadFileService.getInstance().saveImage(context, title, ((BitmapDrawable) image.getDrawable()).getBitmap());
                                                        } catch (Exception e) {
                                                            UIHelper.getInstance().showAlert(context, rs.getString(R.string.unable_to_save_photo));
                                                            e.printStackTrace();
                                                        }
                                                        TrackingService.getInstance().sendTracking("419", "games",
                                                                "coolfie", EVAPromotionService.getInstance().currentPromotion.id, selfie.id, "download");
//                                        EVAPromotionService.getInstance().selfieTracking("605", cmpId, selfie.id, "save");
                                                    }
                                                }, new Action0() {
                                                    @Override
                                                    public void call() {

                                                    }
                                                });
                                            }
                                        });
                                break;
                            case 2:
                                cacheData(adapter.getAllItems(), postion);
                                Flow.get(context).set(new SelfieCameraScreen(selfie));
//                                EVAPromotionService.getInstance().selfieTracking("605", cmpId, selfie.id, "edit");
                                break;
                            case 3:
                                UIHelper.getInstance().showConfirmAlert(context, context.getString(R.string.app_name), rs.getString(R.string.confirm_delete_photo), "Yes", "No", new Action0() {
                                    @Override
                                    public void call() {
                                        UIHelper.getInstance().showProgressDialog(context, "Processing...", false);
                                        deletingItemIndex = postion;
                                        EVAPromotionService.getInstance().deletePhoto(selfie.id);
//                                        EVAPromotionService.getInstance().selfieTracking("605", cmpId, selfie.id, "delete");
                                    }
                                }, new Action0() {
                                    @Override
                                    public void call() {

                                    }
                                });
                                break;

                        }
                    }
                })
                .show();
    }

    @Subscribe
    public void deletePhotoResult(SimpleResponse response){
        UIHelper.getInstance().dismissProgressDialog();
        if(!"success".equals(response.status)){
            UIHelper.getInstance().showAlert(context, context.getResources().getString(R.string.cannot_delete_photo));
        } else {
//            MasterPointService.getInstance().deductPoint(MasterPointService.getInstance().COOLFIE_PHOTO_UPLOAD);
            adapter.removeItemAt(deletingItemIndex);
            if (adapter.getAllItems().size() == 0){
                if (isProfilePhoto){
                    SelfieProfileEvent event = screenService.pop(SelfieProfilePresenter.class);
                    if (event != null){
                        event.photos.clear();
                        screenService.push(SelfieProfilePresenter.class, event);
                    }
                    bus.post(new SelfieProfileEvent(null, -1, null, false, -1));
                    return;
                }
            }
        }
    }

    @Subscribe
    public void badgeNotiEvent(BadgeNotiEvent event){
        MasterPointService.getInstance().getBadgesAPI();
        MasterPointService.getInstance().showToolTips(getView().ultimateRecyclerView, event.badgeName);
    }
}
