package sg.edu.smu.livelabs.mobicom.presenters;

import android.content.Context;
import android.os.Bundle;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import autodagger.AutoComponent;
import automortar.AutoScreen;
import automortar.ScreenParam;
import mortar.MortarScope;
import mortar.ViewPresenter;
import sg.edu.smu.livelabs.mobicom.AppDependencies;
import sg.edu.smu.livelabs.mobicom.DaggerScope;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.UIHelper;
import sg.edu.smu.livelabs.mobicom.adapters.SelfieLikersAdapter;
import sg.edu.smu.livelabs.mobicom.busEvents.SelfieLikerEvent;
import sg.edu.smu.livelabs.mobicom.flow.Layout;
import sg.edu.smu.livelabs.mobicom.models.User;
import sg.edu.smu.livelabs.mobicom.services.EVAPromotionService;
import sg.edu.smu.livelabs.mobicom.services.ScreenService;
import sg.edu.smu.livelabs.mobicom.views.SelfieLikersView;

/**
 * Created by smu on 12/11/15.
 */
@AutoScreen(
        component = @AutoComponent(dependencies = SelfiePresenter.class, superinterfaces = AppDependencies.class),
        screenAnnotations = Layout.class
)
@DaggerScope(SelfieLikersPresenter.class)
@Layout(R.layout.selfie_likers_view)
public class SelfieLikersPresenter extends ViewPresenter<SelfieLikersView> implements SelfieLikersAdapter.LikerListener{
    private Context context;
    private Bus bus;
    private SelfieLikersAdapter adapter;
    private ScreenService screenService;
    public SelfieLikersPresenter(Bus bus, @ScreenParam ScreenService screenService){
        this.bus = bus;
        this.screenService = screenService;
    }

    @Override
    protected void onLoad(Bundle savedInstanceState) {
        super.onLoad(savedInstanceState);
        context = getView().getContext();
        adapter = new SelfieLikersAdapter(context, this);
        getView().listView.setAdapter(adapter);
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
    @Subscribe
    public void openLikerScreen(SelfieLikerEvent event) {
        if (event.hasExecuted) return;
        event.hasExecuted = true;
        if (event.users != null){
            adapter.setData(event.users);
        }
        UIHelper.getInstance().dismissProgressDialog();
    }

    @Override
    public void goToUserDetail(User user) {
        UIHelper.getInstance().showProgressDialog(context, "Loading...", false);
        EVAPromotionService.getInstance().getUserPhotos(user, SelfiePresenter.LIKER_TAB);
        screenService.push(SelfieLikersPresenter.class, adapter.getAllData());
    }
}
