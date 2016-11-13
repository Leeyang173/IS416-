package sg.edu.smu.livelabs.mobicom.views;

import android.content.Context;
import android.util.AttributeSet;
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
import sg.edu.smu.livelabs.mobicom.presenters.EVAPromotionPresenter;
import sg.edu.smu.livelabs.mobicom.presenters.screen.EVAPromotionScreenComponent;

/**
 * Created by smu on 27/10/15.
 */
@AutoInjector(EVAPromotionPresenter.class)
public class EVAPromotionView extends RelativeLayout {
    @Inject
    public EVAPromotionPresenter presenter;
    @Bind(R.id.choose_tem)
    public TextView chooseItem;
    @Bind(R.id.list_view)
    public ListView listView;
    @Bind(R.id.no_result_text)
    public TextView noResult;
    @Bind(R.id.description)
    public TextView descriptionTV;

    public EVAPromotionView(Context context, AttributeSet attrs) {
        super(context, attrs);
        DaggerService.<EVAPromotionScreenComponent>getDaggerComponent(context).inject(this);
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
        UIHelper.getInstance().setTypeface(chooseItem, noResult);
    }
}
