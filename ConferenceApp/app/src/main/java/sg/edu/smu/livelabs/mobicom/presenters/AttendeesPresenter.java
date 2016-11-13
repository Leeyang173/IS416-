package sg.edu.smu.livelabs.mobicom.presenters;

import android.content.Context;
import android.os.Bundle;

import com.squareup.otto.Bus;

import autodagger.AutoComponent;
import automortar.AutoScreen;
import mortar.ViewPresenter;
import sg.edu.smu.livelabs.mobicom.ActionBarOwner;
import sg.edu.smu.livelabs.mobicom.AppDependencies;
import sg.edu.smu.livelabs.mobicom.DaggerScope;
import sg.edu.smu.livelabs.mobicom.MainActivity;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.adapters.SlidePagerAdapter;
import sg.edu.smu.livelabs.mobicom.flow.Layout;
import sg.edu.smu.livelabs.mobicom.services.ScreenService;
import sg.edu.smu.livelabs.mobicom.views.AttendeesView;

/**
 * Created by smu on 22/2/16.
 */
@AutoScreen(
        component = @AutoComponent(dependencies = MainActivity.class, superinterfaces = AppDependencies.class),
        screenAnnotations = Layout.class
)
@DaggerScope(AttendeesPresenter.class)
@Layout(R.layout.attendees_view)
public class AttendeesPresenter extends ViewPresenter<AttendeesView>{
    private Bus bus;
    private ActionBarOwner actionBarOwner;
    private MainActivity mainActivity;
    private ScreenService screenService;
    private Context context;
    private SlidePagerAdapter adapter;
    public AttendeesPresenter(Bus bus, ActionBarOwner actionBarOwner, MainActivity mainActivity, ScreenService screenService){
        this.bus = bus;
        this.actionBarOwner = actionBarOwner;
        this.mainActivity = mainActivity;
        this.screenService = screenService;
    }

    @Override
    protected void onLoad(Bundle savedInstanceState) {
        super.onLoad(savedInstanceState);
        if (!hasView()) return;
        context = getView().getContext();
    }
}
