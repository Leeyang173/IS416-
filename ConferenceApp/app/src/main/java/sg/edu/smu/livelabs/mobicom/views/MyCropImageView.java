package sg.edu.smu.livelabs.mobicom.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.isseiaoki.simplecropview.CropImageView;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.functions.Action1;
import sg.edu.smu.livelabs.mobicom.MainActivity;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.UIHelper;
import sg.edu.smu.livelabs.mobicom.fileupload.UploadFileService;

/**
 * Created by smu on 16/4/16.
 */
public class MyCropImageView extends FrameLayout implements View.OnClickListener{
    @Bind(R.id.cropImageView)
    public com.isseiaoki.simplecropview.CropImageView cropImageView;
    @Bind(R.id.change_image_button)
    public Button changeImageButton;
    @Bind(R.id.rotate_left_button)
    public Button rotateLeftBtn;
    @Bind(R.id.rotate_right_button)
    public Button rotateRightButton;
    @Bind(R.id.crop_button)
    public Button cropButton;
    @Bind(R.id.crop_layout)
    public RelativeLayout cropLayout;

    private Context context;
    private ReloadingImageListener listener;
    private MainActivity mainActivity;

    private Bitmap bitmap;
    private String filePath;


    public MyCropImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        LayoutInflater.from(context).inflate(R.layout.crop_image_view, this, true);
        ButterKnife.bind(this);
        bitmap = null;
        filePath = null;
        cropButton.setOnClickListener(this);
        rotateRightButton.setOnClickListener(this);
        rotateLeftBtn.setOnClickListener(this);
        changeImageButton.setOnClickListener(this);

    }

    public void setListener(ReloadingImageListener listener, MainActivity mainActivity, CropImageView.CropMode mode){
        this.listener = listener;
        this.mainActivity = mainActivity;
        cropImageView.setCropMode(mode);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id){
            case R.id.crop_button:
                done();
                break;
            case R.id.rotate_right_button:
                cropImageView.rotateImage(com.isseiaoki.simplecropview.CropImageView.RotateDegrees.ROTATE_90D);
                break;
            case R.id.rotate_left_button:
                cropImageView.rotateImage(com.isseiaoki.simplecropview.CropImageView.RotateDegrees.ROTATE_270D);
                break;
            case R.id.change_image_button:
                uploadImage();
                break;
        }
    }

    public void uploadImage(){
        UploadFileService.getInstance().requestPhoto(mainActivity, new Action1<String>() {
            @Override
            public void call(String filePath1) {
                filePath = filePath1;
                bitmap = UploadFileService.getInstance().getBitmap(filePath);
                if (bitmap == null || bitmap.isRecycled()){
                    UIHelper.getInstance().showAlert(context, context.getResources().getString(R.string.file_not_support));
                    return;
                }
                cropLayout.setVisibility(View.VISIBLE);
                cropImageView.setImageBitmap(bitmap);
                listener.hideMainLayout();
            }
        });
    }

    public void done(){
        try {
            if (filePath == null || bitmap == null) {
                listener.setNewImage("");
                return;
            } else {
                bitmap = cropImageView.getCroppedBitmap();
                UploadFileService.getInstance().uploadPhoto(mainActivity, filePath, bitmap,
                        new Action1<String>() {
                            @Override
                            public void call(String imageId) {
                                listener.setNewImage(imageId);
                                cropLayout.setVisibility(View.GONE);
                                if (bitmap != null){
                                    bitmap.recycle();
                                    bitmap = null;
                                }
                            }
                        });
            }
        }catch (Exception e){
            UIHelper.getInstance().showAlert(context, context.getResources().getString(R.string.upload_fail));
            e.printStackTrace();
        }
    }

    public interface ReloadingImageListener{
        void setNewImage(String imageID);
        void hideMainLayout();
    }
}
