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
import sg.edu.smu.livelabs.mobicom.presenters.MessagePresenter;
import sg.edu.smu.livelabs.mobicom.presenters.screen.MessageScreenComponent;

/**
 * Created by smu on 4/4/16.
 */
@AutoInjector(MessagePresenter.class)
public class MessageView extends RelativeLayout implements HandlesBack{
    @Inject
    public MessagePresenter presenter;
    @Bind(R.id.viewPager)
    public NoneSwipeableViewPager viewPager;

    public MessageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        DaggerService.<MessageScreenComponent>getDaggerComponent(context).inject(this);
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

    @Override
    public boolean onBackPressed() {
        return presenter.canGoback();
    }
}
