package sg.edu.smu.livelabs.mobicom.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import javax.inject.Inject;

import autodagger.AutoInjector;
import butterknife.Bind;
import butterknife.ButterKnife;
import sg.edu.smu.livelabs.mobicom.DaggerService;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.UIHelper;
import sg.edu.smu.livelabs.mobicom.presenters.Login2Presenter;
import sg.edu.smu.livelabs.mobicom.presenters.screen.Login2ScreenComponent;

/**
 * Created by smu on 26/4/16.
 */
@AutoInjector(Login2Presenter.class)
public class Login2View extends RelativeLayout{
    @Inject
    public Login2Presenter presenter;

    @Bind(R.id.scan_the_qr_tv)
    public TextView scanQRTV;
    @Bind(R.id.qr_code_im)
    public ImageView scanBtn;

    public Login2View(Context context, AttributeSet attrs) {
        super(context, attrs);
        DaggerService.<Login2ScreenComponent>getDaggerComponent(getContext()).inject(this);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        presenter.takeView(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        presenter.dropView(this);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
        UIHelper uiHelper = UIHelper.getInstance();
        uiHelper.setTypeface(scanQRTV);
    }
}
