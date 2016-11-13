package sg.edu.smu.livelabs.mobicom.views;

import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import javax.inject.Inject;

import autodagger.AutoInjector;
import butterknife.Bind;
import butterknife.ButterKnife;
import sg.edu.smu.livelabs.mobicom.DaggerService;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.presenters.SponsorPresenter;
import sg.edu.smu.livelabs.mobicom.presenters.screen.SponsorScreenComponent;

/**
 * Created by smu on 28/4/16.
 */
@AutoInjector(SponsorPresenter.class)
public class SponsorView extends RelativeLayout{

    @Inject
    public SponsorPresenter presenter;
    @Bind(R.id.webview)
    public WebView webView;

    @Bind(R.id.message)
    public TextView messageTV;

    @Bind(R.id.progress_WV)
    public ProgressBar progressBar;
    public SponsorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        DaggerService.<SponsorScreenComponent>getDaggerComponent(context).inject(this);
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
