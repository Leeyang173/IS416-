package sg.edu.smu.livelabs.mobicom.presenters;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.HashSet;
import java.util.Iterator;

import autodagger.AutoComponent;
import automortar.AutoScreen;
import mortar.MortarScope;
import mortar.ViewPresenter;
import sg.edu.smu.livelabs.mobicom.AppDependencies;
import sg.edu.smu.livelabs.mobicom.DaggerScope;
import sg.edu.smu.livelabs.mobicom.MainActivity;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.adapters.Profile2Adapter;
import sg.edu.smu.livelabs.mobicom.busEvents.BadgeNotiEvent;
import sg.edu.smu.livelabs.mobicom.flow.Layout;
import sg.edu.smu.livelabs.mobicom.models.User;
import sg.edu.smu.livelabs.mobicom.services.ChatService;
import sg.edu.smu.livelabs.mobicom.services.DatabaseService;
import sg.edu.smu.livelabs.mobicom.services.InterestService;
import sg.edu.smu.livelabs.mobicom.services.MasterPointService;
import sg.edu.smu.livelabs.mobicom.views.Profile2View;

/**
 * Created by smu on 26/4/16.
 */
@AutoScreen(
        component = @AutoComponent(dependencies = MainActivity.class, superinterfaces = AppDependencies.class),
        screenAnnotations = Layout.class
)
@DaggerScope(Profile2Presenter.class)
@Layout(R.layout.profile2_view)
public class Profile2Presenter extends ViewPresenter<Profile2View> implements Profile2Adapter.Profile2InterestListener {
    private MainActivity mainActivity;
    private Profile2Adapter adapter;
    private Context context;
    private HashSet<String> myInterests;
    private Bus bus;

    public Profile2Presenter(MainActivity mainActivity, Bus bus){
        this.mainActivity = mainActivity;
        this.bus = bus;
    }

    @Override
    protected void onLoad(Bundle savedInstanceState) {
        super.onLoad(savedInstanceState);
        if (!hasView()) return;
        context = getView().getContext();
        adapter = new Profile2Adapter(context, this);
        getView().listView.setAdapter(adapter);
        adapter.setData(InterestService.getInstance().getAllInterest());
        final String[] interests = DatabaseService.getInstance().getMe().getInterests();
        myInterests = new HashSet<>();
        if (interests != null){
            for (String interest : interests){
                if (!myInterests.contains(interest)){
                    myInterests.add(interest);
                }
            }
        }
        getView().doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StringBuilder stringBuilder = new StringBuilder();
                Iterator<String> interests = myInterests.iterator();
                while (interests.hasNext()){
                    stringBuilder.append(interests.next());
                    stringBuilder.append(",");
                }
                if (stringBuilder.length() > 1){
                    stringBuilder = stringBuilder.deleteCharAt(stringBuilder.length() - 1);
                    User me = DatabaseService.getInstance().getMe();
                    me.setInterestsStr(stringBuilder.toString());
                    ChatService.getInstance().updateUser(me);
                }
                mainActivity.goToMainPage();

            }
        });
    }

    @Override
    public void addInterest(String interest) {
        if (!myInterests.contains(interest)){
            myInterests.add(interest);
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
    public void removeInterest(String interest) {
        myInterests.remove(interest);
    }

    @Override
    public boolean isMyInterest(String interest) {
        return myInterests.contains(interest);
    }

    @Subscribe
    public void badgeNotiEvent(BadgeNotiEvent event){
        MasterPointService.getInstance().getBadgesAPI();
        MasterPointService.getInstance().showToolTips(getView().listView, event.badgeName);
    }
}
