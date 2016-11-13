package sg.edu.smu.livelabs.mobicom.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.marshalchen.ultimaterecyclerview.UltimateRecyclerView;

import javax.inject.Inject;

import autodagger.AutoInjector;
import butterknife.Bind;
import butterknife.ButterKnife;
import sg.edu.smu.livelabs.mobicom.DaggerService;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.presenters.MemoriesListPresenter;
import sg.edu.smu.livelabs.mobicom.presenters.screen.MemoriesListScreenComponent;

/**
 * Created by smu on 3/11/15.
 */
@AutoInjector(MemoriesListPresenter.class)
public class MemoriesListView extends LinearLayout {
    @Inject
    MemoriesListPresenter presenter;

    @Bind(R.id.list_view)
    public UltimateRecyclerView ultimateRecyclerView;

    @Bind(R.id.date_title)
    public TextView dateTitle;

    @Bind(R.id.upload_layout)
    public RelativeLayout uploadPhotoRL;

    public MemoriesListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        DaggerService.<MemoriesListScreenComponent>getDaggerComponent(context).inject(this);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
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
}
