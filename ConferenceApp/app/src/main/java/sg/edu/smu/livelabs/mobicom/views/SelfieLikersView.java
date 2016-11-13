package sg.edu.smu.livelabs.mobicom.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;
import android.widget.RelativeLayout;

import javax.inject.Inject;

import autodagger.AutoInjector;
import butterknife.Bind;
import butterknife.ButterKnife;
import sg.edu.smu.livelabs.mobicom.DaggerService;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.presenters.SelfieLikersPresenter;
import sg.edu.smu.livelabs.mobicom.presenters.screen.SelfieLikersScreenComponent;

/**
 * Created by smu on 12/11/15.
 */
@AutoInjector(SelfieLikersPresenter.class)
public class SelfieLikersView extends RelativeLayout {

    @Inject
    SelfieLikersPresenter presenter;
    @Bind(R.id.list_view)
    public ListView listView;

    public SelfieLikersView(Context context, AttributeSet attrs) {
        super(context, attrs);
        DaggerService.<SelfieLikersScreenComponent>getDaggerComponent(context).inject(this);
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
    }
}
