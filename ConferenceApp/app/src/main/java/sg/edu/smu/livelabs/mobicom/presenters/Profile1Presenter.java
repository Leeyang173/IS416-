package sg.edu.smu.livelabs.mobicom.presenters;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import com.isseiaoki.simplecropview.CropImageView;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import autodagger.AutoComponent;
import automortar.AutoScreen;
import flow.Flow;
import mortar.MortarScope;
import mortar.ViewPresenter;
import sg.edu.smu.livelabs.mobicom.AppDependencies;
import sg.edu.smu.livelabs.mobicom.DaggerScope;
import sg.edu.smu.livelabs.mobicom.MainActivity;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.UIHelper;
import sg.edu.smu.livelabs.mobicom.busEvents.BadgeNotiEvent;
import sg.edu.smu.livelabs.mobicom.fileupload.UploadFileService;
import sg.edu.smu.livelabs.mobicom.flow.Layout;
import sg.edu.smu.livelabs.mobicom.models.User;
import sg.edu.smu.livelabs.mobicom.presenters.screen.Profile2Screen;
import sg.edu.smu.livelabs.mobicom.services.ChatService;
import sg.edu.smu.livelabs.mobicom.services.DatabaseService;
import sg.edu.smu.livelabs.mobicom.services.MasterPointService;
import sg.edu.smu.livelabs.mobicom.views.MyCropImageView;
import sg.edu.smu.livelabs.mobicom.views.Profile1View;

/**
 * Created by smu on 28/3/16.
 */
@AutoScreen(
        component = @AutoComponent(dependencies = MainActivity.class, superinterfaces = AppDependencies.class),
        screenAnnotations = Layout.class
)
@DaggerScope(Profile1Presenter.class)
@Layout(R.layout.profile_1_view)
public class Profile1Presenter extends ViewPresenter<Profile1View> implements View.OnClickListener,
        MyCropImageView.ReloadingImageListener {
    private Context context;
    private MainActivity mainActivity;
    private String avatarId;
    private Bus bus;

    public Profile1Presenter(MainActivity mainActivity, Bus bus){
        this.mainActivity = mainActivity;
        this.bus = bus;
    }

    @Override
    protected void onLoad(Bundle savedInstanceState)  {
        super.onLoad(savedInstanceState);
        if (!hasView()){
            return;
        }
        context = getView().getContext();
        User me = DatabaseService.getInstance().getMe();
        avatarId = me.getAvatar();
        getView().avatarImage.setOnClickListener(this);
        UploadFileService.getInstance().loadImage(getView().avatarImage, R.drawable.icon_profile_photo,
                avatarId, 96);
        getView().nextBtn.setOnClickListener(this);
        getView().myCropImageView.setListener(this, mainActivity, CropImageView.CropMode.CIRCLE);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id){
            case R.id.avatar_image:
                getView().myCropImageView.uploadImage();
                break;
            case R.id.next_btn:
                try {
                    String name = getView().nameEditText.getText().toString();
                    String organisation = getView().organisationEditText.getText().toString();
                    User me = DatabaseService.getInstance().getMe();
                    if (name.isEmpty()){
                        UIHelper.getInstance().showAlert(context, context.getString(R.string.please_enter_your_name));
                        return;
                    }
                    if (organisation.isEmpty()){
                        UIHelper.getInstance().showAlert(context, context.getString(R.string.please_enter_your_organisation));
                        return;
                    }
                    me.setSchool(organisation);
                    me.setName(name);
                    me.setAvatar(avatarId);
                    ChatService.getInstance().updateUser(me);
                    Flow.get(context).set(new Profile2Screen());
                }catch (Exception e){
                    UIHelper.getInstance().showAlert(context, context.getResources().getString(R.string.upload_fail));
                    e.printStackTrace();
                }
                break;
        }

    }

    @Override
    protected void onEnterScope(MortarScope scope) {
        super.onEnterScope(scope);
        bus.register(this);
    }

    @Override
    protected void onExitScope() {
        super.onExitScope();
        bus.unregister(this);
    }


    @Override
    public void setNewImage(String avatarId) {
        this.avatarId = avatarId;
        getView().informationLayout.setVisibility(View.VISIBLE);
        UploadFileService.getInstance().loadImage(getView().avatarImage,
                R.drawable.empty_profile, avatarId, 96);
    }

    @Override
    public void hideMainLayout() {
        getView().informationLayout.setVisibility(View.GONE);
    }

    @Subscribe
    public void badgeNotiEvent(BadgeNotiEvent event){
        MasterPointService.getInstance().getBadgesAPI();
        MasterPointService.getInstance().showToolTips(getView().informationLayout, event.badgeName);
    }
}
