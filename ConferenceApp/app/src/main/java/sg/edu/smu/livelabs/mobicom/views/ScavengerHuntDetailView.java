package sg.edu.smu.livelabs.mobicom.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import javax.inject.Inject;

import autodagger.AutoInjector;
import butterknife.Bind;
import butterknife.ButterKnife;
import sg.edu.smu.livelabs.mobicom.DaggerService;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.presenters.ScavengerHuntDetailPresenter;
import sg.edu.smu.livelabs.mobicom.presenters.screen.ScavengerHuntDetailScreenComponent;

/**
 * Created by smu on 22/2/16.
 */
@AutoInjector(ScavengerHuntDetailPresenter.class)
public class ScavengerHuntDetailView extends LinearLayout {
    @Inject
    public ScavengerHuntDetailPresenter presenter;

    @Bind(R.id.hint)
    public TextView hintTV;

    @Bind(R.id.description)
    public TextView descriptionTV;

    @Bind(R.id.team_member_header)
    public TextView teamMemberHeader;

    @Bind(R.id.container)
    public RelativeLayout containerRL;

    @Bind(R.id.team_member_grid)
    public CustomGridView teamMemberGV;

    @Bind(R.id.photo_hints)
    public ImageView photoHintsTV;

    @Bind(R.id.scan)
    public Button scanBtn;

    @Bind(R.id.disband)
    public Button disbandBtn;

    @Bind(R.id.start)
    public Button startBtn;

    @Bind(R.id.before_starting)
    public LinearLayout beforeStartingLL;

    @Bind(R.id.after_starting)
    public LinearLayout afterStartingLL;


    public ScavengerHuntDetailView(Context context, AttributeSet attrs) {
        super(context, attrs);
        DaggerService.<ScavengerHuntDetailScreenComponent>getDaggerComponent(context).inject(this);
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
