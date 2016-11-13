package sg.edu.smu.livelabs.mobicom.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;
import android.widget.LinearLayout;

import javax.inject.Inject;

import autodagger.AutoInjector;
import butterknife.Bind;
import butterknife.ButterKnife;
import sg.edu.smu.livelabs.mobicom.DaggerService;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.UIHelper;
import sg.edu.smu.livelabs.mobicom.presenters.FeedbackPresenter;
import sg.edu.smu.livelabs.mobicom.presenters.screen.FeedbackScreenComponent;

/**
 * Created by smu on 26/5/16.
 */
@AutoInjector(FeedbackPresenter.class)
public class FeedbackView extends LinearLayout {
    @Inject
    public FeedbackPresenter presenter;

    @Bind(R.id.type_text)
    public EditText type;
    @Bind(R.id.content_text)
    public EditText content;

    public FeedbackView(Context context, AttributeSet attrs) {
        super(context, attrs);
        DaggerService.<FeedbackScreenComponent>getDaggerComponent(context).inject(this);
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
        UIHelper.getInstance().setTypeface(type, content);
    }
}
