package sg.edu.smu.livelabs.mobicom.presenters;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import autodagger.AutoComponent;
import automortar.AutoScreen;
import automortar.ScreenParam;
import flow.Flow;
import mortar.MortarScope;
import mortar.ViewPresenter;
import rx.functions.Action1;
import sg.edu.smu.livelabs.mobicom.AppDependencies;
import sg.edu.smu.livelabs.mobicom.DaggerScope;
import sg.edu.smu.livelabs.mobicom.EndlessScrollListener;
import sg.edu.smu.livelabs.mobicom.MainActivity;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.UIHelper;
import sg.edu.smu.livelabs.mobicom.adapters.SelfieProfileAdapter;
import sg.edu.smu.livelabs.mobicom.busEvents.SelfieListEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.SelfieProfileEvent;
import sg.edu.smu.livelabs.mobicom.fileupload.UploadFileService;
import sg.edu.smu.livelabs.mobicom.flow.Layout;
import sg.edu.smu.livelabs.mobicom.models.User;
import sg.edu.smu.livelabs.mobicom.net.RestClient;
import sg.edu.smu.livelabs.mobicom.net.item.Selfie;
//import sg.edu.smu.livelabs.mobicom.presenters.screen.SelfieCameraScreen;
import sg.edu.smu.livelabs.mobicom.presenters.screen.SelfieCameraScreen;
import sg.edu.smu.livelabs.mobicom.services.DatabaseService;
import sg.edu.smu.livelabs.mobicom.services.GameService;
import sg.edu.smu.livelabs.mobicom.services.ScreenService;
import sg.edu.smu.livelabs.mobicom.util.Util;
import sg.edu.smu.livelabs.mobicom.views.SelfieProfileView;


@AutoScreen(
        component = @AutoComponent(dependencies = SelfiePresenter.class, superinterfaces = AppDependencies.class),
        screenAnnotations = Layout.class
)
@DaggerScope(SelfieProfilePresenter.class)
@Layout(R.layout.selfie_profile_view)
public class SelfieProfilePresenter extends ViewPresenter<SelfieProfileView> implements SelfieProfileAdapter.SelfieProfileListener {
    public static int previousPage;
    private SelfieProfileAdapter adapter;
    private User user;
    private List<Selfie> selfies;
    private int totalPhotos;
    private ScreenService screenService;
    private Context context;
    private Bus bus;
    private MainActivity mainActivity;
    private RestClient restClient;

    public SelfieProfilePresenter(RestClient restClient, Bus bus, @ScreenParam ScreenService screenService, @ScreenParam MainActivity mainActivity){
        this.bus = bus;
        this.screenService = screenService;
        this.mainActivity = mainActivity;
        this.restClient = restClient;
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
    protected void onLoad(Bundle savedInstanceState) {
        super.onLoad(savedInstanceState);
        context = getView().getContext();
        adapter = new SelfieProfileAdapter(context, new ArrayList<Selfie>(), this);
        getView().gridView.setAdapter(adapter);
        getView().gridView.setOnScrollListener(new EndlessScrollListener() {
            @Override
            public boolean onLoadMore(int page, int totalItemsCount) {
//                List<Selfie> selfies = EVAPromotionService.getInstance().test();
//                adapter.append(selfies);
//                return true;
                return false;
            }
        });

        getView().uploadPhotoRL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UploadFileService.getInstance().requestPhoto(mainActivity, new Action1<String>() {
                    @Override
                    public void call(String filepath) {
                        Selfie selfie = new Selfie();
                        selfie.imageId = filepath;
                        selfie.id = null;
                        Flow.get(context).set(new SelfieCameraScreen(selfie));

                    }
                });
            }
        });
    }

    @Override
    public void openListView(int currentSelfie) {
        screenService.push(SelfieProfilePresenter.class, new SelfieProfileEvent(user, selfies.size(), selfies, false, previousPage));
        SelfieListEvent event = new SelfieListEvent();
        event.previousPage = SelfiePresenter.PROFILE_TAB;
        event.current = currentSelfie;
        event.selfies = selfies;
        event.isProfilePhoto = true;
        event.isLoadMore = false;
        bus.post(event);
    }


    @Subscribe
    public void setData(SelfieProfileEvent event){
        if (event.hasExecuted) {
            UIHelper.getInstance().dismissProgressDialog();
            return;
        }
        event.hasExecuted = true;
        if (event.user == null) {
            event.user = DatabaseService.getInstance().getMe();
        }
        user = event.user;
        selfies = event.photos;
        totalPhotos = event.totalPhotos;
        previousPage = event.previousPage;
        if (user != null){
            getView().nameTV.setText(user.getName());
            getView().emailTV.setText(user.getEmail());
            if (totalPhotos < 2){
                getView().photoCountTV.setText(totalPhotos + " photo (max 20 photos)");
            } else {
                getView().photoCountTV.setText(totalPhotos + " photos (max 20 photos)");
            }
            getView().photoCountTV.setVisibility(View.GONE);
            Picasso.with(context).cancelRequest(getView().userProfileImage);
            if (user.getAvatar() != null && !user.getAvatar().isEmpty()){
                Picasso.with(context)
                        .load(Util.getPhotoUrlFromId(user.getAvatar(), 96))
                        .noFade()
                        .placeholder(R.drawable.icon_no_profile)
                        .into(getView().userProfileImage, new com.squareup.picasso.Callback() {
                            @Override
                            public void onSuccess() {
                                if(hasView())
                                    getView().nameShortFormTV.setVisibility(View.GONE);
                            }

                            @Override
                            public void onError() { //when failed to load, draw a rect on top of the noavatar and add the name short form
                                GameService.getInstance().drawNameShort(user.getName(), getView().userProfileImage, getView().nameShortFormTV);
                            }
                        });
            } else {
//                getView().userProfileImage.setImageResource(R.drawable.empty_profile);
                GameService.getInstance().drawNameShort(user.getName(), getView().userProfileImage, getView().nameShortFormTV);
            }


        } else {
            if(hasView())
                getView().photoCountTV.setText("");
            UIHelper.getInstance().dismissProgressDialog();
            return;
        }


        if (selfies == null || selfies.size() == 0){
            if (user.getUID() == DatabaseService.getInstance().getMe().getUID()){
                getView().noItemTV.setVisibility(View.VISIBLE);
                getView().shareText.setText(context.getResources().getString(R.string.you_have_not_submitted_any_photo));
                getView().noItemTV.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        UploadFileService.getInstance().requestPhoto(mainActivity, new Action1<String>() {
                            @Override
                            public void call(String filepath) {
                                Selfie selfie = new Selfie();
                                selfie.imageId = filepath;
                                selfie.id = null;
                                Flow.get(context).set(new SelfieCameraScreen(selfie));
                            }
                        });
                    }
                });
            } else {
                String noPhoto = context.getResources().getString(R.string.has_not_submitted_any_photo);
                getView().noItemTV.setVisibility(View.GONE);
                getView().shareText.setText(user.getName() + noPhoto);
                adapter.setData(new ArrayList<Selfie>());
            }
        } else {
            adapter.setData(selfies);
            getView().noItemTV.setVisibility(View.GONE);

        }
        UIHelper.getInstance().dismissProgressDialog();
    }

}


