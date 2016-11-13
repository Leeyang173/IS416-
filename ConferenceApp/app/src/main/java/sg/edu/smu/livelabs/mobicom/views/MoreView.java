package sg.edu.smu.livelabs.mobicom.views;

import android.content.Context;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.AttributeSet;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import javax.inject.Inject;

import autodagger.AutoInjector;
import butterknife.Bind;
import butterknife.ButterKnife;
import sg.edu.smu.livelabs.mobicom.App;
import sg.edu.smu.livelabs.mobicom.DaggerService;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.UIHelper;
import sg.edu.smu.livelabs.mobicom.presenters.MorePresenter;
import sg.edu.smu.livelabs.mobicom.presenters.screen.MoreScreenComponent;

/**
 * Created by smu on 28/2/16.
 */
@AutoInjector(MorePresenter.class)
public class MoreView extends RelativeLayout{

    @Inject
    MorePresenter presenter;
    @Bind(R.id.more_grid)
    public GridView moreGridView;
    @Bind(R.id.version_tv)
    public TextView versionTV;
    @Bind(R.id.term_tv)
    public TextView termTV;
    @Bind(R.id.privacy_policy_tv)
    public TextView privacyPolicyTV;
//    @Bind(R.id.msr_tv)
//    public TextView msrTV;

    public MoreView(Context context, AttributeSet attrs) {
        super(context, attrs);
        DaggerService.<MoreScreenComponent>getDaggerComponent(context).inject(this);
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
        UIHelper.getInstance().setTypeface(termTV, privacyPolicyTV, versionTV);
        versionTV.setText(App.appName + " " + App.appVersion);

//        SpannableString content = new SpannableString("POWERED BY MICROSOFT EMBEDDED SOCIAL");
//        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
//        msrTV.setText(content);
    }
}
