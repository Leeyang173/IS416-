package sg.edu.smu.livelabs.mobicom.presenters;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

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
import sg.edu.smu.livelabs.mobicom.adapters.StumpLeaderboardAdapter;
import sg.edu.smu.livelabs.mobicom.busEvents.BadgeNotiEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.UnregisterStumpEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.UpdateNetworkEvent;
import sg.edu.smu.livelabs.mobicom.flow.Layout;
import sg.edu.smu.livelabs.mobicom.models.User;
import sg.edu.smu.livelabs.mobicom.net.RestClient;
import sg.edu.smu.livelabs.mobicom.net.item.SponsorItem;
import sg.edu.smu.livelabs.mobicom.net.item.StumpItem;
import sg.edu.smu.livelabs.mobicom.net.item.StumpLeaderboardItem;
import sg.edu.smu.livelabs.mobicom.net.item.StumpQuestionItem;
import sg.edu.smu.livelabs.mobicom.net.response.StumpQuestionResponse;
import sg.edu.smu.livelabs.mobicom.net.response.StumpSponsorResponse;
import sg.edu.smu.livelabs.mobicom.net.response.StumpStoreScoreResponse;
import sg.edu.smu.livelabs.mobicom.services.DatabaseService;
import sg.edu.smu.livelabs.mobicom.services.GameService;
import sg.edu.smu.livelabs.mobicom.services.MasterPointService;
import sg.edu.smu.livelabs.mobicom.views.StumpView;

/**
 * Created by smu on 22/2/16.
 */
@AutoScreen(
        component = @AutoComponent(dependencies = MainActivity.class, superinterfaces = AppDependencies.class),
        screenAnnotations = Layout.class
)
@DaggerScope(StumpPresenter.class)
@Layout(R.layout.stump_view)
public class StumpPresenter extends ViewPresenter<StumpView>{

        private ActionBarOwner actionBarOwner;
        private final RestClient restClient;
        private Bus bus;
        public static String NAME = "StumpPresenter";

        private User me;
        private  ConnectivityManager cm;
        private MainActivity mainActivity;
        private boolean isLoaded; //to determine whether have the quiz been loaded

        private StumpLeaderboardAdapter stumpLeaderboardAdapter;

        private CountDownTimer countDownTimer;
        private int score;
        private StumpItem stump;
        private List<StumpQuestionItem> stumpQuestions;
        private Toast tst;

        /**
         *
         * @param restClient
         * @param bus
         * @param actionBarOwner
         */
        public StumpPresenter(RestClient restClient, Bus bus, ActionBarOwner actionBarOwner, MainActivity mainActivity,
                              @ScreenParam StumpItem stump) {
                this.restClient = restClient;
                this.bus = bus;
                this.actionBarOwner = actionBarOwner;
                this.mainActivity = mainActivity;
                this.isLoaded = false;
                this.stump = stump;
        }

        @Override
        protected void onLoad(Bundle savedInstanceState) {
                super.onLoad(savedInstanceState);
                Log.d(App.APP_TAG, " StumpPresenter onload");

                actionBarOwner.setConfig(new ActionBarOwner.Config(true, stump.title, null));

                me = DatabaseService.getInstance().getMe();

                cm = (ConnectivityManager) getView().getContext().getSystemService(Context.CONNECTIVITY_SERVICE);

                getView().scoreTV.setText("" + score);

                if(cm.getActiveNetworkInfo() != null){
                        loadStumpQuestion();
                }
                else{
                        getView().messageTV.setVisibility(View.VISIBLE);
                        getView().container.setVisibility(View.GONE);
                }

                App.getInstance().startNetworkMonitoringReceiver();
        }

        private void nextQuestion(){
                createQuestion();

                if(countDownTimer != null){
                        countDownTimer.cancel();
                        countDownTimer = null;
                }


                if(countDownTimer == null) {
                        countDownTimer = new CountDownTimer(30000, 1000) {

                                public void onTick(long millisUntilFinished) {
                                        if (getView() != null)
                                                getView().overlayMsg.setText(Html.fromHtml("<b>" + (millisUntilFinished / 1000) + "</b>") + " sec left");
                                        //here you can have your logic to set text to edittext
                                }

                                public void onFinish() {
                                        if (getView() != null) {
                                                getView().overlayMsg.setText("Ended!");
                                                nextQuestion();
                                        }
                                }

                        }.start();
                }
        }

        private void createQuestion(){
                getView().question.removeAllViews();

                if(stumpQuestions.size() < 1){
                        return;
                }

                Random r = new Random();
                int i = r.nextInt(stumpQuestions.size());

                final StumpQuestionItem q = stumpQuestions.get(i);

                RelativeLayout.LayoutParams layout = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT);
                layout.addRule(RelativeLayout.BELOW, getView().overlay.getId());
                layout.leftMargin = 20;
                TextView questionTV = new TextView(getView().getContext());
                questionTV.setText(q.title);
                questionTV.setLayoutParams(layout);
                questionTV.setTextColor(getView().getContext().getResources().getColor(R.color.dark_grey));
                Resources resources = getView().getContext().getResources();
                questionTV.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);

                questionTV.setId(R.id.questionTV);
                getView().question.addView(questionTV);


                int count = 0;
                if(q.getQuesOption() != null) {
                        List<String> question = new ArrayList<>();
                        for(String s: q.getQuesOption()){
                                question.add(s);
                        }
                        Collections.shuffle(question);

                        for (String s : question) {
                                RelativeLayout.LayoutParams layoutSelection = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                                        RelativeLayout.LayoutParams.WRAP_CONTENT);

                                final Button selection = new Button(getView().getContext());
                                selection.setText(s);
//                                selection.setTag(count + 1);
                                selection.setTag(s);
                                int id = 0;
                                switch (count) {
                                        case 0:
                                                id = R.id.questionTV;
                                                selection.setId(R.id.selection1);
                                                break;
                                        case 1:
                                                id = R.id.selection1;
                                                selection.setId(R.id.selection2);
                                                break;
                                        case 2:
                                                id = R.id.selection2;
                                                selection.setId(R.id.selection3);
                                                break;
                                        case 3:
                                                id = R.id.selection3;
                                                selection.setId(R.id.selection4);
                                                break;
                                        case 4:
                                                id = R.id.selection4;
                                                selection.setId(R.id.selection5);
                                                break;
                                        case 5:
                                                id = R.id.selection5;
                                                selection.setId(R.id.selection6);
                                                break;
                                }
                                layoutSelection.addRule(RelativeLayout.BELOW, id);
                                layoutSelection.setMargins(10, 5, 10, 5);
                                selection.setLayoutParams(layoutSelection);
                                selection.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
//                                selection.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
//                                        getView().getContext().getResources().getDimension(R.dimen.text_size_super_super_small), resources.getDisplayMetrics()));
                                selection.setBackgroundResource(R.drawable.custom_button);
                                selection.setTextColor(Color.WHITE);
                                selection.setTransformationMethod(null);

                                selection.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                                if (tst != null) {
                                                        tst.cancel();
                                                }
                                                if (selection.getTag().toString().toLowerCase().equals(q.answer.toString().toLowerCase())) { //correct
//                                                if (answerPos == (int) selection.getTag()) {
                                                        score++;
                                                        getView().scoreTV.setText("" + score);

                                                        if (stump.showAnswers.toLowerCase().equals("no")) {
                                                                tst = Toast.makeText(getView().getContext(), "Correct", Toast.LENGTH_SHORT);
                                                                tst.show();
                                                        } else {
                                                                tst = Toast.makeText(getView().getContext(), "Correct", Toast.LENGTH_SHORT);
                                                                tst.show();
                                                        }

                                                        submitScore();

                                                } else { //wrong
                                                        if (stump.showAnswers.toLowerCase().equals("no")) {
                                                                tst = Toast.makeText(getView().getContext(), "Wrong", Toast.LENGTH_SHORT);
                                                                tst.show();
                                                        } else {
                                                                tst = Toast.makeText(getView().getContext(), "Wrong, answer is " +
                                                                        q.answer, Toast.LENGTH_LONG);
                                                                tst.show();
                                                        }
                                                }
                                                countDownTimer.cancel();
                                                nextQuestion();
//                                       if(q.getCorrectAnswerPosition() == (int)selection.getTag()){
//
//                                       }
                                        }
                                });

                                getView().question.addView(selection);
                                count++;
                        }
                }

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

                if(countDownTimer != null){
                        countDownTimer.cancel();
                }

                if(!GameService.getInstance().fromGames(App.getInstance().previousPresenter)){
                        mainActivity.setVisibleBottombar(View.VISIBLE);
                }

                if (NAME.equals(App.getInstance().currentPresenter)){
                        App.getInstance().currentPresenter = "";
                }
        }

        @Subscribe
        public void unregisterStumpReceiver(UnregisterStumpEvent event){
                if(event.toUnregister) {
                        App.getInstance().stopNetworkMonitoringReceiver();
                }
                else{
                        App.getInstance().startNetworkMonitoringReceiver();
                }
        }


        @Subscribe
        public void updateNetwork(UpdateNetworkEvent event) {
                if (event.isConnected) {
                        getView().messageTV.setVisibility(View.GONE);
                        getView().container.setVisibility(View.VISIBLE);

                        if(!isLoaded){
                                loadStumpQuestion();
                        }
                }
                else{
                        getView().messageTV.setVisibility(View.VISIBLE);
                        getView().container.setVisibility(View.GONE);
                }
        }

        public void loadStumpQuestion(){
                UIHelper.getInstance().showProgressDialog(getView().getContext(), "Loading Stump Question...", false);
                final Handler mainHandler = new Handler(getView().getContext().getMainLooper());
                restClient.getStumpApi().getStumpQuestion(Long.toString(me.getUID()), stump.id)
                        .subscribeOn(Schedulers.io())
                        .subscribe(new Action1<StumpQuestionResponse>() {
                                @Override
                                public void call(final StumpQuestionResponse response) {

                                        mainHandler.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                        isLoaded = true;
                                                        stumpQuestions = response.details.ques;
                                                        score = response.details.score;
                                                        if(getView() == null) return;
                                                        getView().scoreTV.setText("" + score);
                                                        Collections.shuffle(stumpQuestions);
                                                        nextQuestion();

                                                        if(stump.showLeaderboard.toLowerCase().equals("yes")) {
                                                                int myPosition = 0;
                                                                for (StumpLeaderboardItem i : response.details.leaderboard) {
                                                                        if (Long.parseLong(i.userId) == me.getUID()) {
                                                                                break;
                                                                        }
                                                                        myPosition++;
                                                                }

                                                                stumpLeaderboardAdapter = new StumpLeaderboardAdapter(getView().getContext()
                                                                        , response.details.leaderboard, null, bus, myPosition, true);
                                                                getView().leaderboardLV.setAdapter(stumpLeaderboardAdapter);

                                                                getView().leaderboardLV.post(new Runnable() {
                                                                        @Override
                                                                        public void run() {
                                                                                int userPosition = stumpLeaderboardAdapter.getUserPosition();
                                                                                if(getView().leaderboardLV.getChildAt(0) != null) {
                                                                                        int h2 = getView().leaderboardLV.getChildAt(0).getMeasuredHeight();
//                                                                                        getView().leaderboardLV.smoothScrollToPositionFromTop(userPosition,
//                                                                                                (getView().leaderboardLV.getMeasuredHeight() / 2) - (h2 / 2));
                                                                                        smoothScrollToPositionFromTopWithBugWorkAround(getView().leaderboardLV, userPosition,
                                                                                                (getView().leaderboardLV.getMeasuredHeight() / 2) - (h2 / 2), 0);
                                                                                }
                                                                        }
                                                                });

                                                                getView().leaderboardLV.setScrollContainer(false);
                                                                getView().leaderboardLV.setClickable(false);

                                                                getView().leaderboardLV.setOnTouchListener(new View.OnTouchListener() {

                                                                        public boolean onTouch(View v, MotionEvent event) {
                                                                                if (event.getAction() == MotionEvent.ACTION_MOVE) {
                                                                                        return true; // Indicates that this has been handled by you and will not be forwarded further.
                                                                                }
                                                                                return false;
                                                                        }
                                                                });
                                                        }
                                                        else{
                                                                loadSponsor();
                                                        }

                                                        UIHelper.getInstance().dismissProgressDialog();
                                                }
                                        });
                                }
                        }, new Action1<Throwable>() {
                                @Override
                                public void call(Throwable throwable) {
                                        Log.e("Mobisys: ", "cannot get stump list", throwable);

                                        mainHandler.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                        UIHelper.getInstance().dismissProgressDialog();
                                                }
                                        });
                                }
                        });
        }

        public void loadSponsor(){
                getView().scoreContainer.setVisibility(View.GONE);
                getView().line.setVisibility(View.GONE);
                final Handler mainHandler = new Handler(getView().getContext().getMainLooper());
                restClient.getStumpApi().getSponsors(Long.toString(me.getUID()))
                        .subscribeOn(Schedulers.io())
                        .subscribe(new Action1<StumpSponsorResponse>() {
                                @Override
                                public void call(final StumpSponsorResponse response) {

                                        mainHandler.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                        List<SponsorItem> sponsorItems = response.details;
                                                        sponsorItems.add(0, new SponsorItem(R.drawable.sponsors_default));
                                                        stumpLeaderboardAdapter = new StumpLeaderboardAdapter(getView().getContext()
                                                                , null, response.details, bus, 0, false);
                                                        getView().leaderboardLV.setAdapter(stumpLeaderboardAdapter);
                                                        UIHelper.getInstance().dismissProgressDialog();

                                                }
                                        });
                                }
                        }, new Action1<Throwable>() {
                                @Override
                                public void call(Throwable throwable) {
                                        Log.e("Mobisys: ", "cannot get stump list", throwable);

                                        mainHandler.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                        UIHelper.getInstance().dismissProgressDialog();
                                                }
                                        });
                                }
                        });
        }

        public void submitScore(){

                final Handler mainHandler = new Handler(getView().getContext().getMainLooper());
                restClient.getStumpApi().storeScore(Long.toString(me.getUID()), stump.id, Integer.toString(score))
                        .subscribeOn(Schedulers.io())
                        .subscribe(new Action1<StumpStoreScoreResponse>() {
                                @Override
                                public void call(final StumpStoreScoreResponse response) {

                                        mainHandler.post(new Runnable() {
                                                @Override
                                                public void run() {

                                                        if(stump.showLeaderboard.toLowerCase().equals("yes")) {
                                                                int myPosition = 0;
                                                                for (StumpLeaderboardItem i : response.details) {
                                                                        if (Long.parseLong(i.userId) == me.getUID()) {
                                                                                break;
                                                                        }
                                                                        myPosition++;
                                                                }
                                                                stumpLeaderboardAdapter.updates(response.details, myPosition);
                                                                if(getView() == null) return;
                                                                getView().leaderboardLV.setAdapter(stumpLeaderboardAdapter);

                                                                getView().leaderboardLV.post(new Runnable() {
                                                                        @Override
                                                                        public void run() {
                                                                                int userPosition = stumpLeaderboardAdapter.getUserPosition();
                                                                                if(getView().leaderboardLV.getChildAt(0) != null) {
                                                                                        int h2 = getView().leaderboardLV.getChildAt(0).getMeasuredHeight();
                                                                                        getView().leaderboardLV.smoothScrollToPositionFromTop(userPosition,
                                                                                                (getView().leaderboardLV.getMeasuredHeight() / 2) - (h2 / 2));
                                                                                }
                                                                        }
                                                                });
                                                        }
                                                }
                                        });
                                }
                        }, new Action1<Throwable>() {
                                @Override
                                public void call(Throwable throwable) {
                                        Log.e("Mobisys: ", "cannot get stump list", throwable);

                                        mainHandler.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                        UIHelper.getInstance().dismissProgressDialog();
                                                }
                                        });
                                }
                        });
        }

        public void smoothScrollToPositionFromTopWithBugWorkAround(final AbsListView listView,
                                                                   final int position,
                                                                   final int offset,
                                                                   final int duration){

                //the bug workaround involves listening to when it has finished scrolling, and then
                //firing a new scroll to the same position.

                //the bug is the case that sometimes smooth Scroll To Position sort of misses its intended position.
                //more info here : https://code.google.com/p/android/issues/detail?id=36062
                listView.smoothScrollToPositionFromTop(position, offset, duration);
                listView.setOnScrollListener(new AbsListView.OnScrollListener() {

                        @Override
                        public void onScrollStateChanged(AbsListView view, int scrollState) {
                                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                                        listView.setOnScrollListener(null);
                                        listView.smoothScrollToPositionFromTop(position, offset, duration);
                                }

                        }

                        @Override
                        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                        }
                });
        }

        @Subscribe
        public void badgeNotiEvent(BadgeNotiEvent event){
                MasterPointService.getInstance().getBadgesAPI();
                MasterPointService.getInstance().showToolTips(getView().scrollView, event.badgeName);
        }
}
