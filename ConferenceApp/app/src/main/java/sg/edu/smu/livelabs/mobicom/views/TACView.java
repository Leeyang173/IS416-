package sg.edu.smu.livelabs.mobicom.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
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
import flow.Flow;
import sg.edu.smu.livelabs.mobicom.App;
import sg.edu.smu.livelabs.mobicom.DaggerService;
import sg.edu.smu.livelabs.mobicom.MainActivity;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.UIHelper;
import sg.edu.smu.livelabs.mobicom.flow.HandlesBack;
import sg.edu.smu.livelabs.mobicom.presenters.TACPresenter;
import sg.edu.smu.livelabs.mobicom.presenters.screen.IRBScreen;
import sg.edu.smu.livelabs.mobicom.presenters.screen.TACScreenComponent;

/**
 * Created by smu (Chau) on 15/6/16.
 */
@AutoInjector(TACPresenter.class)
public class TACView extends RelativeLayout implements HandlesBack {
    @Inject
    public TACPresenter presenter;

    @Bind(R.id.no_result_tv)
    public TextView noResultTV;
    @Bind(R.id.agree_btn)
    public TextView agreeBtn;
    @Bind(R.id.webview)
    public WebView webView;
    @Bind(R.id.message)
    public TextView messageTV;

    @Bind(R.id.progress_WV)
    public ProgressBar progressBar;
    public TACView(Context context, AttributeSet attrs) {
        super(context, attrs);
        DaggerService.<TACScreenComponent>getDaggerComponent(context).inject(this);
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
        webView = (WebView) findViewById(R.id.webview);

        UIHelper.getInstance().setExo2BoldTypeFace(agreeBtn);
        UIHelper.getInstance().setBoldTypeface(noResultTV);
        agreeBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MainActivity.mode == MainActivity.OFFLINE_MODE){
                    UIHelper.getInstance().showAlert(getContext(), "Opp! Please check your internet connection to load Personal Data Protection Policy.");
                } else {
                    SharedPreferences sharedPreferences = getContext().getSharedPreferences(MainActivity.SHARE_PREFERENCES, Context.MODE_APPEND);
                    sharedPreferences.edit().putBoolean("TAC_accept", true).commit();
                    Flow.get(getContext()).set(new IRBScreen());
                }
            }
        });
    }

    @Override
    public boolean onBackPressed() {
        if (webView.canGoBack()){
            webView.goBack();
            return true;
        } else {
            App.getInstance().getMainActivity().finish();
            return false;
        }
    }
}
