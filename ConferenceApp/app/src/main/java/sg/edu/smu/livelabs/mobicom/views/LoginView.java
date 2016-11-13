package sg.edu.smu.livelabs.mobicom.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import javax.inject.Inject;

import autodagger.AutoInjector;
import butterknife.Bind;
import butterknife.ButterKnife;
import me.grantland.widget.AutofitTextView;
import sg.edu.smu.livelabs.mobicom.DaggerService;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.UIHelper;
import sg.edu.smu.livelabs.mobicom.presenters.LoginPresenter;
//import sg.edu.smu.livelabs.mobicom.presenters.screen.LoginScreenComponent;

/**
 * Created by smu on 21/3/16.
 */
@AutoInjector(LoginPresenter.class)
public class LoginView extends RelativeLayout{
    @Inject
    public LoginPresenter presenter;

    @Bind(R.id.welcome)
    public TextView welcomeTxt;
    @Bind(R.id.singapore2016)
    public TextView singaporeTxt;

    @Bind(R.id.login_step1)
    public RelativeLayout login1Layout;
    @Bind(R.id.login_step2)
    public RelativeLayout login2Layout;
    @Bind(R.id.email_edit_text)
    public EditText emailTxt;
    @Bind(R.id.next_btn)
    public Button nextBtn;
    @Bind(R.id.enter_email_txt)
    public AutofitTextView enterEmailTxt;
    @Bind(R.id.password_edit_txt)
    public EditText passText;
    @Bind(R.id.confirm_btn)
    public Button confirmBtn;

    public LoginView(Context context, AttributeSet attrs) {
        super(context, attrs);
//        DaggerService.<LoginScreenComponent>getDaggerComponent(context).inject(this);
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
        uiHelper.setBoldTypeface(welcomeTxt);
        uiHelper.setBoldTypeface(singaporeTxt);
        uiHelper.setTypeface(emailTxt, passText, enterEmailTxt);
        uiHelper.setExo2BoldTypeFace(nextBtn, confirmBtn);
    }
}
