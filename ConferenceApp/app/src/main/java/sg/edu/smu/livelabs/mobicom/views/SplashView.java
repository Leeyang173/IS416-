package sg.edu.smu.livelabs.mobicom.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import javax.inject.Inject;

import autodagger.AutoInjector;
import butterknife.Bind;
import butterknife.ButterKnife;
import sg.edu.smu.livelabs.mobicom.DaggerService;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.UIHelper;
import sg.edu.smu.livelabs.mobicom.presenters.SplashPresenter;
import sg.edu.smu.livelabs.mobicom.presenters.screen.SplashScreenComponent;

/**
 * Created by smu on 21/1/16.
 */
@AutoInjector(SplashPresenter.class)
public class SplashView extends RelativeLayout {
    @Inject
    SplashPresenter presenter;

    @Bind(R.id.background_image)
    public ImageView bgIV;

    public SplashView(Context context, AttributeSet attrs) {
        super(context, attrs);
        DaggerService.<SplashScreenComponent>getDaggerComponent(context).inject(this);
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
        UIHelper uiHelper = UIHelper.getInstance();
    }

}
