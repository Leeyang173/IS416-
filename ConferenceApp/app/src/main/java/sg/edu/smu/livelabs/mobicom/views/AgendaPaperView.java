package sg.edu.smu.livelabs.mobicom.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.marshalchen.ultimaterecyclerview.UltimateRecyclerView;

import javax.inject.Inject;

import autodagger.AutoInjector;
import butterknife.Bind;
import butterknife.ButterKnife;
import sg.edu.smu.livelabs.mobicom.DaggerService;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.presenters.AgendaPaperPresenter;
import sg.edu.smu.livelabs.mobicom.presenters.screen.AgendaPaperScreenComponent;

/**
 * Created by smu on 14/4/16.
 */
@AutoInjector(AgendaPaperPresenter.class)
public class AgendaPaperView extends RelativeLayout{
    @Inject AgendaPaperPresenter presenter;
    @Bind(R.id.paperListview)
    public UltimateRecyclerView papers;
    public AgendaPaperView(Context context, AttributeSet attrs) {
        super(context, attrs);
        DaggerService.<AgendaPaperScreenComponent>getDaggerComponent(context).inject(this);
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
    }
}
