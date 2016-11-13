package sg.edu.smu.livelabs.mobicom.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.searchView.SearchView;

import javax.inject.Inject;

import autodagger.AutoInjector;
import butterknife.Bind;
import butterknife.ButterKnife;
import me.kaede.tagview.TagView;
import sg.edu.smu.livelabs.mobicom.DaggerService;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.UIHelper;
import sg.edu.smu.livelabs.mobicom.presenters.AddGroupChatPresenter;
import sg.edu.smu.livelabs.mobicom.presenters.screen.AddGroupChatScreenComponent;

/**
 * Created by Aftershock PC on 27/7/2015.
 */
@AutoInjector(AddGroupChatPresenter.class)
public class AddGroupChatView extends RelativeLayout {
    @Inject
    AddGroupChatPresenter presenter;

    @Bind(R.id.scrollView)
    public ScrollView scrollView;
    @Bind(R.id.avatar_image)
    public ImageView avatarIV;
    @Bind(R.id.camera_avatar_btn)
    public ImageView cameraBtn;

    @Bind(R.id.title_txt)
    public EditText titleTxt;
    @Bind(R.id.member_lbl)
    public TextView memberLbl;
    @Bind(R.id.tagview)
    public TagView tagView;
    @Bind(R.id.friend_list_view)
    public ListView friendsList;
    @Bind(R.id.delete_btn)
    public Button deleteBtn;

    @Bind(R.id.search_view)
    public SearchView seachView;

    @Bind(R.id.my_crop_image_view)
    public MyCropImageView myCropImageView;
    @Bind(R.id.information_layout)
    public RelativeLayout mainLayout;

    public AddGroupChatView(Context context, AttributeSet attrs) {
        super(context, attrs);
        DaggerService.<AddGroupChatScreenComponent>getDaggerComponent(context).inject(this);
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
        UIHelper.getInstance().setTypeface(titleTxt, memberLbl);
        UIHelper.getInstance().setExo2TypeFace(deleteBtn, false);
        super.onFinishInflate();
    }
}