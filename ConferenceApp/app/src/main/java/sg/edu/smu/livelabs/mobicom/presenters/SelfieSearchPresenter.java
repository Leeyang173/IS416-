package sg.edu.smu.livelabs.mobicom.presenters;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import autodagger.AutoComponent;
import automortar.AutoScreen;
import automortar.ScreenParam;
import mortar.MortarScope;
import mortar.ViewPresenter;
import sg.edu.smu.livelabs.mobicom.AppDependencies;
import sg.edu.smu.livelabs.mobicom.DaggerScope;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.UIHelper;
import sg.edu.smu.livelabs.mobicom.adapters.SelfieSearchAdapter;
import sg.edu.smu.livelabs.mobicom.busEvents.SelfieListEvent;
import sg.edu.smu.livelabs.mobicom.flow.Layout;
import sg.edu.smu.livelabs.mobicom.models.User;
import sg.edu.smu.livelabs.mobicom.net.item.Selfie;
import sg.edu.smu.livelabs.mobicom.net.response.SelfieSearchDetailResponse;
import sg.edu.smu.livelabs.mobicom.net.response.SelfieSearchResponse;
import sg.edu.smu.livelabs.mobicom.services.EVAPromotionService;
import sg.edu.smu.livelabs.mobicom.services.ScreenService;
import sg.edu.smu.livelabs.mobicom.views.SelfieSearchView;


@AutoScreen(
        component = @AutoComponent(dependencies = SelfiePresenter.class, superinterfaces = AppDependencies.class),
        screenAnnotations = Layout.class
)
@DaggerScope(SelfieSearchPresenter.class)
@Layout(R.layout.selfie_search_view)
public class SelfieSearchPresenter extends ViewPresenter<SelfieSearchView>  implements SelfieSearchAdapter.SelfieSearchListener {
    private Bus bus;
    private Context context;
    private SelfieSearchAdapter adapter;
    private Handler searchHandler;
    private Runnable runnable;
    private List<Selfie> selfies;
    private ScreenService screenService;
    private String searchText;

    public SelfieSearchPresenter(Bus bus, @ScreenParam ScreenService screenService) {
        this.bus = bus;
        selfies = new ArrayList<>();
        this.screenService = screenService;
    }

    @Override
    protected void onLoad(Bundle savedInstanceState) {
        super.onLoad(savedInstanceState);
        context = getView().getContext();
        adapter = new SelfieSearchAdapter(context, this);
        getView().listView.setAdapter(adapter);
        if (hasView()){
            getView().searchEdit.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    searchHandler.removeCallbacks(runnable);
                    searchHandler.postDelayed(runnable, 1000);
                }
            });
            getView().searchBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    searchHandler.removeCallbacks(runnable);
                    search();
                }
            });
            searchHandler = new Handler();
            runnable = new Runnable() {
                @Override
                public void run() {
                    search();
                }
            };
            String searchText = screenService.pop(SelfieSearchPresenter.class);
            if (searchText != null && !searchText.isEmpty()){
                getView().searchEdit.setText(searchText);
                search();
            }
        }
    }

    private void search() {
        if (!hasView() || getView().searchEdit.getText() == null){
            return;
        }
        String searchText = getView().searchEdit.getText().toString();
        if (searchText == null){
            return;
        }
        searchText = searchText.trim();
        if (searchText.length() < 2){
            getView().noResultText.setHint(context.getResources().getString(R.string.short_search_term));
            getView().noResultText.setVisibility(View.VISIBLE);
            getView().listView.setVisibility(View.INVISIBLE);
            return;
        } else if (searchText.equals(this.searchText)){
            return;
        }
        this.searchText = searchText;
        UIHelper.getInstance().showProgressDialog(context, "waiting...", false);
        EVAPromotionService.getInstance().search(searchText);
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
    public void searchResultEvent(SelfieSearchResponse response){
        if ("success".equals(response.status)){
            screenService.push(SelfieSearchPresenter.class, searchText);
            SelfieSearchDetailResponse details = response.details;
            List<Object> results = new ArrayList<>();
            selfies.clear();
            if (details.images != null){
                for (Selfie selfie : details.images){
                    if ("active".equals(selfie.status)){
                        selfies.add(selfie);
                    }
                }
            }
            results.addAll(selfies);
            if (details.users != null){
                results.addAll(details.users);
            }
            if (results.size() == 0) {
                getView().noResultText.setHint(context.getResources().getString(R.string.type_to_search_for_photo_people));
                getView().noResultText.setVisibility(View.VISIBLE);
                getView().listView.setVisibility(View.INVISIBLE);
            } else {
                getView().noResultText.setVisibility(View.INVISIBLE);
                getView().listView.setVisibility(View.VISIBLE);
                adapter.setData(results);
            }
        }
        UIHelper.getInstance().dismissProgressDialog();
    }

    @Override
    public void openListView(int currentSelfie) {
        SelfieListEvent event = new SelfieListEvent();
        event.previousPage = SelfiePresenter.SEARCH_TAB;
        event.current = currentSelfie;
        event.selfies = selfies;
        event.isProfilePhoto = false;
        event.isLoadMore = false;
        bus.post(event);
        if (currentSelfie < selfies.size()){
            String cmpId = EVAPromotionService.getInstance().currentPromotion.id;
//            EVAPromotionService.getInstance().selfieTracking("606", cmpId, searchText, selfies.get(currentSelfie).id);
        }
    }

    @Override
    public void openUserProfile(User user) {
        UIHelper.getInstance().showProgressDialog(context, "Loading...", false);
        EVAPromotionService.getInstance().getUserPhotos(user, SelfiePresenter.SEARCH_TAB);
        String cmpId = EVAPromotionService.getInstance().currentPromotion.id;
//        EVAPromotionService.getInstance().selfieTracking("607", cmpId, searchText, String.valueOf(user.getUid()));
    }
}


