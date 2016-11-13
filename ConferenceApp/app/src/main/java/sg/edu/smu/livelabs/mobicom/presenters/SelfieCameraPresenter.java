package sg.edu.smu.livelabs.mobicom.presenters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.isseiaoki.simplecropview.CropImageView;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;

import autodagger.AutoComponent;
import automortar.AutoScreen;
import automortar.ScreenParam;
import flow.Flow;
import mortar.MortarScope;
import mortar.ViewPresenter;
import rx.functions.Action0;
import rx.functions.Action1;
import sg.edu.smu.livelabs.mobicom.ActionBarOwner;
import sg.edu.smu.livelabs.mobicom.AppDependencies;
import sg.edu.smu.livelabs.mobicom.DaggerScope;
import sg.edu.smu.livelabs.mobicom.MainActivity;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.UIHelper;
import sg.edu.smu.livelabs.mobicom.busEvents.BadgeNotiEvent;
import sg.edu.smu.livelabs.mobicom.fileupload.UploadFileService;
import sg.edu.smu.livelabs.mobicom.flow.Layout;
import sg.edu.smu.livelabs.mobicom.models.data.GameListEntity;
import sg.edu.smu.livelabs.mobicom.net.item.Selfie;
import sg.edu.smu.livelabs.mobicom.net.response.SelfiePostPhotoResponse;
import sg.edu.smu.livelabs.mobicom.net.response.SimpleResponse;
import sg.edu.smu.livelabs.mobicom.services.EVAPromotionService;
import sg.edu.smu.livelabs.mobicom.services.GameService;
import sg.edu.smu.livelabs.mobicom.services.MasterPointService;
import sg.edu.smu.livelabs.mobicom.util.Util;
import sg.edu.smu.livelabs.mobicom.views.SelfieCameraView;

/**
 * Created by smu on 5/11/15.
 */
@AutoScreen(
        component = @AutoComponent(dependencies = MainActivity.class, superinterfaces = AppDependencies.class),
        screenAnnotations = Layout.class
)
@DaggerScope(SelfieCameraPresenter.class)
@Layout(R.layout.selfie_camera_view)
public class SelfieCameraPresenter extends ViewPresenter<SelfieCameraView> {
    private Selfie selfie;
    private String imageId;
    private Context context;
    private Bus bus;
    private ActionBarOwner actionBarOwner;
    private MainActivity activity;
    private Bitmap bitmap;
    private ConnectivityManager cm;

    public SelfieCameraPresenter(Bus bus, ActionBarOwner actionBarOwner, MainActivity activity, @ScreenParam Selfie selfie){
        this.bus = bus;
        this.actionBarOwner = actionBarOwner;
        this.selfie = selfie;
        this.imageId = selfie.imageId;
        this.activity = activity;
    }

    @Override
    protected void onLoad(Bundle savedInstanceState) {
        super.onLoad(savedInstanceState);
        context = getView().getContext();
        if (selfie.id == null){
            String title = "Memories";

            GameListEntity game = GameService.getInstance().getGame(7);
            if(game != null && !game.getGameName().isEmpty())
                title = game.getGameName();

            actionBarOwner.setConfig(new ActionBarOwner.Config(true, title, new ActionBarOwner.MenuAction("Post", new Action0() {
                @Override
                public void call() {
                    try {
                        if (getView().cropLayout.getVisibility() == View.VISIBLE){
                            bitmap = getView().cropImageView.getCroppedBitmap();
                        } else {
                            bitmap = ((BitmapDrawable)getView().photo.getDrawable()).getBitmap();
                        }

                        cm = (ConnectivityManager) getView().getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
//                        if(cm.getActiveNetworkInfo() != null) {
                            UploadFileService.getInstance().uploadPhoto(activity, imageId, bitmap, new Action1<String>() {
                                @Override
                                public void call(String imageId) {
                                    String message = getView().description.getText().toString().trim();
                                    UIHelper.getInstance().showProgressDialog(context, "Uploading...", false);
                                    EVAPromotionService.getInstance().postPhoto(imageId, message);
                                }

                            });
//                        }
//                        else{
//                            String message = getView().description.getText().toString().trim();
//                            CoolfieCacheService.getInstance().setUploadImage(bitmap, message, EVAPromotionService.getInstance().currentPromotion.id,
//                                    imageId);
//                        }
                    }catch (Exception e){
                        UIHelper.getInstance().showAlert(context, context.getResources().getString(R.string.upload_fail));
                        e.printStackTrace();
                    }
                }
            })));
            loadImage();
            getView().cropButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getView().cropLayout.setVisibility(View.INVISIBLE);
                    getView().photo.setVisibility(View.VISIBLE);
                    getView().photo.setImageBitmap(getView().cropImageView.getCroppedBitmap());
                    if (bitmap != null) {
                        //Android doesn't allows us reuse recycled Bitmap, see http://stackoverflow.com/questions/22129420/canvas-trying-to-use-a-recycled-bitmap-android-graphics-bitmap-in-android
//                        bitmap.recycle();
                        bitmap = null;
                    }
                }
            });
            getView().rotateRightButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getView().cropImageView.rotateImage(CropImageView.RotateDegrees.ROTATE_90D);
                }
            });
            getView().rotateLeftBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getView().cropImageView.rotateImage(CropImageView.RotateDegrees.ROTATE_270D);
                }
            });
            getView().changeImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    UploadFileService.getInstance().requestPhoto(activity, new Action1<String>() {
                        @Override
                        public void call(String filepath) {
                            selfie = new Selfie();
                            selfie.imageId = filepath;
                            selfie.id = null;
                            loadImage();
                        }
                    });
                }
            });
        } else {
            actionBarOwner.setConfig(new ActionBarOwner.Config(true, "Coolfie", new ActionBarOwner.MenuAction("Post", new Action0() {
                @Override
                public void call() {
                    String message = getView().description.getText().toString().trim();
                    if (message.equals(selfie.description)) {
                        Flow.get(context).goBack();
                        return;
                    }
                    UIHelper.getInstance().showProgressDialog(context, "Processing...", false);
                    EVAPromotionService.getInstance().editPhoto(selfie, message);
                }
            })));
            getView().description.setText(selfie.description);
            getView().description.requestFocus();
            final InputMethodManager imm =(InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);


            Picasso.with(context).cancelRequest(getView().photo);
            if (imageId != null && !imageId.isEmpty()) {
                Picasso.with(context)
                        .load(Util.getPhotoUrlFromId(imageId, 256))
                        .noFade().placeholder(R.drawable.transparent)
                        .into(getView().photo);
            }
            getView().photo.setVisibility(View.VISIBLE);
            getView().cropLayout.setVisibility(View.INVISIBLE);
        }
    }

    private void loadImage(){
        bitmap = UploadFileService.getInstance().getBitmap(imageId);
        if (bitmap == null || bitmap.isRecycled()){
            UIHelper.getInstance().showAlert(context, context.getResources().getString(R.string.file_not_support));
            return;
        }
        getView().cropImageView.setImageBitmap(bitmap);
        getView().photo.setImageBitmap(bitmap);
        getView().photo.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onEnterScope(MortarScope scope) {
        super.onEnterScope(scope);
        bus.register(this);
        activity.setVisibleBottombar(View.GONE);
    }

    @Override
    protected void onExitScope() {
        super.onExitScope();
        bus.unregister(this);

        if (bitmap != null){
            bitmap.recycle();
            bitmap = null;
        }
    }

    @Subscribe
    public void postPhotoResult(SelfiePostPhotoResponse response){
        UIHelper.getInstance().dismissProgressDialog();
        if ("success".equals(response.status)){
//            MasterPointService.getInstance().addPoint(MasterPointService.getInstance().COOLFIE_PHOTO_UPLOAD, getView().parentContainer);
            Flow.get(context).goBack();
        } else {
            if (response.details == null || response.details.isEmpty()){
                response.details = context.getResources().getString(R.string.upload_fail);
            }
            UIHelper.getInstance().showAlert(context, response.details);
        }
    }

    @Subscribe
    public void editDescriptionResult(SimpleResponse response){
        UIHelper.getInstance().dismissProgressDialog();
        if ("success".equals(response.status)){
            getView().onBackPressed();
        } else {
            UIHelper.getInstance().showAlert(context, context.getResources().getString(R.string.edit_caption_fail));
        }
    }

    @Subscribe
    public void badgeNotiEvent(BadgeNotiEvent event){
        MasterPointService.getInstance().getBadgesAPI();
        MasterPointService.getInstance().showToolTips(getView().parentContainer, event.badgeName);
    }
}
