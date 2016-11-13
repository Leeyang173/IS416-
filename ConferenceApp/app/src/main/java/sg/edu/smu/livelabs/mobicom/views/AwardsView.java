package sg.edu.smu.livelabs.mobicom.views;

import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import javax.inject.Inject;

import autodagger.AutoInjector;
import butterknife.Bind;
import butterknife.ButterKnife;
import sg.edu.smu.livelabs.mobicom.DaggerService;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.presenters.AwardsPresenter;
import sg.edu.smu.livelabs.mobicom.presenters.screen.AwardsScreenComponent;

/**
 * Created by smu on 22/2/16.
 */
@AutoInjector(AwardsPresenter.class)
public class AwardsView extends LinearLayout {
    @Inject
    public AwardsPresenter presenter;

    @Bind(R.id.webView)
    public WebView surveyWV;

    @Bind(R.id.message)
    public TextView messageTV;

    @Bind(R.id.progress_WV)
    public ProgressBar progressBar;

    public AwardsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        DaggerService.<AwardsScreenComponent>getDaggerComponent(context).inject(this);
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
