package sg.edu.smu.livelabs.mobicom.views;

import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import javax.inject.Inject;

import autodagger.AutoInjector;
import butterknife.Bind;
import butterknife.ButterKnife;
import sg.edu.smu.livelabs.mobicom.DaggerService;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.flow.FramePathContainerView;
import sg.edu.smu.livelabs.mobicom.presenters.ARNavigationPresenter;
import sg.edu.smu.livelabs.mobicom.presenters.screen.ARNavigationScreenComponent;

/**
 * Created by Jerms on 14/11/16.
 */
@AutoInjector(ARNavigationPresenter.class)
public class ARNavigationView extends RelativeLayout {

    @Inject
    public ARNavigationPresenter presenter;
    @Bind(R.id.mainLayout)
    public FrameLayout arLayout;

    public ARNavigationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        DaggerService.<ARNavigationScreenComponent>getDaggerComponent(context).inject(this);
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
