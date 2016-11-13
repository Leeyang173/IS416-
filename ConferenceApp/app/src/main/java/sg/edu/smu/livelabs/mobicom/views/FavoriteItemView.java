package sg.edu.smu.livelabs.mobicom.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import javax.inject.Inject;

import autodagger.AutoInjector;
import butterknife.Bind;
import butterknife.ButterKnife;
import sg.edu.smu.livelabs.mobicom.DaggerService;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.presenters.FavoriteItemPresenter;
import sg.edu.smu.livelabs.mobicom.presenters.screen.FavoriteItemScreenComponent;

/**
 * Created by smu on 26/10/15.
 */
@AutoInjector(FavoriteItemPresenter.class)
public class FavoriteItemView extends RelativeLayout {
    @Inject
    public FavoriteItemPresenter presenter;

    @Bind(R.id.items)
    public GridView itemGV;

    @Bind(R.id.title)
    public TextView titleTV;

    @Bind(R.id.top)
    public ImageView topIV;

    @Bind(R.id.container)
    public LinearLayout containerLL;

//    @Bind(R.id.container)
//    public ScrollView containerLL;

    @Bind(R.id.no_result_text)
    public TextView noResult;

    public FavoriteItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        DaggerService.<FavoriteItemScreenComponent>getDaggerComponent(context).inject(this);
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
