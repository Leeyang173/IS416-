package sg.edu.smu.livelabs.mobicom.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import javax.inject.Inject;

import autodagger.AutoInjector;
import butterknife.Bind;
import butterknife.ButterKnife;
import sg.edu.smu.livelabs.mobicom.DaggerService;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.presenters.SurveyPresenter;
import sg.edu.smu.livelabs.mobicom.presenters.screen.SurveyScreenComponent;

/**
 * Created by smu on 22/2/16.
 */
@AutoInjector(SurveyPresenter.class)
public class SurveyView extends LinearLayout {
    @Inject
    public SurveyPresenter presenter;

    @Bind(R.id.survey_list)
    public ListView surveyLV;

    @Bind(R.id.message)
    public TextView messageTV;

    public SurveyView(Context context, AttributeSet attrs) {
        super(context, attrs);
        DaggerService.<SurveyScreenComponent>getDaggerComponent(context).inject(this);
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
