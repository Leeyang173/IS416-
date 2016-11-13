package sg.edu.smu.livelabs.mobicom.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.marshalchen.ultimaterecyclerview.UltimateRecyclerView;

import javax.inject.Inject;

import autodagger.AutoInjector;
import butterknife.Bind;
import butterknife.ButterKnife;
import sg.edu.smu.livelabs.mobicom.DaggerService;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.UIHelper;
import sg.edu.smu.livelabs.mobicom.presenters.SelfieHomePresenter;
import sg.edu.smu.livelabs.mobicom.presenters.screen.SelfieHomeScreenComponent;

/**
 * Created by smu on 26/10/15.
 */
@AutoInjector(SelfieHomePresenter.class)
public class SelfieHomeView extends RelativeLayout {
    @Inject
    public SelfieHomePresenter presenter;
    @Bind(R.id.image_list)
    public UltimateRecyclerView imageList;
    @Bind(R.id.no_image)
    public TextView noImage;

    @Bind(R.id.upload_layout)
    public RelativeLayout uploadPhotoRL;

    public SelfieHomeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        DaggerService.<SelfieHomeScreenComponent>getDaggerComponent(context).inject(this);
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
        UIHelper.getInstance().setTypeface(noImage);
    }
}
