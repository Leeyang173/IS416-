package sg.edu.smu.livelabs.mobicom;

import autodagger.AutoExpose;
import dagger.Provides;
import sg.edu.smu.livelabs.mobicom.services.ScreenService;

/**
 * Created by Aftershock PC on 9/7/2015.
 */
@dagger.Module
public class MainActivityModule {
    private final MainActivity mainActivity;
    private final ActionBarOwner actionBarOwner;
    private final ScreenService screenService;
    public MainActivityModule(MainActivity mainActivity, ActionBarOwner actionBarOwner) {
        this.mainActivity = mainActivity;
        this.actionBarOwner = actionBarOwner;
        this.screenService = mainActivity.getScreenService();
    }

    @Provides
    @AutoExpose(MainActivity.class)
    public MainActivity getMainActivity() {
        return mainActivity;
    }

    @Provides
    @AutoExpose(MainActivity.class)
    public ActionBarOwner getActionBarOwner() {
        return actionBarOwner;
    }

    @Provides
    @AutoExpose(MainActivity.class)
    public ScreenService getScreenService() {
        return screenService;
    }

//    @Provides
//    @AutoExpose(MainActivity.class)
//    public Bus getBus() {
//        App app = (App) mainActivity.getApplication();
//        return app.getBus();
//    }
}
