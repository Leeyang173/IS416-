package sg.edu.smu.livelabs.mobicom.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import javax.inject.Inject;

import autodagger.AutoInjector;
import butterknife.Bind;
import butterknife.ButterKnife;
import sg.edu.smu.livelabs.mobicom.DaggerService;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.presenters.StumpListPresenter;
import sg.edu.smu.livelabs.mobicom.presenters.screen.StumpListScreenComponent;

/**
 * Created by smu on 22/2/16.
 */
@AutoInjector(StumpListPresenter.class)
public class StumpListView extends LinearLayout {
    @Inject
    public StumpListPresenter presenter;

    @Bind(R.id.stump_list)
    public ListView stumpLV;

    @Bind(R.id.scroll_main_container)
    public ScrollView scrollContainer;

    @Bind(R.id.description)
    public TextView description;

    @Bind(R.id.no_result_text)
    public TextView noResultTV;

    public StumpListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        DaggerService.<StumpListScreenComponent>getDaggerComponent(context).inject(this);
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
