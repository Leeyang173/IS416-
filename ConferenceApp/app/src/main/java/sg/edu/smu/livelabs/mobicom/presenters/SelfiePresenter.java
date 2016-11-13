package sg.edu.smu.livelabs.mobicom.presenters;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.List;

import autodagger.AutoComponent;
import automortar.AutoScreen;
import flow.Flow;
import flow.path.Path;
import mortar.MortarScope;
import mortar.ViewPresenter;
import rx.functions.Action0;
import sg.edu.smu.livelabs.mobicom.ActionBarOwner;
import sg.edu.smu.livelabs.mobicom.App;
import sg.edu.smu.livelabs.mobicom.AppDependencies;
import sg.edu.smu.livelabs.mobicom.DaggerScope;
import sg.edu.smu.livelabs.mobicom.MainActivity;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.UIHelper;
import sg.edu.smu.livelabs.mobicom.adapters.SlidePagerAdapter;
import sg.edu.smu.livelabs.mobicom.busEvents.BadgeNotiEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.SelfieLikerEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.SelfieListEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.SelfieProfileEvent;
import sg.edu.smu.livelabs.mobicom.flow.Layout;
import sg.edu.smu.livelabs.mobicom.net.response.SelfieUserResponse;
import sg.edu.smu.livelabs.mobicom.presenters.screen.SelfieHomeScreen;
import sg.edu.smu.livelabs.mobicom.presenters.screen.SelfieLikersScreen;
import sg.edu.smu.livelabs.mobicom.presenters.screen.SelfieListScreen;
import sg.edu.smu.livelabs.mobicom.presenters.screen.SelfieProfileScreen;
import sg.edu.smu.livelabs.mobicom.presenters.screen.SelfieSearchScreen;
import sg.edu.smu.livelabs.mobicom.services.DatabaseService;
import sg.edu.smu.livelabs.mobicom.services.EVAPromotionService;
import sg.edu.smu.livelabs.mobicom.services.GameService;
import sg.edu.smu.livelabs.mobicom.services.MasterPointService;
import sg.edu.smu.livelabs.mobicom.services.ScreenService;
import sg.edu.smu.livelabs.mobicom.services.TrackingService;
import sg.edu.smu.livelabs.mobicom.views.SelfieView;

/**
 * Created by smu on 26/10/15.
 */
@AutoScreen(
        component = @AutoComponent(dependencies = MainActivity.class, superinterfaces = AppDependencies.class),
        screenAnnotations = Layout.class
)
@DaggerScope(SelfiePresenter.class)
@Layout(R.layout.selfie_view)
public class SelfiePresenter extends ViewPresenter<SelfieView> implements View.OnClickListener{
    public static String NAME = "SelfiePresenter";
    public static final int HOME_TAB = 0;
    public static final int SEARCH_TAB = 1;
    public static final int PROFILE_TAB = 2;
    public static final int LIST_TAB = 3;
    public static final int LIKER_TAB = 4;
    public static final int CAMERA_TAB = 5;

    private int currentTab;
    private String currentTabName;
    private Bus bus;
    private ActionBarOwner actionBarOwner;
    private SlidePagerAdapter pagerAdapter;
    private MainActivity mainActivity;
    private ScreenService screenService;
    private Context context;

    public SelfiePresenter(ActionBarOwner actionBarOwner, Bus bus, MainActivity mainActivity, ScreenService screenService) {
        this.actionBarOwner = actionBarOwner;
        this.bus = bus;
        this.mainActivity = mainActivity;
        this.screenService = screenService;
    }

    @Override
    protected void onLoad(Bundle savedInstanceState) {
        super.onLoad(savedInstanceState);
        currentTab = -1;

        actionBarOwner.setConfig(new ActionBarOwner.Config(true, null,
                new ActionBarOwner.MenuAction("Main", new Action0() {
                    @Override
                    public void call() {
                        SelfieListEvent event = screenService.pop(SelfieListPresenter.class);
                        TrackingService.getInstance().sendTracking("413", "games",
                                "coolfie", EVAPromotionService.getInstance().currentPromotion.id, "main", "");
                        if (event == null){
                            setSelected(HOME_TAB);
                        } else {
                            bus.post(event);
                        }
                    }
                }),
                new ActionBarOwner.MenuAction("My Photos", new Action0() {
                    @Override
                    public void call() {
                        UIHelper.getInstance().showProgressDialog(context, "Loading", false);
                        EVAPromotionService.getInstance().getUserPhotos(DatabaseService.getInstance().getMe(), -1);
                        setSelected(PROFILE_TAB);
                        TrackingService.getInstance().sendTracking("412", "games",
                                "coolfie", EVAPromotionService.getInstance().currentPromotion.id, "my photo", "");
                    }
                }),
                new ActionBarOwner.MenuAction(R.drawable.icon_search, new Action0() {
                    @Override
                    public void call() {
                        setSelected(SEARCH_TAB);
                        TrackingService.getInstance().sendTracking("414", "games",
                                "coolfie", EVAPromotionService.getInstance().currentPromotion.id, "search", "");
                    }
                })

        ,ActionBarOwner.Config.LEFT_FOCUS));
        context = getView().getContext();
        Path[] paths = new Path[]{
                new SelfieHomeScreen(mainActivity),
                new SelfieSearchScreen(screenService),
                new SelfieListScreen(screenService, mainActivity),
                new SelfieProfileScreen(screenService, mainActivity),
                new SelfieLikersScreen(screenService)
        };
        pagerAdapter = new SlidePagerAdapter(getView().getContext(), paths);
        getView().pager.setOffscreenPageLimit(0);
        getView().pager.setAdapter(pagerAdapter);
        try {
            SelfieListEvent event = screenService.pop(SelfieListPresenter.class);
            if (event == null) {
                setSelected(HOME_TAB);
            } else {
                openSelfieListView(event);
            }
        }
        catch(ClassCastException e){
            Log.d("AAA", e.toString());
            setSelected(HOME_TAB);
        }
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

        if(!GameService.getInstance().fromGames(App.getInstance().previousPresenter)
                && !App.getInstance().currentPresenter.equals("SelfieFullScreenPresenter")){
            mainActivity.setVisibleBottombar(View.VISIBLE);
        }

        if (NAME.equals(App.getInstance().currentPresenter)){
            App.getInstance().currentPresenter = "";
        }
        mainActivity.resetAllToolbarMenu();
    }

    @Override
    public void onClick(View v) {
        try {
            screenService.clearStack(SelfieLikersPresenter.class);
            screenService.clearStack(SelfieListPresenter.class);
            screenService.clearStack(SelfieProfilePresenter.class);
        }catch (Exception e){
//            Log.d("XXX: cannot clear stack", e.toString());
        }
        SelfieListPresenter.previousPage = - 1;
        SelfieProfilePresenter.previousPage = -1;
//        int id = v.getId();
//        if(id == R.id.home_btn){
//            setSelected(HOME_TAB);
//        } else if (id == R.id.camera_btn){
//            UIHelper.getInstance().requestPhoto(mainActivity, new Action1<String>() {
//                @Override
//                public void call(String filepath) {
//                    Selfie selfie = new Selfie();
//                    selfie.imageId = filepath;
//                    selfie.id = null;
//                    Flow.get(context).set(new SelfieCameraScreen(selfie));
//                    EVAPromotionService.getInstance().selfieTracking("608",
//                            EVAPromotionService.getInstance().currentPromotion.id,
//                            UIHelper.photoPlace, "");
//                }
//            });
//        } else if (id == R.id.search_btn){
//            setSelected(SEARCH_TAB);
//        } else if (id == R.id.profile_btn){
//            getView().homeBtn.setSelected(false);
//            getView().cameraBtn.setSelected(false);
//            getView().searchBtn.setSelected(false);
//            getView().profileBtn.setSelected(true);
//            UIHelper.getInstance().showProgressDialog(context, "Loading", false);
//            EVAPromotionService.getInstance().getUserPhotos(DatabaseService.getInstance().getMe(), -1);
//        }
//        EVAPromotionService.getInstance().selfieTracking("602",
//                EVAPromotionService.getInstance().currentPromotion.id, currentTabName, "");
    }

    @Subscribe
    public void openProfile(final SelfieProfileEvent event) {
        System.out.println("Opening Selfie presenter profile");
        if (currentTab == PROFILE_TAB) return;
        if (event.isFromMainPage || event.hasExecuted) return;
        event.isFromMainPage = true;
        if (event.user == null){
            SelfieProfileEvent cacheEvent = screenService.pop(SelfieProfilePresenter.class);
            if (cacheEvent != null){
                postDelay100(cacheEvent);
            }
            return;
        }
        if (event.user.getUID() == DatabaseService.getInstance().getMe().getUID()){
            setSelected(PROFILE_TAB);
        } else {
            setSelected(PROFILE_TAB);
        }
        postDelay100(event);
    }

    @Subscribe
    public void openSelfieListView(SelfieListEvent event){
        if (currentTab == LIST_TAB) return;
        if (event.isFromMainPage || event.hasExecuted) return;
        event.isFromMainPage = true;
        setSelected(LIST_TAB);
        postDelay100(event);
    }

    @Subscribe
    public void openLikerScreen(SelfieLikerEvent event){
        System.out.println("Selfie opening liker screen");
        if (currentTab == LIKER_TAB) return;
        if (event.isFromMainPage || event.hasExecuted) return;
        event.isFromMainPage = true;
        if (event.users != null){
            setSelected(LIKER_TAB);
            postDelay100(event);
        }
        UIHelper.getInstance().dismissProgressDialog();
    }

    private void postDelay100(final Object event){
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                bus.post(event);
            }
        }, 100);
    }

    public boolean goBack(){
        switch (currentTab){
            case LIST_TAB:
                setSelected(SelfieListPresenter.previousPage);
                if (SelfieListPresenter.previousPage == PROFILE_TAB){
                    SelfieProfileEvent event = screenService.pop(SelfieProfilePresenter.class);
                    if (event != null){
                        bus.post(event);
                    }
                }
                break;
            case PROFILE_TAB:
                if(SelfieProfilePresenter.previousPage == SEARCH_TAB){
                    setSelected(SEARCH_TAB);
                } else if(SelfieProfilePresenter.previousPage == LIKER_TAB){
                    List<SelfieUserResponse> users = screenService.pop(SelfieLikersPresenter.class);
                    if (users == null) {
                        setSelected(HOME_TAB);
                    } else {
                        SelfieLikerEvent event = new SelfieLikerEvent();
                        event.users = users;
                        bus.post(event);
                    }
                } else if (SelfieProfilePresenter.previousPage == LIST_TAB){
                    SelfieListEvent event = screenService.pop(SelfieListPresenter.class);
                    if (event == null){
                        setSelected(HOME_TAB);
                    } else {
                        bus.post(event);
                    }
                } else{
                    Flow.get(context).goBack();
                }
                break;
            case LIKER_TAB:
                SelfieListEvent event = screenService.pop(SelfieListPresenter.class);
                if (event == null){
                    setSelected(HOME_TAB);
                } else {
                    bus.post(event);
                }
                break;
            default:
                Flow.get(context).goBack();
                break;
        }
        return true;
    }

    private void setSelected(int tab) {
        if(getView() == null) return;
        switch (tab) {
            case HOME_TAB:
                getView().pager.setCurrentItem(0, false);
                currentTabName = "main";
                break;
            case LIST_TAB:
                if (currentTab != - 1){
                    SelfieListPresenter.previousPage = currentTab;
                }

                getView().pager.setCurrentItem(2, false);
                currentTabName = "list";
                break;
            case SEARCH_TAB:
                getView().pager.setCurrentItem(1, false);
                currentTabName = "search";
                mainActivity.unselectAllToolbarMenu();
                break;
            case PROFILE_TAB:
                getView().pager.setCurrentItem(3);
                currentTabName = "profile";
                break;
            case LIKER_TAB:
                getView().pager.setCurrentItem(4);
                currentTabName = "liker";

        }
        currentTab = tab;
    }

    @Subscribe
    public void badgeNotiEvent(BadgeNotiEvent event){
        MasterPointService.getInstance().getBadgesAPI();
        if(getView() == null) return;
        MasterPointService.getInstance().showToolTips(getView().pager, event.badgeName);
    }
}
