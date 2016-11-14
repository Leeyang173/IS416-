package sg.edu.smu.livelabs.mobicom.views;

import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import javax.inject.Inject;

import autodagger.AutoInjector;
import butterknife.Bind;
import butterknife.ButterKnife;
import sg.edu.smu.livelabs.mobicom.DaggerService;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.presenters.NavigationPresenter;
import sg.edu.smu.livelabs.mobicom.presenters.screen.NavigationScreenComponent;

/**
 * Created by Jerms on 14/11/16.
 */
@AutoInjector(NavigationPresenter.class)
public class NavigationView extends RelativeLayout {

    @Inject
    public NavigationPresenter presenter;

    @Bind(R.id.message)
    public TextView messageTV;
    @Bind(R.id.arButton)
    public Button arButton;
    @Bind(R.id.locationSpinner)
    public Spinner locDDL;
    @Bind(R.id.textView)
    public TextView tv;

    public NavigationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        DaggerService.<NavigationScreenComponent>getDaggerComponent(context).inject(this);
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

