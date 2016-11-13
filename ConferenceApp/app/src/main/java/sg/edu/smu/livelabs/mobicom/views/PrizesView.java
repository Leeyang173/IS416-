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
import sg.edu.smu.livelabs.mobicom.presenters.PrizesPresenter;
import sg.edu.smu.livelabs.mobicom.presenters.screen.PrizesScreenComponent;

/**
 * Created by smu on 22/2/16.
 */
@AutoInjector(PrizesPresenter.class)
public class PrizesView extends LinearLayout {
    @Inject
    public PrizesPresenter presenter;

    @Bind(R.id.message)
    public TextView message;

    @Bind(R.id.progress_WV)
    public ProgressBar progressBar;

    @Bind(R.id.webView)
    public WebView webView;

    public PrizesView(Context context, AttributeSet attrs) {
        super(context, attrs);
        DaggerService.<PrizesScreenComponent>getDaggerComponent(context).inject(this);
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
