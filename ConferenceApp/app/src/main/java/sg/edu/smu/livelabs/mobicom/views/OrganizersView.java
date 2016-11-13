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
import sg.edu.smu.livelabs.mobicom.presenters.OrganizersPresenter;
import sg.edu.smu.livelabs.mobicom.presenters.screen.OrganizersScreenComponent;

/**
 * Created by smu on 28/4/16.
 */
@AutoInjector(OrganizersPresenter.class)
public class OrganizersView extends RelativeLayout {
    @Inject
    public OrganizersPresenter presenter;
    @Bind(R.id.viewPager)
    public NoneSwipeableViewPager viewPager;
    public OrganizersView(Context context, AttributeSet attrs) {
        super(context, attrs);
        DaggerService.<OrganizersScreenComponent>getDaggerComponent(context).inject(this);
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

        super.onFinishInflate();
        ButterKnife.bind(this);

    }
}
