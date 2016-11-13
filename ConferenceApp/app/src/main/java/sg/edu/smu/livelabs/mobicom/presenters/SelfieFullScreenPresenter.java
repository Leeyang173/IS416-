package sg.edu.smu.livelabs.mobicom.presenters;

import android.Manifest;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.afollestad.materialdialogs.MaterialDialog;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.text.SimpleDateFormat;
import java.util.List;

import autodagger.AutoComponent;
import automortar.AutoScreen;
import automortar.ScreenParam;
import flow.Flow;
import mortar.MortarScope;
import mortar.ViewPresenter;
import rx.functions.Action0;
import rx.functions.Action1;
import sg.edu.smu.livelabs.mobicom.App;
import sg.edu.smu.livelabs.mobicom.AppDependencies;
import sg.edu.smu.livelabs.mobicom.DaggerScope;
import sg.edu.smu.livelabs.mobicom.MainActivity;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.UIHelper;
import sg.edu.smu.livelabs.mobicom.adapters.SelfieFullScreenAdapter;
import sg.edu.smu.livelabs.mobicom.busEvents.BadgeNotiEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.SelfieListEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.SelfieStatusEvent;
import sg.edu.smu.livelabs.mobicom.fileupload.UploadFileService;
import sg.edu.smu.livelabs.mobicom.flow.Layout;
import sg.edu.smu.livelabs.mobicom.models.User;
import sg.edu.smu.livelabs.mobicom.net.item.Selfie;
import sg.edu.smu.livelabs.mobicom.net.response.SimpleResponse;
import sg.edu.smu.livelabs.mobicom.presenters.screen.SelfieCameraScreen;
import sg.edu.smu.livelabs.mobicom.services.DatabaseService;
import sg.edu.smu.livelabs.mobicom.services.EVAPromotionService;
import sg.edu.smu.livelabs.mobicom.services.MasterPointService;
import sg.edu.smu.livelabs.mobicom.services.ScreenService;
import sg.edu.smu.livelabs.mobicom.services.TrackingService;
import sg.edu.smu.livelabs.mobicom.views.SelfieFullScreenView;

/**
 * Created by smu on 13/11/15.
 */
@AutoScreen(
        component = @AutoComponent(dependencies = MainActivity.class, superinterfaces = AppDependencies.class),
        screenAnnotations = Layout.class
)
@DaggerScope(SelfieFullScreenPresenter.class)
@Layout(R.layout.selfie_full_screen_view)
public class SelfieFullScreenPresenter extends ViewPresenter<SelfieFullScreenView> implements View.OnClickListener{

    public static String NAME = "SelfieFullScreenPresenter";
    private Bus bus;
    private MainActivity mainActivity;
    private ScreenService screenService;
    private List<Selfie> selfies;
    private int current;
    private Selfie currentSelfie;
    private boolean isProfilePhoto;
    private Context context;
    private SelfieFullScreenAdapter adapter;
    private SimpleDateFormat dateToStrFormat = new SimpleDateFormat("dd MMM yyyy");

    public SelfieFullScreenPresenter(MainActivity mainActivity, Bus bus, ScreenService screenService,
                                     @ScreenParam List<Selfie> selfies, @ScreenParam int currentSelfie, @ScreenParam boolean isProfilePhoto){
        this.bus = bus;
        this.selfies = selfies;
        this.mainActivity = mainActivity;
        this.screenService = screenService;
        this.current = currentSelfie;
        this.isProfilePhoto = isProfilePhoto;
        this.currentSelfie = selfies.get(current);
    }

    @Override
    protected void onEnterScope(MortarScope scope) {
        super.onEnterScope(scope);
        bus.register(this);
        App.getInstance().setPrevious();
        App.getInstance().currentPresenter = NAME;
        mainActivity.setVisibleBottombar(View.GONE);

    }

    private void setInfomation() {
        currentSelfie = selfies.get(current);
        getView().description.setText(currentSelfie.description);
        getView().likeno.setText(currentSelfie.getLikesCount());
        getView().tokenText.setText(currentSelfie.token);
        getView().dateText.setText(dateToStrFormat.format(currentSelfie.createdTime));
        if ("yes".equals(currentSelfie.likeStatus)) {
            getView().like.setSelected(true);
        } else {
            getView().like.setSelected(false);
        }
        if ("yes".equals(currentSelfie.reportStatus)) {
            getView().report.setSelected(true);
        } else {
            getView().report.setSelected(false);
        }
    }

    @Override
    protected void onExitScope() {
        super.onExitScope();
        bus.unregister(this);
    }

    @Override
    protected void onLoad(Bundle savedInstanceState) {
        super.onLoad(savedInstanceState);
        context = getView().getContext();

        mainActivity.getSupportActionBar().hide();
        mainActivity.setVisibleBottombar(View.GONE);
        adapter = new SelfieFullScreenAdapter(getView().getContext(), selfies, this);
        getView().pager.setAdapter(adapter);
        getView().pager.setCurrentItem(current);
        getView().pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                current = position;
                setInfomation();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        setInfomation();

        getView().pager.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
    }

    @Subscribe
    public void updatePhotoStatus(SelfieStatusEvent event){
        if (event.isSuccess) {
            switch (event.type) {
                case SelfieStatusEvent.LIKE:
                    getView().like.setSelected(true);
                    getView().likeno.setText(currentSelfie.getLikesCount());
                    break;
                case SelfieStatusEvent.UNLIKE:
                    getView().like.setSelected(false);
                    getView().likeno.setText(currentSelfie.getLikesCount());
                    break;
                case SelfieStatusEvent.REPORT:
                    getView().report.setSelected(true);
                    break;
            }
            UIHelper.getInstance().dismissProgressDialog();
        } else {
            UIHelper.getInstance().dismissProgressDialog();
            UIHelper.getInstance().showAlert(context, event.details);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        final String cmpId = EVAPromotionService.getInstance().currentPromotion.id;
        switch (id){
            case R.id.editBtn:
                String[] menuString;
                User user = DatabaseService.getInstance().getMe();
                if (user != null && currentSelfie.userId == user.getUID()){
                    menuString = new String[]{"Set as avatar",  "Edit caption", "Delete"};
                } else {
                    menuString = new String[]{"Set as avatar"};
                }
                new MaterialDialog.Builder(context)
                        .items(menuString)
                        .itemsCallback(new MaterialDialog.ListCallback() {
                            @Override
                            public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                                switch (which){
                                    case 0:
                                        EVAPromotionService.getInstance().setUserAvatar(currentSelfie.imageId);
                                        UIHelper.getInstance().showAlert(context, context.getResources().getString(R.string.avatar_updated));
                                        TrackingService.getInstance().sendTracking("418", "games",
                                                "coolfie", EVAPromotionService.getInstance().currentPromotion.id, currentSelfie.id, "avatar");
//                                        EVAPromotionService.getInstance().selfieTracking("605", cmpId, currentSelfie.id, "avatar");
                                        break;
                                    case 1:
                                        mainActivity.getSupportActionBar().show();
                                        mainActivity.bottomBar.setVisibility(View.VISIBLE);
                                        Flow.get(context).set(new SelfieCameraScreen(currentSelfie));
//                                        EVAPromotionService.getInstance().selfieTracking("605", cmpId, currentSelfie.id, "edit");
                                        break;
                                    case 2:
                                        UIHelper.getInstance().showConfirmAlert(context, context.getString(R.string.app_name), context.getResources().getString(R.string.confirm_delete_photo), "Yes", "No", new Action0() {
                                            @Override
                                            public void call() {
                                                UIHelper.getInstance().showProgressDialog(context, "Processing...", false);
                                                EVAPromotionService.getInstance().deletePhoto(currentSelfie.id);
//                                                EVAPromotionService.getInstance().selfieTracking("605", cmpId, currentSelfie.id, "delete");
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
                break;
            case R.id.like:
                UIHelper.getInstance().showProgressDialog(context, "Waiting...", false);
                if ("yes".equals(currentSelfie.likeStatus)){
                    EVAPromotionService.getInstance().unlike(currentSelfie, false);
                } else{
                    EVAPromotionService.getInstance().like(currentSelfie, false, getView().buttonLayout);
                }
//                EVAPromotionService.getInstance().selfieTracking("605", cmpId, currentSelfie.id, "like");
                break;
            case R.id.download:
                mainActivity.checkAndRequirePermissions(
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        new Action1<String>() {

                            @Override
                            public void call(String s) {
                                UIHelper.getInstance().showConfirmAlert(context, context.getString(R.string.app_name), context.getResources().getString(R.string.confirm_saving_photo), "Yes", "No", new Action0() {
                                    @Override
                                    public void call() {
                                        String title;
                                        if (currentSelfie.description != null) {
                                            title = context.getString(R.string.app_name)+ "_" + currentSelfie.description.replace(" ", "_");
                                        } else {
                                            title = context.getString(R.string.app_name);
                                        }
                                        try {
                                            ImageView image = (ImageView) getView().pager.findViewWithTag(current);
                                            UploadFileService.getInstance().saveImage(context, title, ((BitmapDrawable) image.getDrawable()).getBitmap());
                                        } catch (Exception e) {
                                            UIHelper.getInstance().showAlert(context, context.getResources().getString(R.string.unable_to_save_photo));
                                            e.printStackTrace();
                                        }

                                    }
                                }, new Action0() {
                                    @Override
                                    public void call() {

                                    }
                                });

                                TrackingService.getInstance().sendTracking("419", "games",
                                        "coolfie", EVAPromotionService.getInstance().currentPromotion.id, currentSelfie.id, "download");
                            }
                        });

//                EVAPromotionService.getInstance().selfieTracking("605", cmpId, currentSelfie.id, "save");
                break;
            case R.id.report:
                if ("yes".equals(currentSelfie.reportStatus)){
                    UIHelper.getInstance().showAlert(context, context.getResources().getString(R.string.reported));
                    return;
                }
                UIHelper.getInstance().showConfirmAlert(context, context.getString(R.string.app_name), context.getResources().getString(R.string.confirm_report_photo), "Yes", "No", new Action0() {
                    @Override
                    public void call() {
                        UIHelper.getInstance().showAlert(context, context.getResources().getString(R.string.thanks_report));
                        EVAPromotionService.getInstance().report(currentSelfie, false);
                    }
                }, new Action0() {
                    @Override
                    public void call() {

                    }
                });
//                EVAPromotionService.getInstance().selfieTracking("605", cmpId, currentSelfie.id, "report");
                break;
        }
    }

    public void hideOrShowControlLayout() {
        getView().hideOrShowButtonLayout();
    }

    //For closing of Full Screen Presenter
    public void closePage() {
        SelfieListEvent event = screenService.pop(SelfieListPresenter.class);
        if(event != null) {
            event.current = current;
            event.selfies = selfies;
            screenService.push(SelfieListPresenter.class, event);
        }
        mainActivity.getSupportActionBar().show();
        mainActivity.bottomBar.setVisibility(View.VISIBLE);
    }

    @Subscribe
    public void deletePhotoResult(SimpleResponse response){
        UIHelper.getInstance().dismissProgressDialog();
        if(!"success".equals(response.status)){
            UIHelper.getInstance().showAlert(context, context.getResources().getString(R.string.cannot_delete_photo));
        } else {
            selfies.remove(current);
            adapter.notifyDataSetChanged();
            if (selfies.size() == 0) {
                closePage();
                Flow.get(context).goBack();
            } else {
                current = getView().pager.getCurrentItem();
                currentSelfie = selfies.get(current);
            }
        }
    }

    @Subscribe
    public void badgeNotiEvent(BadgeNotiEvent event){
        MasterPointService.getInstance().getBadgesAPI();
        MasterPointService.getInstance().showToolTips(getView().parent, event.badgeName);
    }
}
