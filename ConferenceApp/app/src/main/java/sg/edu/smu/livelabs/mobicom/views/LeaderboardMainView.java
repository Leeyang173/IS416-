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
import sg.edu.smu.livelabs.mobicom.presenters.LeaderboardMainPresenter;
import sg.edu.smu.livelabs.mobicom.presenters.screen.LeaderboardMainScreenComponent;

/**
 * Created by smu on 4/4/16.
 */
@AutoInjector(LeaderboardMainPresenter.class)
public class LeaderboardMainView extends RelativeLayout {
    @Inject
    public LeaderboardMainPresenter presenter;
    @Bind(R.id.viewPager)
    public NoneSwipeableViewPager viewPager;

    public LeaderboardMainView(Context context, AttributeSet attrs) {
        super(context, attrs);
        DaggerService.<LeaderboardMainScreenComponent>getDaggerComponent(context).inject(this);
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
