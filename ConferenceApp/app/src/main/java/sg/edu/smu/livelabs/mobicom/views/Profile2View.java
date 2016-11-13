package sg.edu.smu.livelabs.mobicom.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import javax.inject.Inject;

import autodagger.AutoInjector;
import butterknife.Bind;
import butterknife.ButterKnife;
import sg.edu.smu.livelabs.mobicom.DaggerService;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.UIHelper;
import sg.edu.smu.livelabs.mobicom.presenters.Profile2Presenter;
import sg.edu.smu.livelabs.mobicom.presenters.screen.Profile2ScreenComponent;

/**
 * Created by smu on 26/4/16.
 */
@AutoInjector(Profile2Presenter.class)
public class Profile2View extends RelativeLayout{
    @Inject
    public Profile2Presenter presenter;
    @Bind(R.id.done_btn)
    public Button doneBtn;
    @Bind(R.id.register_tv)
    public TextView registerTV;
    @Bind(R.id.select_txt)
    public TextView selectTV;
    @Bind(R.id.listview)
    public ListView listView;

    @Bind(R.id.instruction_view)
    public InstructionView instructionView;

    public Profile2View(Context context, AttributeSet attrs) {
        super(context, attrs);
        DaggerService.<Profile2ScreenComponent>getDaggerComponent(context).inject(this);
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
        UIHelper.getInstance().setTypeface(registerTV, selectTV);
        UIHelper.getInstance().setBoldTypeface(doneBtn);
        instructionView.setContent(getContext().getString(R.string.guide2_title),
                getContext().getString(R.string.guide2),
                50);
    }
}
