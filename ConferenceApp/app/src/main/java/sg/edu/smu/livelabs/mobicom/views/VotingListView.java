package sg.edu.smu.livelabs.mobicom.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import javax.inject.Inject;

import autodagger.AutoInjector;
import butterknife.Bind;
import butterknife.ButterKnife;
import sg.edu.smu.livelabs.mobicom.DaggerService;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.presenters.VotingListPresenter;
import sg.edu.smu.livelabs.mobicom.presenters.screen.VotingListScreenComponent;

/**
 * Created by smu on 22/2/16.
 */
@AutoInjector(VotingListPresenter.class)
public class VotingListView extends LinearLayout {
    @Inject
    public VotingListPresenter presenter;

    @Bind(R.id.polling_list)
    public ListView pollingList;

    @Bind(R.id.no_internet_container)
    public RelativeLayout noInternetContainer;

    @Bind(R.id.internet_msg)
    public TextView internetTV;

    @Bind(R.id.empty_msg)
    public TextView msgTV;

    public VotingListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        DaggerService.<VotingListScreenComponent>getDaggerComponent(context).inject(this);
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
