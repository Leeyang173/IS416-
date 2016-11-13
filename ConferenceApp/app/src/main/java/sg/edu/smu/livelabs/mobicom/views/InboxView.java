package sg.edu.smu.livelabs.mobicom.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.marshalchen.ultimaterecyclerview.UltimateRecyclerView;

import javax.inject.Inject;

import autodagger.AutoInjector;
import butterknife.Bind;
import butterknife.ButterKnife;
import sg.edu.smu.livelabs.mobicom.DaggerService;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.UIHelper;
import sg.edu.smu.livelabs.mobicom.presenters.InboxPresenter;
import sg.edu.smu.livelabs.mobicom.presenters.screen.InboxScreenComponent;

/**
 * Created by smu on 30/5/16.
 */
@AutoInjector(InboxPresenter.class)
public class InboxView extends RelativeLayout {
    @Inject
    public InboxPresenter presenter;
    @Bind(R.id.mark_all_as_read_view)
    public TextView markAllAsReadBtn;
    @Bind(R.id.empty_view)
    public TextView emptyView;
    @Bind(R.id.list_view)
    public UltimateRecyclerView ultimateRecyclerView;

    public InboxView(Context context, AttributeSet attrs) {
        super(context, attrs);
        DaggerService.<InboxScreenComponent>getDaggerComponent(context).inject(this);
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
        UIHelper.getInstance().setBoldTypeface(emptyView);
        UIHelper.getInstance().setBoldTypeface(markAllAsReadBtn);
        markAllAsReadBtn.setOnClickListener(presenter);
    }
}
