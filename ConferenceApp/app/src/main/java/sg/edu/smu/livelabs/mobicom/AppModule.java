package sg.edu.smu.livelabs.mobicom;

import com.google.gson.Gson;
import com.squareup.otto.Bus;

import autodagger.AutoExpose;
import dagger.Provides;
import sg.edu.smu.livelabs.mobicom.net.RestClient;

/**
 * Created by Aftershock PC on 1/7/2015.
 */
@dagger.Module
public class AppModule {
    private final App app;
    private final Bus bus;
    private final Gson gson;
    private final RestClient restClient;

    public AppModule() {
        throw new RuntimeException("Should never come here.");
    }

    public AppModule(App app) {
        this.app = app;
        this.bus = app.getBus();
        this.restClient = app.getRestClient();
        this.gson = app.getGson();
    }

    @Provides
    @AutoExpose(App.class)
    public App getApp() {
        return app;
    }

    @Provides
    @AutoExpose(App.class)
    public Bus bus() {
        return bus;
    }

    @Provides
    @AutoExpose(App.class)
    public RestClient restClient() {
        return restClient;
    }

    @Provides
    @AutoExpose(App.class)
    public Gson getGson() {
        return gson;
    }
}
