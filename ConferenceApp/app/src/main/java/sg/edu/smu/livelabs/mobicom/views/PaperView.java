package sg.edu.smu.livelabs.mobicom.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.marshalchen.ultimaterecyclerview.UltimateRecyclerView;

import javax.inject.Inject;

import autodagger.AutoInjector;
import butterknife.Bind;
import butterknife.ButterKnife;
import sg.edu.smu.livelabs.mobicom.DaggerService;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.presenters.PaperPresenter;
import sg.edu.smu.livelabs.mobicom.presenters.screen.PaperScreenComponent;

/**
 * Created by smu on 28/2/16.
 */
@AutoInjector(PaperPresenter.class)
public class PaperView extends LinearLayout {
    @Inject
    PaperPresenter presenter;

    @Bind(R.id.paperListview)
    public UltimateRecyclerView papers;

    public PaperView(Context context, AttributeSet attrs) {
        super(context, attrs);
        DaggerService.<PaperScreenComponent>getDaggerComponent(context).inject(this);
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
