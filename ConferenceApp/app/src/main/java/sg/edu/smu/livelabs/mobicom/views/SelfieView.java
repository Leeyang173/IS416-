package sg.edu.smu.livelabs.mobicom.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import javax.inject.Inject;

import autodagger.AutoInjector;
import butterknife.Bind;
import butterknife.ButterKnife;
import sg.edu.smu.livelabs.mobicom.DaggerService;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.flow.HandlesBack;
import sg.edu.smu.livelabs.mobicom.presenters.SelfiePresenter;
import sg.edu.smu.livelabs.mobicom.presenters.screen.SelfieScreenComponent;

/**
 * Created by smu on 26/10/15.
 */
@AutoInjector(SelfiePresenter.class)
public class SelfieView extends RelativeLayout implements HandlesBack {
    @Inject
    public SelfiePresenter presenter;
    @Bind(R.id.viewpager)
    public NoneSwipeableViewPager pager;

    public SelfieView(Context context, AttributeSet attrs) {
        super(context, attrs);
        DaggerService.<SelfieScreenComponent>getDaggerComponent(context).inject(this);
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
    }

    @Override
    public boolean onBackPressed() {
        return presenter.goBack();
    }
}
