package sg.edu.smu.livelabs.mobicom.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import javax.inject.Inject;

import autodagger.AutoInjector;
import butterknife.Bind;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import sg.edu.smu.livelabs.mobicom.DaggerService;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.presenters.SelfieProfilePresenter;
import sg.edu.smu.livelabs.mobicom.presenters.screen.SelfieProfileScreenComponent;

/**
 * Created by smu on 5/7/15.
 */
@AutoInjector(SelfieProfilePresenter.class)
public class SelfieProfileView extends RelativeLayout {

    @Inject
    SelfieProfilePresenter presenter;

    @Bind(R.id.avatar_image)
    public CircleImageView userProfileImage;

    @Bind(R.id.name)
    public TextView nameTV;

    @Bind(R.id.email)
    public TextView emailTV;

    @Bind(R.id.photoCount)
    public TextView photoCountTV;

    @Bind(R.id.share_text)
    public TextView shareText;
    @Bind(R.id.selfie_grid)
    public GridView gridView;

    @Bind(R.id.noitem)
    public TextView noItemTV;

    @Bind(R.id.name_short_form)
    public TextView nameShortFormTV;

    @Bind(R.id.upload_layout)
    public RelativeLayout uploadPhotoRL;

    public SelfieProfileView(Context context, AttributeSet attrs) {
        super(context, attrs);
        DaggerService.<SelfieProfileScreenComponent>getDaggerComponent(context).inject(this);
    }
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        presenter.takeView(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        presenter.dropView(this);
        super.onDetachedFromWindow();
    }

    @Override
    protected void onFinishInflate() {
        ButterKnife.bind(this);
        super.onFinishInflate();
    }
}
