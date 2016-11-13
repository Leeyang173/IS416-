package sg.edu.smu.livelabs.mobicom.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import javax.inject.Inject;

import autodagger.AutoInjector;
import butterknife.Bind;
import butterknife.ButterKnife;
import sg.edu.smu.livelabs.mobicom.DaggerService;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.presenters.VotingPresenter;
import sg.edu.smu.livelabs.mobicom.presenters.screen.VotingScreenComponent;

/**
 * Created by smu on 22/2/16.
 */
@AutoInjector(VotingPresenter.class)
public class VotingView extends LinearLayout {
    @Inject
    public VotingPresenter presenter;

    @Bind(R.id.message)
    public TextView messageTV;

    @Bind(R.id.chronometer)
    public Chronometer chronometer;

    @Bind(R.id.result)
    public TextView resultTV;

    @Bind(R.id.error_message)
    public TextView errorMsgTV;

    @Bind(R.id.title)
    public EditText titleTV;

    @Bind(R.id.questionGroup)
    public LinearLayout questionGroup;

    @Bind(R.id.logical_selection)
    public Button logicalRadioBtn;

    @Bind(R.id.multiple_selection)
    public Button mcqRadioBtn;

    @Bind(R.id.button)
    public Button button;

    @Bind(R.id.message_user)
    public TextView userMessageTV;

    @Bind(R.id.title_user)
    public TextView userTitleTV;

    @Bind(R.id.radio_group_user_one)
    public LinearLayout userRadioGroupLLOne;

    @Bind(R.id.radio_group_user_two)
    public LinearLayout userRadioGroupLLTwo;

    @Bind(R.id.key_note_container)
    public RelativeLayout keyNoteContainer;

    @Bind(R.id.user_container)
    public RelativeLayout userContainer;

    @Bind(R.id.no_internet_container)
    public RelativeLayout noInternetContainer;

    public VotingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        DaggerService.<VotingScreenComponent>getDaggerComponent(context).inject(this);
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
