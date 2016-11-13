package sg.edu.smu.livelabs.mobicom.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.webkit.WebView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import javax.inject.Inject;

import autodagger.AutoInjector;
import butterknife.Bind;
import butterknife.ButterKnife;
import sg.edu.smu.livelabs.mobicom.DaggerService;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.presenters.BingoPresenter;
import sg.edu.smu.livelabs.mobicom.presenters.QuizPresenter;
import sg.edu.smu.livelabs.mobicom.presenters.screen.BingoScreenComponent;

/**
 * Created by smu on 22/2/16.
 */
@AutoInjector(BingoPresenter.class)
public class BingoView extends LinearLayout {
    @Inject
    public BingoPresenter presenter;

    @Bind(R.id.bingo_grid)
    public GridView bingoGV;

    @Bind(R.id.description)
    public TextView description;

    @Bind(R.id.no_result_text)
    public TextView messageTV;

    @Bind(R.id.main_container)
    public LinearLayout mainContainer;

    @Bind(R.id.my_crop_image_view)
    public MyCropImageView myCropImageView;

    public BingoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        DaggerService.<BingoScreenComponent>getDaggerComponent(context).inject(this);
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
