package sg.edu.smu.livelabs.mobicom.presenters;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.text.Editable;
import android.text.TextWatcher;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import autodagger.AutoComponent;
import automortar.AutoScreen;
import automortar.ScreenParam;
import de.greenrobot.dao.query.LazyList;
import mortar.MortarScope;
import mortar.ViewPresenter;
import sg.edu.smu.livelabs.mobicom.App;
import sg.edu.smu.livelabs.mobicom.AppDependencies;
import sg.edu.smu.livelabs.mobicom.DaggerScope;
import sg.edu.smu.livelabs.mobicom.MainActivity;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.adapters.ContactsAdapter;
import sg.edu.smu.livelabs.mobicom.busEvents.UpdatedAttendeesEvent;
import sg.edu.smu.livelabs.mobicom.flow.Layout;
import sg.edu.smu.livelabs.mobicom.services.AttendeesService;
import sg.edu.smu.livelabs.mobicom.views.ContactsView;

/**
 * Created by smu on 8/3/16.
 */
@AutoScreen(
        component = @AutoComponent(dependencies = MessagePresenter.class, superinterfaces = AppDependencies.class),
        screenAnnotations = Layout.class
)
@DaggerScope(ContactsPresenter.class)
@Layout(R.layout.contacts_view)
public class ContactsPresenter extends ViewPresenter<ContactsView> {
    private final static int TIME_DELAY = 1000;
    private ContactsAdapter contactsAdapter;
    private Context context;
    private MainActivity mainActivity;
    private LazyList attendees;
    private Handler searchHandler;
    private Runnable searchRunable;
    private String currentKey;
    private String previousKey;
    private Bus bus;

    public ContactsPresenter(Bus bus, @ScreenParam MainActivity mainActivity){
        this.mainActivity = mainActivity;
        this.bus = bus;
    }

    @Override
    protected void onLoad(Bundle savedInstanceState) {
        super.onLoad(savedInstanceState);
        if (!hasView()) return;
        currentKey = "";
        previousKey = "";
        this.context = getView().getContext();
        contactsAdapter = new ContactsAdapter(context);
        getView().contactListview.setHasFixedSize(false);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        getView().contactListview.setLayoutManager(linearLayoutManager);
        getView().contactListview.setAdapter(contactsAdapter);
        getView().contactListview.disableLoadmore();
        getView().contactListview.setDefaultOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                AttendeesService.getInstance().syncAttendees(false);
            }
        });
        attendees = AttendeesService.getInstance().getAllAttendees();
        contactsAdapter.setData(attendees);

//        getView().searchFilterListview.setAdapter(contactsSearchKeysAdapter);

//        getView().searchKeyLayout.setOnClickListener(this);
        getView().searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //TODO search
                searchHandler.removeCallbacks(searchRunable);
                searchHandler.postDelayed(searchRunable, TIME_DELAY);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        searchHandler = new Handler();
        searchRunable = new Runnable() {
            @Override
            public void run() {
                search();
            }
        };
    }

    private void search() {
        if(getView() == null) return;
        Editable key = getView().searchText.getText();
        if (key == null) return;
        previousKey = currentKey;
        currentKey = key.toString().trim();
        if (previousKey.equals(currentKey)) {
            return;
        }
        if (currentKey.isEmpty()){
            attendees = AttendeesService.getInstance().getAllAttendees();
        } else {
            attendees = AttendeesService.getInstance().search(currentKey);
        }
        contactsAdapter.setData(attendees);
    }

    @Override
    protected void onEnterScope(MortarScope scope) {
        super.onEnterScope(scope);
        bus.register(this);
        if(App.getInstance().currentPresenter.equals(MorePresenter.NAME)){
            App.getInstance().setPrevious();
            App.getInstance().currentPresenter = "ContactsPresenter";
        }
    }

    @Override
    protected void onExitScope() {
        bus.unregister(this);
        if (attendees != null){
            attendees.close();
        }

        App.getInstance().previousPresenter = "";
        super.onExitScope();
    }

    @Subscribe
    public void updateAtendees(UpdatedAttendeesEvent event){
        if (event.success){
            attendees = AttendeesService.getInstance().getAllAttendees();
            contactsAdapter.setData(attendees);
        }
        getView().contactListview.setRefreshing(false);
    }

}
