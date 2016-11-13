package sg.edu.smu.livelabs.mobicom.presenters;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.LinearLayout;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

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
import sg.edu.smu.livelabs.mobicom.ToolTipsWindow;
import sg.edu.smu.livelabs.mobicom.UIHelper;
import sg.edu.smu.livelabs.mobicom.busEvents.BadgeNotiEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.UnregisterVotingEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.UpdateNetworkEvent;
import sg.edu.smu.livelabs.mobicom.flow.Layout;
import sg.edu.smu.livelabs.mobicom.models.User;
import sg.edu.smu.livelabs.mobicom.models.data.GameListEntity;
import sg.edu.smu.livelabs.mobicom.net.RestClient;
import sg.edu.smu.livelabs.mobicom.net.item.PollingItem;
import sg.edu.smu.livelabs.mobicom.net.item.PollingResultItem;
import sg.edu.smu.livelabs.mobicom.net.response.PollingGetDetailsResponse;
import sg.edu.smu.livelabs.mobicom.net.response.PollingResponse;
import sg.edu.smu.livelabs.mobicom.net.response.PollingSimpleResponse;
import sg.edu.smu.livelabs.mobicom.services.DatabaseService;
import sg.edu.smu.livelabs.mobicom.services.GameService;
import sg.edu.smu.livelabs.mobicom.services.MasterPointService;
import sg.edu.smu.livelabs.mobicom.views.VotingView;

/**
 * Created by smu on 22/2/16.
 */
@AutoScreen(
        component = @AutoComponent(dependencies = MainActivity.class, superinterfaces = AppDependencies.class),
        screenAnnotations = Layout.class
)
@DaggerScope(VotingPresenter.class)
@Layout(R.layout.voting_master_view)
public class VotingPresenter extends ViewPresenter<VotingView> {

        private ActionBarOwner actionBarOwner;
        private final RestClient restClient;
        private Bus bus;
        private MainActivity mainActivity;
        private ConnectivityManager cm;
        private User me;

        private String actionBarTitle;

        private SharedPreferences sharedPreferences;
        private SharedPreferences.Editor editor;
        private String votingType;
        private String title;
        public static String NAME = "VotingPresenter";
        private long pollingId;
        private String pollId; //poll id from the server
        private Button[] rb = null;
        private List<String> questionStructure;
        private List<Integer> resourceCustomBtn;
        private boolean isInitialNoInternet;
        private int screenWidth;
        private int screenHeight;

        private boolean isMCQ;
        private boolean isYesNo;
        private boolean isAdmin;

        /** buttonStates
         * 0 = not started
         * 1 = voting started
         * 2 = voting stopped
         */
        private int buttonStates;
        private long timeElapsed;
        private Calendar calendar;

        /**
         *
         * @param restClient
         * @param bus
         * @param actionBarOwner
         * @param pollingId
         */
        public VotingPresenter(RestClient restClient, Bus bus, ActionBarOwner actionBarOwner, MainActivity mainActivity,
                               @ScreenParam long pollingId,  @ScreenParam String title) {
                this.restClient = restClient;
                this.bus = bus;
                this.buttonStates = 0;
                this.actionBarOwner = actionBarOwner;
                this.pollingId = pollingId;
                this.mainActivity = mainActivity;
                this.actionBarTitle = title;

                resourceCustomBtn = new ArrayList<>();
                resourceCustomBtn.add(R.drawable.custom_button_emerald);
                resourceCustomBtn.add(R.drawable.custom_button_orange);
                resourceCustomBtn.add(R.drawable.custom_button_pomegranate);
                resourceCustomBtn.add(R.drawable.custom_button_wisteria);

                isMCQ = false;
                isYesNo = false;
                isAdmin = false;
        }

        @Override
        protected void onLoad(Bundle savedInstanceState) {
                super.onLoad(savedInstanceState);
                Log.d(App.APP_TAG, " VotingPresenter onload");

                GameListEntity game = GameService.getInstance().getGameByKeyword("polling");
                if(game != null &&  !game.getGameName().isEmpty()){
                        actionBarOwner.setConfig(new ActionBarOwner.Config(true, game.getGameName(), null));
                }
                else{
                        actionBarOwner.setConfig(new ActionBarOwner.Config(true, "Polling", null));
                }
//                actionBarOwner.setConfig(new ActionBarOwner.Config(true, actionBarTitle, null));

                mainActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

                WindowManager wm = (WindowManager) getView().getContext().getSystemService(Context.WINDOW_SERVICE);
                Display display = wm.getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);
                screenWidth = size.x;
                screenHeight = size.y;

                me = DatabaseService.getInstance().getMe();

                cm = (ConnectivityManager) getView().getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                App.getInstance().startNetworkMonitoringReceiver();

                for(String s: me.getRole()){
                        if(s.trim().equals("moderator")){
                                isAdmin = true;
                                break;
                        }
                }

                //TODO change if this user is a key note presenter (keynote)
                if(isAdmin) {
                        if(cm.getActiveNetworkInfo() == null){
                                getView().button.setEnabled(false);
                                getView().button.setText(getView().getContext().getResources().getText(R.string.no_internet_connection));
                        }

                        getView().userContainer.setVisibility(View.GONE);
                        getView().keyNoteContainer.setVisibility(View.VISIBLE);
                        getView().noInternetContainer.setVisibility(View.GONE);
                        displayKeyNoteSetting();
                }
                else{
                        if(cm.getActiveNetworkInfo() == null){
                                getView().userContainer.setVisibility(View.GONE);
                                getView().keyNoteContainer.setVisibility(View.GONE);
                                getView().noInternetContainer.setVisibility(View.VISIBLE);
                                isInitialNoInternet = true;
                                return;
                        }
                        else{
                                isInitialNoInternet = false;
                        }

                        getView().userContainer.setVisibility(View.VISIBLE);
                        getView().keyNoteContainer.setVisibility(View.GONE);
                        getView().noInternetContainer.setVisibility(View.GONE);
                        displayUserSetting();
                }
        }


        //this function display the polling setting (to allow user to submit) for the audience point of view
        private void displayUserSetting(){
                getView().userMessageTV.setText(getView().getContext().getResources().getText(R.string.no_poll));

                final Handler mainHandler = new Handler(getView().getContext().getMainLooper());

                restClient.getPollingApi().getPollDetails(Long.toString(pollingId), Long.toString(me.getUID()))
                        .subscribeOn(Schedulers.io())
                        .subscribe(new Action1<PollingGetDetailsResponse>() {
                                @Override
                                public void call(final PollingGetDetailsResponse response) {
                                        if (response.status.equals("success")) {
                                                if (response.details != null && response.details.size() > 0) {

                                                        pollId = response.details.get(0).pollId;
                                                        if(response.details.get(0).hasSubmitted.toLowerCase().equals("false")) {
                                                                mainHandler.post(new Runnable() {
                                                                        @Override
                                                                        public void run() {

                                                                                if (getView() != null) {
                                                                                        LinearLayout.LayoutParams layout = new LinearLayout.LayoutParams((int) (screenWidth * 0.3), LinearLayout.LayoutParams.WRAP_CONTENT);
                                                                                        layout.rightMargin = (int) getView().getContext().getResources().getDimension(R.dimen.inset_spacing);
                                                                                        layout.leftMargin = (int) getView().getContext().getResources().getDimension(R.dimen.inset_spacing);

                                                                                        if (response.details.get(0).type.equals("1")) {

                                                                                                getView().userTitleTV.setText("Q: " + response.details.get(0).title);
                                                                                                getView().userMessageTV.setText("Select your choice!");
                                                                                                //load polling type Y/N or MCQ, and check if the polling is open or not (for new or recreated polling)
                                                                                                rb = new Button[4];
                                                                                                questionStructure = new ArrayList<>();
                                                                                                questionStructure.add("A");
                                                                                                questionStructure.add("B");
                                                                                                questionStructure.add("C");
                                                                                                questionStructure.add("D");

                                                                                                for (int i = 0; i < 4; i++) {
                                                                                                        rb[i] = new Button(getView().getContext());
                                                                                                        rb[i].setBackgroundResource(resourceCustomBtn.get(i));
                                                                                                        rb[i].setLayoutParams(layout);
                                                                                                        rb[i].setTextSize(getView().getContext().getResources().getDimension(R.dimen.text_size_super_large));
                                                                                                        rb[i].setText(questionStructure.get(i));
                                                                                                        rb[i].setTextColor(getView().getContext().getResources().getColor(R.color.white));

                                                                                                        if (i < 2) { //to seperate the 4 button into 2 linearLayout
                                                                                                                getView().userRadioGroupLLOne.addView(rb[i]);
                                                                                                        } else {
                                                                                                                getView().userRadioGroupLLTwo.addView(rb[i]);
                                                                                                        }
                                                                                                }
                                                                                        } else if (response.details.get(0).type.equals("2")) {

                                                                                                getView().userTitleTV.setText("Q: " + response.details.get(0).title);
                                                                                                getView().userMessageTV.setText("Select your choice!");
                                                                                                //TODO if polling is yes no
                                                                                                questionStructure = new ArrayList<>();
                                                                                                questionStructure.add("Y");
                                                                                                questionStructure.add("N");

                                                                                                rb = new Button[2];

                                                                                                for (int i = 0; i < 2; i++) {
                                                                                                        rb[i] = new Button(getView().getContext());

                                                                                                        if (i == 0) {
                                                                                                                rb[i].setBackgroundResource(R.drawable.custom_button_emerald);
                                                                                                        } else if (i == 1) {
                                                                                                                rb[i].setBackgroundResource(R.drawable.custom_button_orange);
                                                                                                        }

                                                                                                        rb[i].setLayoutParams(layout);
                                                                                                        rb[i].setTextSize(getView().getContext().getResources().getDimension(R.dimen.text_size_super_large));
                                                                                                        getView().userRadioGroupLLOne.addView(rb[i]); //the RadioButtons are added to the radioGroup instead of the layout
                                                                                                        rb[i].setText(questionStructure.get(i));

                                                                                                }
                                                                                        }

                                                                                        for (final Button btn : rb) {
                                                                                                btn.setOnClickListener(new View.OnClickListener() {
                                                                                                        @Override
                                                                                                        public void onClick(View v) {
                                                                                                                submitVote(btn.getText().toString());
//                                                                                                        Toast.makeText(getView().getContext(), btn.getText(), Toast.LENGTH_SHORT).show();
                                                                                                        }
                                                                                                });
                                                                                        }
                                                                                }
                                                                        }
                                                                });
                                                        }
                                                        else{ //if user already submitted the polling before
                                                                mainHandler.post(new Runnable() {
                                                                        @Override
                                                                        public void run() {

                                                                                if (getView() != null) {
                                                                                        LinearLayout.LayoutParams layout = new LinearLayout.LayoutParams((int) (screenWidth * 0.3), LinearLayout.LayoutParams.WRAP_CONTENT);
                                                                                        layout.rightMargin = (int) getView().getContext().getResources().getDimension(R.dimen.inset_spacing);
                                                                                        layout.leftMargin = (int) getView().getContext().getResources().getDimension(R.dimen.inset_spacing);

                                                                                        getView().userTitleTV.setText("Q: " + response.details.get(0).title);

                                                                                        getView().userMessageTV.setText("You have already submitted this poll!");
                                                                                        if (response.details.get(0).type.equals("1")) {

                                                                                                //load polling type Y/N or MCQ, and check if the polling is open or not (for new or recreated polling)
                                                                                                rb = new Button[4];
                                                                                                questionStructure = new ArrayList<>();
                                                                                                questionStructure.add("A");
                                                                                                questionStructure.add("B");
                                                                                                questionStructure.add("C");
                                                                                                questionStructure.add("D");

                                                                                                for (int i = 0; i < 4; i++) {
                                                                                                        rb[i] = new Button(getView().getContext());

                                                                                                        if(questionStructure.get(i).toLowerCase().equals(response.details.get(0).userAnswer.toLowerCase())){
                                                                                                                rb[i].setBackgroundResource(resourceCustomBtn.get(i));
                                                                                                        }
                                                                                                        else{
                                                                                                                rb[i].setBackgroundResource(R.drawable.custom_button_grey);
                                                                                                        }
                                                                                                        rb[i].setLayoutParams(layout);
                                                                                                        rb[i].setTextSize(getView().getContext().getResources().getDimension(R.dimen.text_size_super_large));
                                                                                                        rb[i].setText(questionStructure.get(i));
                                                                                                        rb[i].setTextColor(getView().getContext().getResources().getColor(R.color.white));

                                                                                                        if (i < 2) { //to seperate the 4 button into 2 linearLayout
                                                                                                                getView().userRadioGroupLLOne.addView(rb[i]);
                                                                                                        } else {
                                                                                                                getView().userRadioGroupLLTwo.addView(rb[i]);
                                                                                                        }


                                                                                                }
                                                                                        } else if (response.details.get(0).type.equals("2")) {
                                                                                                //TODO if polling is yes no
                                                                                                questionStructure = new ArrayList<>();
                                                                                                questionStructure.add("Y");
                                                                                                questionStructure.add("N");

                                                                                                rb = new Button[2];

                                                                                                for (int i = 0; i < 2; i++) {
                                                                                                        rb[i] = new Button(getView().getContext());

                                                                                                        if(questionStructure.get(i).toLowerCase().equals(response.details.get(0).userAnswer.toLowerCase())){
                                                                                                                if (i == 0) {
                                                                                                                        rb[i].setBackgroundResource(R.drawable.custom_button_emerald);
                                                                                                                } else if (i == 1) {
                                                                                                                        rb[i].setBackgroundResource(R.drawable.custom_button_orange);
                                                                                                                }
                                                                                                        }
                                                                                                        else{
                                                                                                                rb[i].setBackgroundResource(R.drawable.custom_button_grey);
                                                                                                        }

                                                                                                        rb[i].setLayoutParams(layout);
                                                                                                        rb[i].setTextSize(getView().getContext().getResources().getDimension(R.dimen.text_size_super_large));
                                                                                                        getView().userRadioGroupLLOne.addView(rb[i]); //the RadioButtons are added to the radioGroup instead of the layout
                                                                                                        rb[i].setText(questionStructure.get(i));

                                                                                                }
                                                                                        }

                                                                                        for (final Button btn : rb) {
                                                                                                btn.setEnabled(false);
                                                                                        }
                                                                                }
                                                                        }
                                                                });
                                                        }
                                                }

                                        } else {

                                        }
                                }
                        }, new Action1<Throwable>() {
                                @Override
                                public void call(Throwable throwable) {
                                        Log.e("Mobisys: ", "cannot stop polling", throwable);

                                }
                        });






        }

        //this function cater to display the setting for the presenter
        private void displayKeyNoteSetting(){
                sharedPreferences = getView().getContext().getSharedPreferences("Mobicom_MyVotingPref", Context.MODE_PRIVATE);

                getView().messageTV.setText("Select a polling type");
                getView().messageTV.setVisibility(View.VISIBLE);
                setTimeElapsedOnClick();

                LinearLayout.LayoutParams layout = new LinearLayout.LayoutParams((int)screenWidth/3, (int)screenWidth/3);
                layout.rightMargin = 15;
                layout.leftMargin = 15;

                getView().logicalRadioBtn.setLayoutParams(layout);
                getView().mcqRadioBtn.setLayoutParams(layout);

                getView().logicalRadioBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                                isYesNo = true;
                                isMCQ = false;

                                getView().mcqRadioBtn.setBackgroundResource(R.drawable.custom_grey_circle_button);
                                getView().logicalRadioBtn.setBackgroundResource(R.drawable.custom_circle_button_emerald);
                        }
                });

                getView().mcqRadioBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                                isYesNo = false;
                                isMCQ = true;

                                getView().mcqRadioBtn.setBackgroundResource(R.drawable.custom_circle_button_pomegranate);
                                getView().logicalRadioBtn.setBackgroundResource(R.drawable.custom_grey_circle_button);
                        }
                });

                //this part check whether have there been any voting data stored in the preference,
                //if so, reload the selection, time and etc.
                if(sharedPreferences.contains("voting_current_time")){
                        votingType = sharedPreferences.getString("voting_type", "");
                        buttonStates = sharedPreferences.getInt("voting_button_states", 0);
                        title = sharedPreferences.getString("title", "");
                        long previousTimeStamp = sharedPreferences.getLong("voting_current_time", 0);

                        getView().logicalRadioBtn.setEnabled(false);
                        getView().mcqRadioBtn.setEnabled(false);
                        getView().titleTV.setText(title);
                        getView().titleTV.setFocusable(false);
                        getView().titleTV.setEnabled(false);

                        if(votingType.equals("logical")){
                                getView().logicalRadioBtn.setBackgroundResource(R.drawable.custom_grey_circle_button);
                        }
                        else{
                                getView().mcqRadioBtn.setBackgroundResource(R.drawable.custom_grey_circle_button);
                        }

                        getView().logicalRadioBtn.setEnabled(false);
                        getView().mcqRadioBtn.setEnabled(false);

                        getView().errorMsgTV.setVisibility(View.GONE);
                        getView().button.setText("STOP POLLING");
                        getView().button.setBackgroundResource(R.drawable.custom_button_orange);

                        getView().messageTV.setText("Time Elapsed");
                        getView().chronometer.setVisibility(View.VISIBLE);
                        getView().resultTV.setVisibility(View.GONE);

                        if (previousTimeStamp != 0) {
                                getView().chronometer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
                                        @Override
                                        public void onChronometerTick(Chronometer cArg) {
                                                long time = SystemClock.elapsedRealtime() - cArg.getBase();
                                                int h = (int) (time / 3600000);
                                                int m = (int) (time - h * 3600000) / 60000;
                                                int s = (int) (time - h * 3600000 - m * 60000) / 1000;
                                                String hh = h < 10 ? "0" + h : h + "";
                                                String mm = m < 10 ? "0" + m : m + "";
                                                String ss = s < 10 ? "0" + s : s + "";
                                                cArg.setText(hh + ":" + mm + ":" + ss);
                                        }
                                });

                                long elapsedRealtimeOffset = System.currentTimeMillis() - SystemClock.elapsedRealtime();

                                getView().chronometer.setBase(SystemClock.elapsedRealtime());
                                getView().chronometer.setBase(previousTimeStamp - elapsedRealtimeOffset);
                                getView().chronometer.start();

                                setTimeElapsedOnClick();
                        }


                }

        }

        private void setTimeElapsedOnClick(){

                getView().button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                                if (buttonStates == 0) {

                                        title = getView().titleTV.getText().toString();
                                        if (title.isEmpty()) {
                                                getView().errorMsgTV.setVisibility(View.VISIBLE);
                                                getView().errorMsgTV.setText("Title is empty!");
                                                return;
                                        } else {
                                                getView().errorMsgTV.setVisibility(View.GONE);
                                        }

                                        if (!isMCQ && isYesNo) {
                                                votingType = "logical";
                                                startPolling("2");
//                                                Toast.makeText(getView().getContext(), "Yes/No", Toast.LENGTH_SHORT).show();
                                        } else if (!isYesNo && isMCQ) {
                                                votingType = "multiple";
                                                startPolling("1");
//                                                Toast.makeText(getView().getContext(), "MCQ", Toast.LENGTH_SHORT).show();
                                        } else {
                                                getView().errorMsgTV.setVisibility(View.VISIBLE);
                                                getView().errorMsgTV.setText("No polling type selected!");

                                                getView().titleTV.setFocusable(true);
                                                getView().titleTV.setEnabled(true);
                                                return;
                                        }


                                        //TODO send to server to create this voting for this session


                                        //send to backend about the starting of this voting from this user

                                } else if (buttonStates == 1) {
                                        if (sharedPreferences.contains("poll_id")) {
                                                String pollId = sharedPreferences.getString("poll_id", "0");

                                                stopPolling(pollId);
                                        }

                                        //call backend about that this voting had stopped
                                        //retrieve result from backend
                                } else {
                                        getView().button.setText("START POLLING");
                                        getView().button.setBackgroundResource(R.drawable.custom_button);
                                        buttonStates = 0;

                                        getView().errorMsgTV.setEnabled(true);

                                        getView().messageTV.setText("Select a polling type.");
                                        getView().messageTV.setVisibility(View.VISIBLE);
                                        getView().chronometer.setVisibility(View.GONE);
                                        getView().resultTV.setVisibility(View.GONE);
                                        getView().questionGroup.setVisibility(View.VISIBLE);
                                        getView().titleTV.setVisibility(View.VISIBLE);
                                        getView().titleTV.requestFocus();
                                        getView().titleTV.setEnabled(true);
                                        getView().titleTV.setFocusable(true);
                                        getView().titleTV.setFocusableInTouchMode(true);

                                        getView().titleTV.setText(""); //empty it
                                        getView().logicalRadioBtn.setEnabled(true);
                                        getView().mcqRadioBtn.setEnabled(true);

                                        getView().logicalRadioBtn.setBackgroundResource(R.drawable.custom_circle_button_emerald);
                                        getView().mcqRadioBtn.setBackgroundResource(R.drawable.custom_circle_button_pomegranate);

                                        //TODO send to server to stop this voting for this session and get the result
                                }

                        }
                });
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
        public void updateNetwork(UpdateNetworkEvent event){

                if(event.isConnected){

                        if(isAdmin) {
                                if(isInitialNoInternet){
                                        isInitialNoInternet = false;
                                        getView().userContainer.setVisibility(View.GONE);
                                        getView().keyNoteContainer.setVisibility(View.VISIBLE);
                                        getView().noInternetContainer.setVisibility(View.GONE);
                                }

                                getView().button.setEnabled(true);
                                if (buttonStates == 1) {
                                        getView().button.setText("STOP POLLING");
                                }
                                else if (buttonStates == 2) {
                                        getView().button.setText("RESET POLLING");
                                }
                                else{
                                        getView().button.setText("CREATE POLLING");
                                }

                        }
                        else{
                                if(isInitialNoInternet){
                                        displayUserSetting();
                                        isInitialNoInternet = false;
                                }

                                getView().userContainer.setVisibility(View.VISIBLE);
                                getView().keyNoteContainer.setVisibility(View.GONE);
                                getView().noInternetContainer.setVisibility(View.GONE);
                        }
                }
                else{
                        if(isAdmin) {
                                getView().button.setEnabled(false);
                                getView().button.setText(getView().getContext().getResources().getText(R.string.no_internet_connection));

                        }
                        else{
                                getView().userContainer.setVisibility(View.GONE);
                                getView().keyNoteContainer.setVisibility(View.GONE);
                                getView().noInternetContainer.setVisibility(View.VISIBLE);
                        }

                }
        }


        private void startPolling(final String type){
                final Handler mainHandler = new Handler(getView().getContext().getMainLooper());

                UIHelper.getInstance().showProgressDialog(getView().getContext(), "Loading", false);
                restClient.getPollingApi().startPolling(Long.toString(me.getUID()), title, type)
                        .subscribeOn(Schedulers.io())
                        .subscribe(new Action1<PollingResponse>() {
                                @Override
                                public void call(final PollingResponse response) {
                                        if (response.status.equals("success")) {
                                                if (response.details != null) {
                                                        mainHandler.post(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                        getView().errorMsgTV.setVisibility(View.GONE);
                                                                        getView().button.setText("STOP POLLING");
                                                                        getView().button.setBackgroundResource(R.drawable.custom_button_orange);

                                                                        getView().messageTV.setText("Time Elapsed");
                                                                        getView().chronometer.setVisibility(View.VISIBLE);
                                                                        getView().resultTV.setVisibility(View.GONE);

                                                                        getView().logicalRadioBtn.setEnabled(false);
                                                                        getView().mcqRadioBtn.setEnabled(false);

                                                                        getView().titleTV.setFocusable(false);
                                                                        getView().titleTV.setEnabled(false);

                                                                        getView().chronometer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
                                                                                @Override
                                                                                public void onChronometerTick(Chronometer cArg) {
                                                                                        long time = SystemClock.elapsedRealtime() - cArg.getBase();
                                                                                        int h = (int) (time / 3600000);
                                                                                        int m = (int) (time - h * 3600000) / 60000;
                                                                                        int s = (int) (time - h * 3600000 - m * 60000) / 1000;
                                                                                        String hh = h < 10 ? "0" + h : h + "";
                                                                                        String mm = m < 10 ? "0" + m : m + "";
                                                                                        String ss = s < 10 ? "0" + s : s + "";
                                                                                        cArg.setText(hh + ":" + mm + ":" + ss);
                                                                                }
                                                                        });

                                                                        getView().chronometer.setBase(SystemClock.elapsedRealtime());
                                                                        getView().chronometer.start();

                                                                        Date d = new Date();

                                                                        buttonStates = 1;

                                                                        editor = sharedPreferences.edit();
                                                                        editor.putLong("voting_current_time", d.getTime());
                                                                        editor.putString("voting_type", votingType);
                                                                        editor.putInt("voting_button_states", buttonStates);
                                                                        editor.putString("poll_id", response.details.pollId);
                                                                        editor.putString("title", title);
                                                                        editor.commit();

                                                                }
                                                        });
                                                }

                                        }
                                        else{
                                                ToolTipsWindow toolTipsWindow = new ToolTipsWindow(getView().getContext(), "Poll can't be started");
                                                toolTipsWindow.showToolTip(getView(), screenWidth, screenHeight);
                                        }
                                        UIHelper.getInstance().dismissProgressDialog();
                                }
                        }, new Action1<Throwable>() {
                                @Override
                                public void call(Throwable throwable) {
                                        Log.e("Mobisys: ", "cannot start polling", throwable);
                                        UIHelper.getInstance().dismissProgressDialog();

                                        if(getView() != null)
                                                UIHelper.getInstance().showAlert(getView().getContext(), "Unable to start poll. Please try again.");
                                }
                        });
        }


        private void stopPolling(String pollId){
                final Handler mainHandler = new Handler(getView().getContext().getMainLooper());

                User me = DatabaseService.getInstance().getMe();
                restClient.getPollingApi().stopPolling(pollId, Long.toString(me.getUID()))
                        .subscribeOn(Schedulers.io())
                        .subscribe(new Action1<PollingResponse>() {
                                @Override
                                public void call(final PollingResponse response) {
                                        if (response.status.equals("success")) {
                                                if (response.details != null) {
                                                        mainHandler.post(new Runnable() {
                                                                @Override
                                                                public void run() {

                                                                        getView().chronometer.stop();

                                                                        getView().button.setText("RESET VOTING");
                                                                        getView().resultTV.setVisibility(View.VISIBLE);
                                                                        getView().resultTV.setText("The result are as follow:\n\n" +
                                                                                computeResult(response.details));
                                                                        getView().questionGroup.setVisibility(View.GONE);

                                                                        getView().titleTV.setVisibility(View.GONE);

                                                                        buttonStates = 2;

                                                                        editor = sharedPreferences.edit();
                                                                        if(sharedPreferences.contains("voting_current_time")){

                                                                                editor.remove("voting_current_time");
                                                                        }

                                                                        if(sharedPreferences.contains("voting_type")){
                                                                                editor.remove("voting_type");
                                                                        }

                                                                        if(sharedPreferences.contains("voting_button_states")){
                                                                                editor.remove("voting_button_states");
                                                                        }

                                                                        if(sharedPreferences.contains("poll_id")){

                                                                                editor.remove("poll_id");
                                                                        }

                                                                        editor.apply();

                                                                }
                                                        });
                                                }

                                        }
                                        else{
                                                ToolTipsWindow toolTipsWindow = new ToolTipsWindow(getView().getContext(), "Polling can't be stopped");
                                                toolTipsWindow.showToolTip(getView(), screenWidth, screenHeight);
                                        }
                                }
                        }, new Action1<Throwable>() {
                                @Override
                                public void call(Throwable throwable) {
                                        Log.e("Mobisys: ", "cannot stop polling", throwable);
                                        if(getView() != null)
                                                UIHelper.getInstance().showAlert(getView().getContext(), "Unable to stop poll. Please try again.");
                                }
                        });
        }

        private String computeResult(PollingItem details){

                List<PollingResultItem> pollingResultItems = details.pollingResultItemList;
                String result = "Total Submission: " + details.totalPollCount + "\n";

                if(votingType.equals("logical")){
                        Collections.sort(pollingResultItems, new Comparator<PollingResultItem>() {
                                public int compare(PollingResultItem p1, PollingResultItem p2) {
                                        return p2.answer.compareTo(p1.answer);
                                }
                        });

                        float totalParticipation = 0;

                        for(PollingResultItem item: pollingResultItems) {
                                totalParticipation += Float.parseFloat(item.count);
                        }

                        for(PollingResultItem item: pollingResultItems) {
                                switch (item.answer) {
                                        case "y":
                                                result += "\n\nYes: " +  String.format("%.2f",((Float.parseFloat(item.count) / totalParticipation) * 100)) + "%"; //show precentage
                                                break;
                                        case "n":
                                                result += "\n\nNo: " +  String.format("%.2f",((Float.parseFloat(item.count) / totalParticipation) * 100)) + "%"; //show precentage
                                                break;
                                }
                        }
                }
                else{
                        Collections.sort(pollingResultItems, new Comparator<PollingResultItem>() {
                                public int compare(PollingResultItem p1, PollingResultItem p2) {
                                        return p1.answer.compareTo(p2.answer);
                                }
                        });;

                        float totalParticipation = 0;

                        for(PollingResultItem item: pollingResultItems) {
                                totalParticipation += Float.parseFloat(item.count);
                        }

                        for(PollingResultItem item: pollingResultItems) {
                                switch (item.answer) {
                                        case "a":
                                                result += "\nA: " +  String.format("%.2f",((Float.parseFloat(item.count) / totalParticipation) * 100)) + "%"; //show precentage
                                                break;
                                        case "b":
                                                result += "\nB: " +  String.format("%.2f",((Float.parseFloat(item.count) / totalParticipation) * 100)) + "%"; //show precentage
                                                break;
                                        case "c":
                                                result += "\nC: " +  String.format("%.2f",((Float.parseFloat(item.count) / totalParticipation) * 100)) + "%"; //show precentage
                                                break;
                                        case "d":
                                                result += "\nD: " +  String.format("%.2f",((Float.parseFloat(item.count) / totalParticipation) * 100)) + "%"; //show precentage
                                                break;
                                }
                        }
                }


                return result;
        }

        //this function cater to allow user to submit their vote
        private void submitVote(String answer){
                final Handler mainHandler = new Handler(getView().getContext().getMainLooper());

                restClient.getPollingApi().submitPoll(Long.toString(me.getUID()), pollId, answer.toLowerCase())
                        .subscribeOn(Schedulers.io())
                        .subscribe(new Action1<PollingSimpleResponse>() {
                                @Override
                                public void call(final PollingSimpleResponse response) {
                                        if (response.status.equals("success")) {
                                                if (response.details != null) {
                                                        mainHandler.post(new Runnable() {
                                                                @Override
                                                                public void run() {

                                                                        if(getView() != null){
//                                                                                MasterPointService.getInstance().addPoint(MasterPointService.getInstance().POLLING, getView().userContainer);


                                                                                getView().userRadioGroupLLOne.setVisibility(View.GONE);
                                                                                getView().userRadioGroupLLTwo.setVisibility(View.GONE);
                                                                                getView().userMessageTV.setText(getView().getContext().getResources().getText(R.string.complete));
                                                                        }

                                                                }
                                                        });
                                                }
                                        }
                                        else{
                                                if (response.details != null) {
                                                        mainHandler.post(new Runnable() {
                                                                @Override
                                                                public void run() {

                                                                        if(getView() != null){
                                                                                getView().userRadioGroupLLOne.setVisibility(View.GONE);
                                                                                getView().userRadioGroupLLTwo.setVisibility(View.GONE);
                                                                                getView().userMessageTV.setText(response.details);
                                                                        }

                                                                }
                                                        });
                                                }
                                        }
                                }
                        }, new Action1<Throwable>() {
                                @Override
                                public void call(Throwable throwable) {
                                        Log.e("Mobisys: ", "cannot submit polling", throwable);
                                        if(getView() != null)
                                                UIHelper.getInstance().showAlert(getView().getContext(), "Unable to submit poll. Please try again.");
                                }
                        });
        }

        @Subscribe
        public void badgeNotiEvent(BadgeNotiEvent event){
                MasterPointService.getInstance().getBadgesAPI();
                MasterPointService.getInstance().showToolTips(getView().userContainer, event.badgeName);
        }
}
