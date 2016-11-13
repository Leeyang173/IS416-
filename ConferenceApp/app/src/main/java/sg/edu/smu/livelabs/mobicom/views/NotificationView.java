package sg.edu.smu.livelabs.mobicom.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import javax.inject.Inject;

import autodagger.AutoInjector;
import butterknife.Bind;
import butterknife.ButterKnife;
import sg.edu.smu.livelabs.mobicom.DaggerService;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.UIHelper;
import sg.edu.smu.livelabs.mobicom.presenters.NotificationPresenter;
import sg.edu.smu.livelabs.mobicom.presenters.screen.NotificationScreenComponent;

/**
 * Created by smu on 1/6/16.
 */
@AutoInjector(NotificationPresenter.class)
public class NotificationView extends LinearLayout{
    @Inject
    public NotificationPresenter presenter;
    @Bind(R.id.select_userpool_btn)
    public TextView selectUserpoolBtn;
    @Bind(R.id.message_edit_text)
    public EditText message;

    public NotificationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        DaggerService.<NotificationScreenComponent>getDaggerComponent(context).inject(this);
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
        UIHelper.getInstance().setExo2BoldTypeFace(selectUserpoolBtn);
    }
}
