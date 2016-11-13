package sg.edu.smu.livelabs.mobicom.presenters;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import autodagger.AutoComponent;
import automortar.AutoScreen;
import flow.Flow;
import mortar.MortarScope;
import mortar.ViewPresenter;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import sg.edu.smu.livelabs.mobicom.ActionBarOwner;
import sg.edu.smu.livelabs.mobicom.App;
import sg.edu.smu.livelabs.mobicom.AppDependencies;
import sg.edu.smu.livelabs.mobicom.DaggerScope;
import sg.edu.smu.livelabs.mobicom.MainActivity;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.adapters.SurveyListAdapter;
import sg.edu.smu.livelabs.mobicom.busEvents.UnregisterSurveyEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.UpdateNetworkEvent;
import sg.edu.smu.livelabs.mobicom.flow.Layout;
import sg.edu.smu.livelabs.mobicom.models.User;
import sg.edu.smu.livelabs.mobicom.models.data.GameListEntity;
import sg.edu.smu.livelabs.mobicom.models.data.SurveyEntity;
import sg.edu.smu.livelabs.mobicom.net.RestClient;
import sg.edu.smu.livelabs.mobicom.net.response.SurveyResponse;
import sg.edu.smu.livelabs.mobicom.presenters.screen.SurveyWebScreen;
import sg.edu.smu.livelabs.mobicom.presenters.screen.SurveyResultScreen;
import sg.edu.smu.livelabs.mobicom.services.DatabaseService;
import sg.edu.smu.livelabs.mobicom.services.GameService;
import sg.edu.smu.livelabs.mobicom.services.SurveyService;
import sg.edu.smu.livelabs.mobicom.services.TrackingService;
import sg.edu.smu.livelabs.mobicom.views.SurveyView;

/**
 * Created by smu on 22/2/16.
 */
@AutoScreen(
        component = @AutoComponent(dependencies = MainActivity.class, superinterfaces = AppDependencies.class),
        screenAnnotations = Layout.class
)
@DaggerScope(SurveyPresenter.class)
@Layout(R.layout.survey_view)
public class SurveyPresenter extends ViewPresenter<SurveyView>{

        private ActionBarOwner actionBarOwner;
        private final RestClient restClient;
        private Bus bus;
        public static String NAME = "SurveyPresenter";
        private MainActivity mainActivity;

        private SurveyListAdapter surveyListAdapter;
        private List<SurveyEntity> surveys;
        private  ConnectivityManager cm;
        private boolean isFirstTime = true;
        private User me;

        public SurveyPresenter(RestClient restClient, Bus bus, ActionBarOwner actionBarOwner, MainActivity mainActivity) {
                this.restClient = restClient;
                this.bus = bus;
                this.actionBarOwner = actionBarOwner;
                this.mainActivity = mainActivity;
        }

        @Override
        protected void onLoad(Bundle savedInstanceState) {
                super.onLoad(savedInstanceState);
                Log.d(App.APP_TAG, " SurveyPresenter onload");

                GameListEntity game = GameService.getInstance().getGameByKeyword("survey");
                if(game != null &&  !game.getGameName().isEmpty()){
                        actionBarOwner.setConfig(new ActionBarOwner.Config(true, game.getGameName(),
                                new ActionBarOwner.MenuAction("Result", new Action0() {
                                        @Override
                                        public void call() {
                                                Flow.get(getView().getContext()).set(new SurveyResultScreen());
                                        }
                                })));
                }
                else{
                        actionBarOwner.setConfig(new ActionBarOwner.Config(true, "Survey",
                                new ActionBarOwner.MenuAction("Result", new Action0() {
                                        @Override
                                        public void call() {
                                                Flow.get(getView().getContext()).set(new SurveyResultScreen());
                                        }
                                })));
                }

                cm = (ConnectivityManager) getView().getContext().getSystemService(Context.CONNECTIVITY_SERVICE);

                me = DatabaseService.getInstance().getMe();

                if(cm.getActiveNetworkInfo() != null) {
                        loadSurvey();
                        isFirstTime = false;
                }
                else{
                        surveys = SurveyService.getInstance().getSurveys();
                        if(surveys != null && surveys.size() > 0){
                                surveyListAdapter = new SurveyListAdapter(getView(), surveys);
                                getView().surveyLV.setAdapter(surveyListAdapter);
                        }
                        else{
                                getView().messageTV.setVisibility(View.VISIBLE);
                                getView().messageTV.setText("It looks like this is your first time coming to the survey, turn on your internet connection to retrieve the list of surveys");
                        }
                }



                getView().surveyLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                Date currentDate= new java.util.Date();
                                if(surveys.get(position).getEndTime() != null &&
                                        currentDate.before(surveys.get(position).getEndTime())){ //null will be for survey that are not from BEP
                                        TrackingService.getInstance().sendTracking("409", "games",
                                                "survey", Long.toString(surveys.get(position).getId()), "", "");
                                        Flow.get(getView()).set(new SurveyWebScreen(surveys.get(position).getId()));
                                }
                                else if(surveys.get(position).getEndTime() == null){ //normal survey without end time
                                        TrackingService.getInstance().sendTracking("409", "games",
                                                "survey", Long.toString(surveys.get(position).getId()), "", "");
                                        Flow.get(getView()).set(new SurveyWebScreen(surveys.get(position).getId()));
                                }

                        }
                });

                App.getInstance().startNetworkMonitoringReceiver();
        }

        @Override
        protected void onEnterScope(MortarScope scope) {
                super.onEnterScope(scope);
                bus.register(this);
                App.getInstance().currentPresenter = NAME;
                mainActivity.setVisibleBottombar(View.GONE);
                if(App.getInstance().previousPresenter.equals(MorePresenter.NAME)){
                        App.getInstance().previousPresenter = "";
                }
        }

        @Override
        protected void onExitScope() {
                super.onExitScope();
                bus.unregister(this);
                if (NAME.equals(App.getInstance().currentPresenter)){
                        App.getInstance().currentPresenter = "";
                }
        }

        //this function update/load list of survey from the server
        private void loadSurvey(){
                final Handler mainHandler = new Handler(getView().getContext().getMainLooper());

                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.ENGLISH);
//                SurveyEntity surveyEntity = SurveyService.getInstance().getSurvey();
                String lastTime = "2016-01-01";//surveyEntity != null ? df.format(surveyEntity.getLastUpdated()) : "2016-01-01";

                restClient.getSurveyApi().getSurvey(lastTime, Long.toString(me.getUID()))
                        .subscribeOn(Schedulers.io())
                        .subscribe(new Action1<SurveyResponse>() {
                                @Override
                                public void call(final SurveyResponse response) {
                                        if (response.status.equals("success")) {
                                                if (response.details != null ) {

                                                        mainHandler.post(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                        surveys = SurveyService.getInstance().updateSurveyList(response.details); //update to db and display it when there is no internet
                                                                        if (getView() != null) {
                                                                                if (surveys.size() > 0) {
                                                                                        if (getView().surveyLV.getAdapter() == null) { //first time loading to adapter
                                                                                                surveyListAdapter = new SurveyListAdapter(getView(), surveys);
                                                                                                getView().surveyLV.setAdapter(surveyListAdapter);
                                                                                        } else {
                                                                                                surveyListAdapter.updates(surveys);
                                                                                        }

                                                                                        getView().messageTV.setVisibility(View.GONE);
                                                                                } else {
                                                                                        getView().messageTV.setVisibility(View.VISIBLE);
                                                                                        getView().messageTV.setText("There is no survey going on now.");
                                                                                }
                                                                        }

                                                                }
                                                        });
                                                }

                                        } else {
                                                mainHandler.post(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                                if (getView() != null) {
                                                                        getView().messageTV.setVisibility(View.VISIBLE);
                                                                        getView().messageTV.setText("There is no survey going on now.");

                                                                }
                                                        }
                                                });
                                        }
                                }
                        }, new Action1<Throwable>() {
                                @Override
                                public void call(Throwable throwable) {
                                        Log.e("Mobisys: ", "cannot get survey", throwable);
                                }
                        });
        }

        @Subscribe
        public void updateNetwork(UpdateNetworkEvent event) {
                if (event.isConnected) {
                        if(isFirstTime) {
                                loadSurvey();
                                isFirstTime = false;
                        }
                }
        }

        @Subscribe
        public void unregisterSurveyReceiver(UnregisterSurveyEvent event){
                if(event.toUnregister) {
                        App.getInstance().stopNetworkMonitoringReceiver();
                }
                else{
                        App.getInstance().startNetworkMonitoringReceiver();
                }
        }

}
