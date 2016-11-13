package sg.edu.smu.livelabs.mobicom.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import javax.inject.Inject;

import autodagger.AutoInjector;
import butterknife.Bind;
import butterknife.ButterKnife;
import sg.edu.smu.livelabs.mobicom.DaggerService;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.presenters.ScavengerHuntPresenter;
import sg.edu.smu.livelabs.mobicom.presenters.screen.ScavengerHuntScreenComponent;

/**
 * Created by smu on 22/2/16.
 */
@AutoInjector(ScavengerHuntPresenter.class)
public class ScavengerHuntView extends RelativeLayout {
    @Inject
    public ScavengerHuntPresenter presenter;

    @Bind(R.id.scavenger_hunt_list)
    public CustomListView scavengerHuntList;

    @Bind(R.id.description)
    public TextView descriptionTV;

    @Bind(R.id.scroll_main_container)
    public ScrollView scrollView;

    public ScavengerHuntView(Context context, AttributeSet attrs) {
        super(context, attrs);
        DaggerService.<ScavengerHuntScreenComponent>getDaggerComponent(context).inject(this);
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
