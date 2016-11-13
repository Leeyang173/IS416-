package sg.edu.smu.livelabs.mobicom.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import javax.inject.Inject;

import autodagger.AutoInjector;
import butterknife.Bind;
import butterknife.ButterKnife;
import sg.edu.smu.livelabs.mobicom.DaggerService;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.presenters.AttendeesPresenter;
import sg.edu.smu.livelabs.mobicom.presenters.screen.AttendeesScreenComponent;

/**
 * Created by smu on 22/2/16.
 */
@AutoInjector(AttendeesPresenter.class)
public class AttendeesView extends LinearLayout{
    @Inject
    public AttendeesPresenter presenter;

    @Bind(R.id.viewPager)
    public NoneSwipeableViewPager viewPager;

    public AttendeesView(Context context, AttributeSet attrs) {
        super(context, attrs);
        DaggerService.<AttendeesScreenComponent>getDaggerComponent(context).inject(this);
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
