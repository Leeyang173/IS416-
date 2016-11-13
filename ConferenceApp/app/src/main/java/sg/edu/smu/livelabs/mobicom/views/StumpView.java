package sg.edu.smu.livelabs.mobicom.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import javax.inject.Inject;

import autodagger.AutoInjector;
import butterknife.Bind;
import butterknife.ButterKnife;
import sg.edu.smu.livelabs.mobicom.DaggerService;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.presenters.StumpPresenter;
import sg.edu.smu.livelabs.mobicom.presenters.screen.StumpScreenComponent;

/**
 * Created by smu on 22/2/16.
 */
@AutoInjector(StumpPresenter.class)
public class StumpView extends LinearLayout {
    @Inject
    public StumpPresenter presenter;

    @Bind(R.id.leaderboard)
    public ListView leaderboardLV;

    @Bind(R.id.question_container)
    public LinearLayout questionContainer;

    @Bind(R.id.overlay_msg)
    public TextView overlayMsg;

    @Bind(R.id.score)
    public TextView scoreTV;

    @Bind(R.id.overlay)
    public LinearLayout overlay;

    @Bind(R.id.question)
    public RelativeLayout question;

    @Bind(R.id.message)
    public TextView messageTV;

    @Bind(R.id.scroll_container)
    public ScrollView scrollView;

    @Bind(R.id.container)
    public LinearLayout container;

    @Bind(R.id.score_container)
    public LinearLayout scoreContainer;

    @Bind(R.id.line)
    public View line;

    public StumpView(Context context, AttributeSet attrs) {
        super(context, attrs);
        DaggerService.<StumpScreenComponent>getDaggerComponent(context).inject(this);
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
