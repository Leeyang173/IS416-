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
import sg.edu.smu.livelabs.mobicom.presenters.AgendaTabPresenter;
import sg.edu.smu.livelabs.mobicom.presenters.screen.AgendaTabScreenComponent;

/**
 * Created by smu on 25/2/16.
 */
@AutoInjector(AgendaTabPresenter.class)
public class AgendaTabView extends RelativeLayout {
    @Inject
    AgendaTabPresenter presenter;

    @Bind(R.id.recycleView)
    public UltimateRecyclerView ultimateRecyclerView;
    @Bind(R.id.empty_tv)
    public TextView emptyTV;

    public AgendaTabView(Context context, AttributeSet attrs) {
        super(context, attrs);
        DaggerService.<AgendaTabScreenComponent>getDaggerComponent(context).inject(this);
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
        UIHelper.getInstance().setBoldTypeface(emptyTV);
    }


}
