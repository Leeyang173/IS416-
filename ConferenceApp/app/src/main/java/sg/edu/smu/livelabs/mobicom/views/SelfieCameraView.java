package sg.edu.smu.livelabs.mobicom.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.isseiaoki.simplecropview.CropImageView;

import javax.inject.Inject;

import autodagger.AutoInjector;
import butterknife.Bind;
import butterknife.ButterKnife;
import flow.Flow;
import sg.edu.smu.livelabs.mobicom.DaggerService;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.UIHelper;
import sg.edu.smu.livelabs.mobicom.flow.HandlesBack;
import sg.edu.smu.livelabs.mobicom.presenters.SelfieCameraPresenter;
import sg.edu.smu.livelabs.mobicom.presenters.screen.SelfieCameraScreenComponent;

/**
 * Created by smu on 5/11/15.
 */
@AutoInjector(SelfieCameraPresenter.class)
public class SelfieCameraView extends LinearLayout implements HandlesBack {
    @Inject
    SelfieCameraPresenter presenter;

    @Bind(R.id.description_text)
    public EditText description;
    @Bind(R.id.cropImageView)
    public CropImageView cropImageView;
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
    @Bind(R.id.image)
    public ImageView photo;
    @Bind(R.id.parent)
    public LinearLayout parentContainer;

    public SelfieCameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
        DaggerService.<SelfieCameraScreenComponent>getDaggerComponent(context).inject(this);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        presenter.takeView(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        presenter.dropView(this);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
        UIHelper.getInstance().setTypeface(description);
    }


    @Override
    public boolean onBackPressed() {
        Context context = this.getContext();
        description.clearFocus();
        InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(description.getWindowToken(), 0);
        Flow.get(context).goBack();
        return true;
    }
}
