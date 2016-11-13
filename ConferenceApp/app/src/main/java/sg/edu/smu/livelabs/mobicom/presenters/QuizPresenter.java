package sg.edu.smu.livelabs.mobicom.presenters;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import autodagger.AutoComponent;
import automortar.AutoScreen;
import automortar.ScreenParam;
import mortar.MortarScope;
import mortar.ViewPresenter;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import sg.edu.smu.livelabs.mobicom.ActionBarOwner;
import sg.edu.smu.livelabs.mobicom.App;
import sg.edu.smu.livelabs.mobicom.AppDependencies;
import sg.edu.smu.livelabs.mobicom.DaggerScope;
import sg.edu.smu.livelabs.mobicom.MainActivity;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.UIHelper;
import sg.edu.smu.livelabs.mobicom.busEvents.BadgeNotiEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.UnregisterVotingEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.UpdateNetworkEvent;
import sg.edu.smu.livelabs.mobicom.flow.Layout;
import sg.edu.smu.livelabs.mobicom.models.User;
import sg.edu.smu.livelabs.mobicom.net.RestClient;
import sg.edu.smu.livelabs.mobicom.net.response.FormResponse;
import sg.edu.smu.livelabs.mobicom.net.response.SimpleResponse;
import sg.edu.smu.livelabs.mobicom.services.DatabaseService;
import sg.edu.smu.livelabs.mobicom.services.GameService;
import sg.edu.smu.livelabs.mobicom.services.MasterPointService;
import sg.edu.smu.livelabs.mobicom.services.TrackingService;
import sg.edu.smu.livelabs.mobicom.views.QuizView;

/**
 * Created by smu on 22/2/16.
 */
@AutoScreen(
        component = @AutoComponent(dependencies = MainActivity.class, superinterfaces = AppDependencies.class),
        screenAnnotations = Layout.class
)
@DaggerScope(QuizPresenter.class)
@Layout(R.layout.quiz_view)
public class QuizPresenter extends ViewPresenter<QuizView>{

        private ActionBarOwner actionBarOwner;
        private final RestClient restClient;
        private Bus bus;
        public static String NAME = "QuizPresenter";

        private long paperId;
        private User me;
        private  ConnectivityManager cm;
        private MainActivity mainActivity;
        private boolean isLoaded; //to determine whether have the quiz been loaded
        private boolean isFirstTime = true;

        /**
         *
         * @param restClient
         * @param bus
         * @param actionBarOwner
         * @param paperId: Paper Id will be the event id itself
         */
        public QuizPresenter(RestClient restClient, Bus bus, ActionBarOwner actionBarOwner, MainActivity mainActivity, @ScreenParam long paperId) {
                this.restClient = restClient;
                this.bus = bus;
                this.paperId = paperId;
                this.actionBarOwner = actionBarOwner;
                this.mainActivity = mainActivity;
                this.isLoaded = false;
        }

        @Override
        protected void onLoad(Bundle savedInstanceState) {
                super.onLoad(savedInstanceState);
                Log.d(App.APP_TAG, " QuizPresenter onload");

                actionBarOwner.setConfig(new ActionBarOwner.Config(true, "Quiz", null));

                getView().progress.setVisibility(View.VISIBLE);

                me = DatabaseService.getInstance().getMe();

                cm = (ConnectivityManager) getView().getContext().getSystemService(Context.CONNECTIVITY_SERVICE);

                if(cm.getActiveNetworkInfo() != null){
                        loadQuiz();
                        checkCount("quiz");
                        isFirstTime = false;
                }
                else{
                        getView().progress.setVisibility(View.GONE);
                        getView().messageTV.setVisibility(View.VISIBLE);
                        getView().messageTV.setText("Quiz cannot be loaded without Internet connection");
                }

                App.getInstance().startNetworkMonitoringReceiver();
        }

        @Override
        protected void onEnterScope(MortarScope scope) {
                super.onEnterScope(scope);
                bus.register(this);
                App.getInstance().setPrevious();
                App.getInstance().currentPresenter = NAME;
                mainActivity.setVisibleBottombar(View.GONE);
        }

        @Override
        protected void onExitScope() {
                super.onExitScope();
                bus.unregister(this);
                if(!GameService.getInstance().fromGames(App.getInstance().previousPresenter)){
                        mainActivity.setVisibleBottombar(View.VISIBLE);
                }
                if (NAME.equals(App.getInstance().currentPresenter)){
                        App.getInstance().currentPresenter = "";
                }
        }

        @Subscribe
        public void unregisterVotingReceiver(UnregisterVotingEvent event){
                if(event.toUnregister) {
                        App.getInstance().stopNetworkMonitoringReceiver();
                }
                else{
                        App.getInstance().startNetworkMonitoringReceiver();
                }
        }


        @Subscribe
        public void updateNetwork(UpdateNetworkEvent event) {
                if (event.isConnected && !isLoaded && isFirstTime) {
                        loadQuiz();
                        checkCount("quiz");
                        isFirstTime = false;
                }
        }

        private void loadQuiz(){
                final Handler mainHandler = new Handler(getView().getContext().getMainLooper());

                restClient.getQuizApi().getQuiz(Long.toString(paperId), Long.toString(me.getUID()))
                        .subscribeOn(Schedulers.io())
                        .subscribe(new Action1<SimpleResponse>() {
                                @Override
                                public void call(final SimpleResponse response) {
                                        if (response.status.equals("success")) {
                                                if (response.details != null) {
                                                        mainHandler.post(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                        if (getView() != null) {
                                                                                getView().webView.setVisibility(View.VISIBLE);
                                                                                getView().webView.loadUrl(response.details);
                                                                                getView().webView.setWebViewClient(new WebViewClient() {

                                                                                        @Override
                                                                                        public boolean shouldOverrideUrlLoading(WebView view, String url) {
                                                                                                view.loadUrl(url);
                                                                                                return true;
                                                                                        }

                                                                                        @Override
                                                                                        public void onPageStarted(WebView view, String url, Bitmap favicon) {
                                                                                                super.onPageStarted(view, url, favicon);
                                                                                                if (getView() != null) {
                                                                                                        getView().progress.setVisibility(View.VISIBLE);
                                                                                                        getView().messageTV.setVisibility(View.VISIBLE);
                                                                                                        getView().messageTV.setText("Loading");
                                                                                                }

                                                                                        }

                                                                                        @Override
                                                                                        public void onPageFinished(WebView view, String url) {
                                                                                                super.onPageFinished(view, url);
                                                                                                if (getView() != null) {
                                                                                                        getView().progress.setVisibility(View.GONE);
                                                                                                        getView().messageTV.setVisibility(View.GONE);
                                                                                                        isLoaded = true;
                                                                                                }
                                                                                        }
                                                                                });
                                                                        }
                                                                }
                                                        });
                                                }

                                        }
                                        else{
                                                mainHandler.post(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                                if (getView() != null) {
                                                                        getView().webView.setVisibility(View.GONE);
                                                                        getView().progress.setVisibility(View.GONE);
                                                                        getView().messageTV.setVisibility(View.VISIBLE);
                                                                        getView().messageTV.setText(response.details);

                                                                }
                                                        }
                                                });


                                        }
                                }
                        }, new Action1<Throwable>() {
                                @Override
                                public void call(Throwable throwable) {
                                        Log.e("Mobisys: ", "cannot get quiz", throwable);
                                }
                        });

                TrackingService.getInstance().sendTracking("110", "agenda", Long.toString(paperId), "quiz", "", "");
        }

        /**
         * This basically check with the server if this is the first time user open this form before awarding the user
         */
        private void checkCount(String formType){
                if(getView() != null) {
                        final Handler mainHandler = new Handler(getView().getContext().getMainLooper());
                        restClient.getBadgeApi().checkFormCount(Long.toString(me.getUID()), formType, Long.toString(paperId))
                                .subscribeOn(Schedulers.io())
                                .subscribe(new Action1<FormResponse>() {
                                        @Override
                                        public void call(final FormResponse response) {
                                                UIHelper.getInstance().dismissProgressDialog();
                                                if (response.details == 0)
                                                        mainHandler.post(new Runnable() {
                                                                @Override
                                                                public void run() {

//                                                                MasterPointService.getInstance().addPoint(MasterPointService.getInstance().QUIZ, getView().webView);
                                                                }
                                                        });
                                        }
                                }, new Action1<Throwable>() {
                                        @Override
                                        public void call(Throwable throwable) {
                                                Log.e("Mobisys: ", "cannot check form count", throwable);
                                        }
                                });
                }
        }

        @Subscribe
        public void badgeNotiEvent(BadgeNotiEvent event){
                MasterPointService.getInstance().getBadgesAPI();
                MasterPointService.getInstance().showToolTips(getView().webView, event.badgeName);
        }
}
