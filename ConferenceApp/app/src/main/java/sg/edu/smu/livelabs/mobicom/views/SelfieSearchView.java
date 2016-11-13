package sg.edu.smu.livelabs.mobicom.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import javax.inject.Inject;

import autodagger.AutoInjector;
import butterknife.Bind;
import butterknife.ButterKnife;
import sg.edu.smu.livelabs.mobicom.DaggerService;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.UIHelper;
import sg.edu.smu.livelabs.mobicom.presenters.SelfieSearchPresenter;
import sg.edu.smu.livelabs.mobicom.presenters.screen.SelfieSearchScreenComponent;

/**
 * Created by smu on 5/7/15.
 */
@AutoInjector(SelfieSearchPresenter.class)
public class SelfieSearchView extends RelativeLayout {

    @Inject
    SelfieSearchPresenter presenter;

    @Bind(R.id.search_edit_Text)
    public EditText searchEdit;
    @Bind(R.id.search_btn)
    public RelativeLayout searchBtn;

    @Bind(R.id.search_result_list)
    public ListView listView;
    @Bind(R.id.no_result_text)
    public TextView noResultText;

    public SelfieSearchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        DaggerService.<SelfieSearchScreenComponent>getDaggerComponent(context).inject(this);
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
        UIHelper.getInstance().setTypeface(noResultText);
    }
}
