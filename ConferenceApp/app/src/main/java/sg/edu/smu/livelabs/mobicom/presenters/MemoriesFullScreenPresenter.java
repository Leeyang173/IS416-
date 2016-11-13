package sg.edu.smu.livelabs.mobicom.presenters;

import android.Manifest;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.text.SimpleDateFormat;
import java.util.List;

import autodagger.AutoComponent;
import automortar.AutoScreen;
import automortar.ScreenParam;
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
import sg.edu.smu.livelabs.mobicom.adapters.MemoriesFullScreenAdapter;
import sg.edu.smu.livelabs.mobicom.busEvents.BadgeNotiEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.MemoriesListEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.SelfieStatusEvent;
import sg.edu.smu.livelabs.mobicom.fileupload.UploadFileService;
import sg.edu.smu.livelabs.mobicom.flow.Layout;
import sg.edu.smu.livelabs.mobicom.net.item.MemoriesItem;
import sg.edu.smu.livelabs.mobicom.services.MasterPointService;
import sg.edu.smu.livelabs.mobicom.services.ScreenService;
import sg.edu.smu.livelabs.mobicom.views.MemoriesFullScreenView;

/**
 * Created by smu on 13/11/15.
 */
@AutoScreen(
        component = @AutoComponent(dependencies = MainActivity.class, superinterfaces = AppDependencies.class),
        screenAnnotations = Layout.class
)
@DaggerScope(MemoriesFullScreenPresenter.class)
@Layout(R.layout.memories_full_screen_view)
public class MemoriesFullScreenPresenter extends ViewPresenter<MemoriesFullScreenView> implements View.OnClickListener{

    public static String NAME = "MemoriesFullScreenPresenter";
    private Bus bus;
    private MainActivity mainActivity;
    private ScreenService screenService;
    private List<MemoriesItem> images;
    private int current;
    private MemoriesItem currentSelfie;
    private boolean isProfilePhoto;
    private Context context;
    private MemoriesFullScreenAdapter adapter;
    private SimpleDateFormat dateToStrFormat = new SimpleDateFormat("dd MMM yyyy");

    public MemoriesFullScreenPresenter(MainActivity mainActivity, Bus bus, ScreenService screenService,
                                       @ScreenParam List<MemoriesItem> images, @ScreenParam int currentSelfie, @ScreenParam boolean isProfilePhoto){
        this.bus = bus;
        this.images = images;
        this.mainActivity = mainActivity;
        this.screenService = screenService;
        this.current = currentSelfie;
        this.isProfilePhoto = isProfilePhoto;
        this.currentSelfie = images.get(current);
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
        currentSelfie = images.get(current);
        getView().description.setText(currentSelfie.caption);
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
        adapter = new MemoriesFullScreenAdapter(getView().getContext(), images, this);
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
            UIHelper.getInstance().dismissProgressDialog();
        } else {
            UIHelper.getInstance().dismissProgressDialog();
            UIHelper.getInstance().showAlert(context, event.details);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id){

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
                                        if (currentSelfie.caption != null) {
                                            title = context.getString(R.string.app_name)+ "_" + currentSelfie.caption.replace(" ", "_");
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
                            }
                        });
                break;

        }
    }

    public void hideOrShowControlLayout() {
        getView().hideOrShowButtonLayout();
    }

    //For closing of Full Screen Presenter
    public void closePage() {
        MemoriesListEvent event = screenService.pop(SelfieListPresenter.class);
        if(event != null) {
            event.current = current;
            event.images = images;
            screenService.push(SelfieListPresenter.class, event);
        }
        mainActivity.getSupportActionBar().show();
//        mainActivity.bottomBar.setVisibility(View.VISIBLE);
    }

    @Subscribe
    public void badgeNotiEvent(BadgeNotiEvent event){
        MasterPointService.getInstance().getBadgesAPI();
        MasterPointService.getInstance().showToolTips(getView().parent, event.badgeName);
    }
}
