package sg.edu.smu.livelabs.mobicom.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import javax.inject.Inject;

import autodagger.AutoInjector;
import butterknife.Bind;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import sg.edu.smu.livelabs.mobicom.DaggerService;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.UIHelper;
import sg.edu.smu.livelabs.mobicom.models.User;
import sg.edu.smu.livelabs.mobicom.presenters.Profile1Presenter;
import sg.edu.smu.livelabs.mobicom.presenters.screen.Profile1ScreenComponent;
import sg.edu.smu.livelabs.mobicom.services.DatabaseService;

/**
 * Created by smu on 28/3/16.
 */
@AutoInjector(Profile1Presenter.class)
public class Profile1View extends RelativeLayout{
    @Inject
    public Profile1Presenter presenter;

    @Bind(R.id.information_layout)
    public RelativeLayout informationLayout;
    @Bind(R.id.avatar_txt)
    public TextView avatarTxt;
    @Bind(R.id.avatar_image)
    public CircleImageView avatarImage;
    @Bind(R.id.my_crop_image_view)
    public MyCropImageView myCropImageView;


    @Bind(R.id.instruction_view)
    public InstructionView instructionView;
    @Bind(R.id.name_txt)
    public TextView nameText;
    @Bind(R.id.name_edit_text)
    public EditText nameEditText;
    @Bind(R.id.organisation_txt)
    public TextView organisationTxt;
    @Bind(R.id.organisation_edit_text)
    public EditText organisationEditText;
    @Bind(R.id.next_btn)
    public Button nextBtn;

    public Profile1View(Context context, AttributeSet attrs) {
        super(context, attrs);
        DaggerService.<Profile1ScreenComponent>getDaggerComponent(context).inject(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        presenter.dropView(this);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        presenter.takeView(this);
    }

    @Override
    protected void onFinishInflate() {
        ButterKnife.bind(this);
        super.onFinishInflate();
        UIHelper uiHelper = UIHelper.getInstance();
        uiHelper.setTypeface(nameText, nameEditText, organisationEditText, organisationTxt, avatarTxt);
        uiHelper.setExo2BoldTypeFace(nextBtn);
        instructionView.setContent(getContext().getString(R.string.guide1_title),
                                    getContext().getString(R.string.guide1),
                                    50);
        User me = DatabaseService.getInstance().getMe();
        if (me != null){
            if (me.getName() == null || me.getName().isEmpty()){
                nameEditText.setText("");
            } else {
                nameEditText.setText(me.getName());
            }
            if (me.getSchool() == null || me.getSchool().isEmpty()){
                organisationEditText.setText("");
            } else {
                organisationEditText.setText(me.getSchool());
            }
        }

    }
}
