package sg.edu.smu.livelabs.mobicom.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import javax.inject.Inject;

import autodagger.AutoInjector;
import butterknife.Bind;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import me.kaede.tagview.TagView;
import sg.edu.smu.livelabs.mobicom.DaggerService;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.presenters.ProfilePresenter;
import sg.edu.smu.livelabs.mobicom.presenters.screen.ProfileScreenComponent;

/**
 * Created by smu on 22/2/16.
 */
@AutoInjector(ProfilePresenter.class)
public class ProfileView extends RelativeLayout {
    @Inject
    public ProfilePresenter presenter;


    @Bind(R.id.avatar_image)
    public CircleImageView profileCIV;

    @Bind(R.id.my_crop_image_view)
    public MyCropImageView myCropImageView;

    @Bind(R.id.full_name_text)
    public TextView nameTV;

    @Bind(R.id.name_short_form)
    public TextView nameShortFormTV;

    @Bind(R.id.institution_text)
    public TextView institutionTV;

    @Bind(R.id.designation_text)
    public TextView designationTV;

    @Bind(R.id.scroll_main_container)
    public ScrollView scrollMainContainer;

    @Bind(R.id.main_container)
    public LinearLayout mainContainer;

    @Bind(R.id.no_network_container)
    public LinearLayout noNetworkContainer;

    @Bind(R.id.badges_list)
    public CustomListView badgesLV;

    @Bind(R.id.tagview)
    public TagView tagView;


    public ProfileView(Context context, AttributeSet attrs) {
        super(context, attrs);
        DaggerService.<ProfileScreenComponent>getDaggerComponent(context).inject(this);
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
