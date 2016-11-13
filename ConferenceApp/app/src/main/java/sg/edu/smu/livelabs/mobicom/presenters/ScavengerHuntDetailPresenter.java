package sg.edu.smu.livelabs.mobicom.presenters;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import autodagger.AutoComponent;
import automortar.AutoScreen;
import automortar.ScreenParam;
import flow.Flow;
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
import sg.edu.smu.livelabs.mobicom.adapters.ScavengerTeamMemberGridAdapter;
import sg.edu.smu.livelabs.mobicom.busEvents.BadgeNotiEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.ScavengerUpdateDetailEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.UnregisterScavengerEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.UpdateNetworkEvent;
import sg.edu.smu.livelabs.mobicom.flow.Layout;
import sg.edu.smu.livelabs.mobicom.models.TeamMember;
import sg.edu.smu.livelabs.mobicom.models.User;
import sg.edu.smu.livelabs.mobicom.models.data.ScavengerEntity;
import sg.edu.smu.livelabs.mobicom.models.data.ScavengerGroupDetailEntity;
import sg.edu.smu.livelabs.mobicom.net.RestClient;
import sg.edu.smu.livelabs.mobicom.net.item.ScavengerGroupItem;
import sg.edu.smu.livelabs.mobicom.net.response.IceBreakerFriendDetailResponse;
import sg.edu.smu.livelabs.mobicom.net.response.ScavengerGroupResponse;
import sg.edu.smu.livelabs.mobicom.net.response.SimpleResponse;
import sg.edu.smu.livelabs.mobicom.qrScanner.QRScannerService;
import sg.edu.smu.livelabs.mobicom.services.DatabaseService;
import sg.edu.smu.livelabs.mobicom.services.GameService;
import sg.edu.smu.livelabs.mobicom.services.MasterPointService;
import sg.edu.smu.livelabs.mobicom.services.ScavengerService;
import sg.edu.smu.livelabs.mobicom.util.Util;
import sg.edu.smu.livelabs.mobicom.views.ScavengerHuntDetailView;

/**
 * Created by johnlee on 22/2/16.
 */
@AutoScreen(
        component = @AutoComponent(dependencies = MainActivity.class, superinterfaces = AppDependencies.class),
        screenAnnotations = Layout.class
)
@DaggerScope(ScavengerHuntDetailPresenter.class)
@Layout(R.layout.scavenger_detail_view)
public class ScavengerHuntDetailPresenter extends ViewPresenter<ScavengerHuntDetailView>{

        private final RestClient restClient;
        private Bus bus;
        private ScavengerEntity scavengerHunt;
        public static String NAME = "ScavengerHuntDetailPresenter";

        private User me;
        private ScavengerTeamMemberGridAdapter memberGridAdapter;
        private  ConnectivityManager cm;
        private MainActivity mainActivity;
        private ActionBarOwner actionBarOwner;
        private List<ScavengerGroupDetailEntity> member;
        private int maxGroupSize;
        boolean isLeader = false;
        private String groupId; //this is gotten from the server every time we load the page
        private boolean isHuntStarted = false;
        private boolean isMeScanned = false;
        private int waitForHowManyPpl = 3;
        private boolean isSubmitHuntResultCalled = false;
        private boolean isFirstTimeCalling = true;
        private boolean hasMeSubmitted = false;

        private Timer timer;
        private Handler mTimerHandler = new Handler();

        public ScavengerHuntDetailPresenter(RestClient restClient, Bus bus, ActionBarOwner actionBarOwner
                , MainActivity mainActivity, @ScreenParam ScavengerEntity scavengerHunt) {
                this.restClient = restClient;
                this.bus = bus;
                this.scavengerHunt = scavengerHunt;
                this.mainActivity = mainActivity;
                this.actionBarOwner = actionBarOwner;
                this.maxGroupSize = 3;
                this.groupId = "0"; //default
        }

        @Override
        protected void onLoad(Bundle savedInstanceState) {
                super.onLoad(savedInstanceState);
                Log.d(App.APP_TAG, " ScavengerHuntDetailPresenter onload");

                me = DatabaseService.getInstance().getMe();
                cm = (ConnectivityManager) getView().getContext().getSystemService(Context.CONNECTIVITY_SERVICE);

                actionBarOwner.setConfig(new ActionBarOwner.Config(true, scavengerHunt.getTitle(), null));

                App.getInstance().startNetworkMonitoringReceiver();

                if(scavengerHunt.getType().equals("single")) { //setup for single layout (no team)
                        getView().teamMemberGV.setVisibility(View.GONE);
                        getView().teamMemberHeader.setVisibility(View.GONE);
                        getView().startBtn.setVisibility(View.GONE);
                        getView().disbandBtn.setVisibility(View.GONE);
                        getView().beforeStartingLL.setVisibility(View.GONE);
                        getView().afterStartingLL.setVisibility(View.VISIBLE);
                        getView().scanBtn.setVisibility(View.VISIBLE);
                }
                else{
                        //call server to check (in case notification didn't reach) whether is this hunt for the user been added as group
                        if (cm.getActiveNetworkInfo() != null) {
                                isFirstTimeCalling = false;
                                getHuntMember();
                        }
                }

                //here are all the common features
                getView().descriptionTV.setText(scavengerHunt.getDescription());

                if(scavengerHunt.getPhoto() != null && !scavengerHunt.getPhoto().isEmpty()){
                        try {
                                Picasso.with(getView().getContext()).load(scavengerHunt.getPhoto())
                                        .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                                        .networkPolicy(NetworkPolicy.NO_CACHE)
                                        .resize(512, 512)
                                        .into(getView().photoHintsTV);
                        }
                        catch(Throwable e){//catch OOM
                                System.gc();
                        }
                }

                if(scavengerHunt.getType().equals("single")) {
                        getView().hintTV.setVisibility(View.GONE);
                }

                getView().startBtn.setEnabled(false);
                getView().scanBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                                if(scavengerHunt.getType().equals("single")) {
                                        QRScannerService.getInstance().requestScan(mainActivity, new Action1<String>() {
                                                @Override
                                                public void call(String s) {
                                                        //QR code result
                                                        if (s.equals(scavengerHunt.getQrCode())) {
                                                                submitHuntResult();
                                                        } else {
                                                                showDialog2("Hmmm, this does not seem to be the correct answer");
                                                        }
                                                }
                                        });
                                }
                                else {

                                        UIHelper.getInstance().showProgressDialog(getView().getContext(), "Loading...", false);
                                        //here we need to do a check, to ensure scan qr is officially on (if no noti)
                                        final Handler mainHandler = new Handler(getView().getContext().getMainLooper());
                                        restClient.getScavengerApi()
                                                .getHuntMembers(Long.toString(me.getUID()), Long.toString(scavengerHunt.getHuntId())).subscribeOn(Schedulers.io())
                                                .subscribe(new Action1<ScavengerGroupResponse>() {
                                                        @Override
                                                        public void call(final ScavengerGroupResponse response) {
                                                                UIHelper.getInstance().dismissProgressDialog();
                                                                if (response.status.equals("success")) {
                                                                        if (response.details.size() > 0) {
                                                                                if (response.details.get(0).isHuntStarted.equals("true")) {
                                                                                        mainHandler.post(new Runnable() {
                                                                                                @Override
                                                                                                public void run() {
                                                                                                        QRScannerService.getInstance().requestScan(mainActivity, new Action1<String>() {
                                                                                                                @Override
                                                                                                                public void call(String s) {
                                                                                                                        //QR code result
                                                                                                                        if (s.equals(scavengerHunt.getQrCode())) {
                                                                                                                                submitHuntResult();
                                                                                                                        } else {
                                                                                                                                showDialog2("Hmmm, this does not seem to be the correct answer");
                                                                                                                        }
                                                                                                                }
                                                                                                        });
                                                                                                }
                                                                                        });

                                                                                }
                                                                        } else {
                                                                                mainHandler.post(new Runnable() {
                                                                                        @Override
                                                                                        public void run() {
                                                                                                showResetDialog("Group have been disband by the leader.");
                                                                                        }
                                                                                });
                                                                        }
                                                                } else {
                                                                        mainHandler.post(new Runnable() {
                                                                                @Override
                                                                                public void run() {
                                                                                        showResetDialog("Group have been disband by the leader.");
                                                                                }
                                                                        });
                                                                }
                                                        }
                                                }, new Action1<Throwable>() {
                                                        @Override
                                                        public void call(Throwable throwable) {
                                                                UIHelper.getInstance().dismissProgressDialog();
                                                                mainHandler.post(new Runnable() {
                                                                        @Override
                                                                        public void run() {
                                                                                showResetDialog("Please check your network.");
                                                                        }
                                                                });
                                                                Log.e("Mobisys: ", "cannot get member in scan btn", throwable);
                                                        }
                                                });
                                }
                        }
                });

                startUpdateTimer();
        }

        /**
         * This will load all functionalities for the leader
         * Common feature will be in the onLoad
         */
        private void loadLeaderContent(boolean isHuntStarted){
                loadMemberList(false);
                isSubmitHuntResultCalled = false; //reset after this user scan the hint QR (he got scan or not just reset)

                if(scavengerHunt.getIsStarted()){ //hunt started
                        getView().beforeStartingLL.setVisibility(View.GONE);
                        getView().afterStartingLL.setVisibility(View.VISIBLE);
                        ScavengerService.getInstance().updateIsStarted(scavengerHunt.getHuntId(), true);
                        //disable remove btn
                        int size = memberGridAdapter.getCount();
                        for (int i = 0; i < size; i++) {
                                View view = getViewByPosition(i, getView().teamMemberGV);
                                Button removeBtn = (Button) view.findViewById(R.id.remove);
                                Button addBtn = (Button) view.findViewById(R.id.add);

                                addBtn.setBackgroundResource(R.drawable.custom_button);
                                addBtn.setEnabled(false);

                                removeBtn.setBackgroundResource(R.drawable.custom_button);
                                removeBtn.setEnabled(false);

                        }
                }

                getView().teamMemberGV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                                if (cm.getActiveNetworkInfo() != null) {
                                        if (memberGridAdapter.getTeamMember().get(position) == null) {

                                                QRScannerService.getInstance().requestScan(mainActivity, new Action1<String>() {
                                                        @Override
                                                        public void call(String s) {
                                                                getCheckQR(s, position);
//                                                                Toast.makeText(getView().getContext(), s, Toast.LENGTH_SHORT).show();

                                                        }
                                                });
                                        }
                                }
                        }
                });

                getView().startBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                                storeStartHuntAPI();
                        }
                });

                getView().disbandBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                                showDialog("Are you sure you want to disband your current group?", false);
                        }
                });

                if(member != null && member.size() == maxGroupSize - 1){ //need to deduct 1 for the leader
                        getView().startBtn.setBackgroundResource(R.drawable.custom_button);
                        getView().startBtn.setEnabled(true);
                }

                if(isHuntStarted){
                        if(isMeScanned){ //update the scan button text to display how many member not yet scanned
                                getView().scanBtn.setEnabled(false);
                                getView().scanBtn.setText(waitForHowManyPpl + " member left");
                                getView().scanBtn.setBackgroundResource(R.drawable.custom_button_grey);
                        }
                        else{
                                getView().scanBtn.setEnabled(true);
                                getView().scanBtn.setText("Scan QR Code");
                                getView().scanBtn.setBackgroundResource(R.drawable.custom_button);
                        }

                        getView().beforeStartingLL.setVisibility(View.GONE);
                        getView().afterStartingLL.setVisibility(View.VISIBLE);
                        ScavengerService.getInstance().updateIsStarted(scavengerHunt.getHuntId(), true);

                }
                else{
                        getView().beforeStartingLL.setVisibility(View.VISIBLE);
                        getView().afterStartingLL.setVisibility(View.GONE);
                        getView().scanBtn.setText("Scan QR Code");
                        ScavengerService.getInstance().updateIsStarted(scavengerHunt.getHuntId(), false);
                }
        }

        /**
         * This is the member view, thus, all functionalities will be disable until the leader remove the user or disband the group
         * Common feature will be in the onLoad
         */
        private void loadMemberContent(boolean isHuntStarted){
                loadMemberList(true);
                isSubmitHuntResultCalled = false; //reset after this user scan the hint QR (he got scan or not just reset)

                getView().startBtn.setBackgroundResource(R.drawable.custom_button_grey);
                getView().startBtn.setEnabled(false);
                getView().disbandBtn.setVisibility(View.GONE);

                if(isHuntStarted){
                        if(isMeScanned){ //update the scan button text to display how many member not yet scanned
                                getView().scanBtn.setEnabled(false);
                                getView().scanBtn.setText(waitForHowManyPpl + " member left");
                                getView().scanBtn.setBackgroundResource(R.drawable.custom_button_grey);
                        }
                        else{
                                getView().scanBtn.setEnabled(true);
                                getView().scanBtn.setText("Scan QR Code");
                                getView().scanBtn.setBackgroundResource(R.drawable.custom_button);
                        }

                        getView().beforeStartingLL.setVisibility(View.GONE);
                        getView().afterStartingLL.setVisibility(View.VISIBLE);

                        ScavengerService.getInstance().updateIsStarted(scavengerHunt.getHuntId(), true);
                }
                else{
                        getView().beforeStartingLL.setVisibility(View.VISIBLE);
                        getView().afterStartingLL.setVisibility(View.GONE);
                        getView().scanBtn.setText("Scan QR Code");
                        ScavengerService.getInstance().updateIsStarted(scavengerHunt.getHuntId(), false);
                }
        }

        /**
         * This is to load the list of member list the user have in this hunt
         */
        private void loadMemberList(final boolean isMember){
                //To load from local db whether is there any team member
                if(member != null && member.size() > 0){
                        member.clear();
                }
                member = ScavengerService.getInstance().getGroupMemberOfTheHunt(scavengerHunt.getHuntId());

                List<TeamMember> teamMembers = new ArrayList<>();
                List<User> userlist = new ArrayList<>();
                teamMembers.add(new TeamMember(me, true));

                for(ScavengerGroupDetailEntity m: member){
                        boolean hasDuplicate = false;
                        //to check there is no duplicate member
                        for(User user: userlist){
                                if(user.getUID() == m.getUserId()){
                                        ScavengerService.getInstance().deleteGroupMemberOfAHuntByEntity(m);
                                        break;
                                }
                        }

                        if(!hasDuplicate) {
                                User u = new User();
                                u.setAvatar(m.getAvatarId());
                                u.setUID(m.getUserId());
                                u.setName(m.getName());
                                teamMembers.add(new TeamMember(u, false));
                                userlist.add(u);
                        }
                }

                if(teamMembers.size() < maxGroupSize){
                        for(int i=teamMembers.size(); i< maxGroupSize; i++){
                                teamMembers.add(null);
                        }
                }

                memberGridAdapter = new ScavengerTeamMemberGridAdapter(getView().getContext(), teamMembers,
                        maxGroupSize, isMember, isHuntStarted, new BtnClickListener() {

                        @Override
                        public void onBtnClick(int position) { //remove
                                // Call your function which creates and shows the dialog here
                                long oldMemberUserId = memberGridAdapter.removeMember(position);
                                View view = getViewByPosition(position, getView().teamMemberGV);
                                ImageView avatar = (ImageView) view.findViewById(R.id.avatar_image);
                                TextView nameTV = (TextView) view.findViewById(R.id.name);
                                TextView nameShortFormTV = (TextView) view.findViewById(R.id.name_short_form);
                                Button removeBtn = (Button) view.findViewById(R.id.remove);
                                Button addBtn = (Button) view.findViewById(R.id.add);

                                Picasso.with(getView().getContext()).load(R.drawable.icon_no_profile)
                                        .memoryPolicy(MemoryPolicy.NO_CACHE)
                                        .networkPolicy(NetworkPolicy.NO_CACHE)
                                        .into(avatar);
                                nameTV.setText("");
                                removeBtn.setVisibility(View.GONE);
                                addBtn.setVisibility(View.VISIBLE);
                                nameShortFormTV.setVisibility(View.GONE);

                                if(memberGridAdapter.getNumberOfMember() == 3){
                                        getView().startBtn.setBackgroundResource(R.drawable.custom_button);
                                        getView().startBtn.setEnabled(true);
                                }
                                else{
                                        getView().startBtn.setBackgroundResource(R.drawable.custom_button_grey);
                                        getView().startBtn.setEnabled(false);
                                }

//                                ScavengerService.getInstance().deleteGroupMemberOfAHunt(scavengerHunt.getHuntId(), oldMemberUserId);
                                removeMemberfromGroup(oldMemberUserId);
                        }

                        @Override
                        public void onAddBtnClick(final int position) { //add
                                // Call your function which creates and shows the dialog here
                                if (cm.getActiveNetworkInfo() != null) {
                                        //TODO need to call server to check (in case notification didn't reach) whether is this hunt for the user been added as group
                                        //if so load the team content and not allow user to create his/her group

                                        if (memberGridAdapter.getTeamMember().get(position) == null) {
                                                QRScannerService.getInstance().requestScan(mainActivity, new Action1<String>() {
                                                        @Override
                                                        public void call(String s) {
                                                                getCheckQR(s, position);
//                                                                Toast.makeText(getView().getContext(), s, Toast.LENGTH_SHORT).show();

                                                        }
                                                });
                                        }
                                }
                        }

                });

                getView().teamMemberGV.setAdapter(memberGridAdapter);
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
                endScavengerTimer();
                bus.unregister(this);
                if(!GameService.getInstance().fromGames(App.getInstance().previousPresenter)){
                        mainActivity.setVisibleBottombar(View.VISIBLE);
                }
                if (NAME.equals(App.getInstance().currentPresenter)){
                        App.getInstance().currentPresenter = "";
                }
        }

        //on click for remove button
        public interface BtnClickListener {
                public abstract void onBtnClick(int position);
                public abstract void onAddBtnClick(int position);
        }

        public View getViewByPosition(int pos, GridView gridView) {
                final int firstListItemPosition = gridView.getFirstVisiblePosition();
                final int lastListItemPosition = firstListItemPosition + gridView.getChildCount() - 1;

                if (pos < firstListItemPosition || pos > lastListItemPosition ) {
                        return gridView.getAdapter().getView(pos, null, gridView);
                } else {
                        final int childIndex = pos - firstListItemPosition;
                        return gridView.getChildAt(childIndex);
                }
        }


        /**
         * Scanning member QR code
         * @param qrCode
         * @param position
         */
        private void getCheckQR(final String qrCode, final int position){
                final Handler mainHandler2 = new Handler(getView().getContext().getMainLooper());
                //first check in case no notification, whether user already added to a group
                restClient.getScavengerApi()
                        .getHuntMembers(Long.toString(me.getUID()), Long.toString(scavengerHunt.getHuntId())).subscribeOn(Schedulers.io())
                        .subscribe(new Action1<ScavengerGroupResponse>() {
                                @Override
                                public void call(final ScavengerGroupResponse response) {
                                        if(response.status.equals("success")){
                                                boolean isUserLeader = false;
                                                if(response.details.size() > 0){
                                                        for(ScavengerGroupItem g: response.details){
                                                                if(g.userId.equals(Long.toString(me.getUID())) && g.isLeader.toLowerCase().equals("true")){
                                                                        isUserLeader = true;
                                                                        break;
                                                                }
                                                        }
                                                }

                                                if(response.details.size() < 1 || isUserLeader){ //state that user have not enter to any group yet, if user is leader, then it's okay
                                                        //check QR is it a valid user
                                                        final Handler mainHandler = new Handler(getView().getContext().getMainLooper());
                                                        UIHelper.getInstance().showProgressDialog(getView().getContext(), "Processing team member...", false);
                                                        restClient.getIceBreakerApi().getUserFromQR(qrCode, Long.toString(me.getUID()))
                                                                .subscribeOn(Schedulers.io())
                                                                .subscribe(new Action1<IceBreakerFriendDetailResponse>() {
                                                                        @Override
                                                                        public void call(final IceBreakerFriendDetailResponse response) {
                                                                                if (response.status.equals("success")) {

                                                                                        if (response.details != null && response.details.size() > 0) {

                                                                                                mainHandler.post(new Runnable() {
                                                                                                        @Override
                                                                                                        public void run() {
                                                                                                                if (getView() != null) {
                                                                                                                        //check if the added user is not user him/herself
                                                                                                                        if (Long.parseLong(response.details.get(0).userId) == me.getUID()) {
                                                                                                                                showDialog2("You are already inside the team");
                                                                                                                                UIHelper.getInstance().dismissProgressDialog();
                                                                                                                                return;
                                                                                                                        }

                                                                                                                        //reload for any new update (newly added user)
                                                                                                                        member = ScavengerService.getInstance().getGroupMemberOfTheHunt(scavengerHunt.getHuntId());
                                                                                                                        //check if the added user is already added before
                                                                                                                        for (ScavengerGroupDetailEntity m : member) {
                                                                                                                                if (Long.parseLong(response.details.get(0).userId) == m.getUserId()) {
                                                                                                                                        showDialog2("This user is already part of the team");
                                                                                                                                        UIHelper.getInstance().dismissProgressDialog();
                                                                                                                                        return;
                                                                                                                                }
                                                                                                                        }


                                                                                                                        addMemberToGroup(position, Long.parseLong(response.details.get(0).userId),
                                                                                                                                response.details.get(0).name, response.details.get(0).avatar,
                                                                                                                                response.details.get(0).email, response.details.get(0).desig);
                                                                                                                }

                                                                                                        }
                                                                                                });
                                                                                        } else {
                                                                                                mainHandler.post(new Runnable() {
                                                                                                        @Override
                                                                                                        public void run() {
                                                                                                                UIHelper.getInstance().dismissProgressDialog();
                                                                                                                showDialog2("This is not a valid user");
                                                                                                        }
                                                                                                });
                                                                                        }
                                                                                }
                                                                        }
                                                                }, new Action1<Throwable>() {
                                                                        @Override
                                                                        public void call(Throwable throwable) {
                                                                                mainHandler.post(new Runnable() {
                                                                                        @Override
                                                                                        public void run() {
                                                                                                UIHelper.getInstance().dismissProgressDialog();
                                                                                        }
                                                                                });
                                                                                Log.e("Mobisys: ", "cannot get qr details", throwable);
                                                                        }
                                                                });
                                                }
                                                else{
                                                        getHuntMember(); //refresh
                                                        Handler mainHandler = new Handler(getView().getContext().getMainLooper());
                                                        mainHandler.post(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                        showDialog2("You have been added to a group.");
                                                                }
                                                        });

                                                }
                                        }
                                        else{
                                                getHuntMember(); //refresh
                                                Handler mainHandler = new Handler(getView().getContext().getMainLooper());
                                                mainHandler.post(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                                showDialog2("You have been added to a group.");
                                                        }
                                                });
                                        }
                                }

                        }, new Action1<Throwable>() {
                                   @Override
                                   public void call(Throwable throwable) {
                                           UIHelper.getInstance().dismissProgressDialog();
                                           mainHandler2.post(new Runnable() {
                                                   @Override
                                                   public void run() {
                                                           getHuntMember();
                                                   }
                                           });
                                           Log.e("Mobisys: ", "cannot get member in add btn", throwable);
                                   }
                        });


        }



        //this helps to show detail of the friend scanned from the QR code

        /**
         *
         * @param msg
         * @param isNotification to indicate if it is for QR code or disband
         */
        public void showDialog(String msg, final Boolean isNotification){

                final Dialog dialog = new Dialog(getView().getContext());
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.dialog_box_scavenger);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

                Button disbandBtn = (Button) dialog.findViewById(R.id.disbandBtn);
                Button closeBtn = (Button) dialog.findViewById(R.id.closeBtn);
                TextView messageTV = (TextView) dialog.findViewById(R.id.message);
                final ImageView personIV = (ImageView) dialog.findViewById(R.id.person);

                final RelativeLayout.LayoutParams layout = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                layout.addRule(RelativeLayout.ALIGN_RIGHT, R.id.container);
                layout.addRule(RelativeLayout.ALIGN_TOP, R.id.container);
                layout.rightMargin = -1 *(int) getView().getContext().getResources().getDimension(R.dimen.scavenger_right_margin);

                personIV.setLayoutParams(layout);

                if(isNotification){
                        closeBtn.setText("Dismiss");
                        disbandBtn.setText("Join");
                }

                messageTV.setText(msg);

                closeBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                                dialog.dismiss();
                        }
                });

                disbandBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                                if(!isNotification) { //to show the disband
                                        checkBeforeDisband();
                                }
                                else{

                                }
                                dialog.dismiss();
                        }
                });

                dialog.show();
        }

    /**
     * This check the getHuntMembers if everyone have submitted, in case the leader didn't get the notification
     */
    public void checkBeforeDisband(){
                restClient.getScavengerApi()
                        .getHuntMembers(Long.toString(me.getUID()), Long.toString(scavengerHunt.getHuntId())).subscribeOn(Schedulers.io())
                        .subscribe(new Action1<ScavengerGroupResponse>() {
                                @Override
                                public void call(final ScavengerGroupResponse response) {
                                        if(response.status.equals("success")){
                                                if(response.details != null && response.details.size() > 0){
                                                        int numberOfSubmitted = 0;
                                                        for(ScavengerGroupItem s: response.details){
                                                                if(s.submitted.toLowerCase().equals("true")){
                                                                        numberOfSubmitted++;
                                                                }
                                                        }

                                                        if(numberOfSubmitted >= 3){
                                                                final Handler mainHandler = new Handler(getView().getContext().getMainLooper());
                                                                mainHandler.post(new Runnable() {
                                                                        @Override
                                                                        public void run() {
                                                                                if(getView() != null) {
                                                                                        ScavengerService.getInstance().updateIsComplete(scavengerHunt.getHuntId(), true);
                                                                                        Flow.get(getView().getContext()).goBack();
                                                                                }
                                                                        }
                                                                });
                                                        }
                                                        else{
                                                                disband2();
                                                        }
                                                }
                                                else{
                                                        disband2();
                                                }
                                        }
                                        else{
                                                disband2();
                                        }

                                }
                        }, new Action1<Throwable>() {
                                @Override
                                public void call(Throwable throwable) {
                                        disband2();
                                        Log.e("Mobisys: ", "cannot get member - disband", throwable);
                                }
                        });
        }

        public void disband2(){
                final Handler mainHandler = new Handler(getView().getContext().getMainLooper());
                mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                                ScavengerService.getInstance().updateIsStarted(scavengerHunt.getHuntId(), false); //need to update that the user stop this hunt
                                memberGridAdapter.disband();

                                disbandGroupAPI();

                                ScavengerService.getInstance().deleteAllGroupMemberOfAHunt(scavengerHunt.getHuntId());
                                ScavengerService.getInstance().updateIsStarted(scavengerHunt.getHuntId(), false);
                        }
                });

        }

        //this helps to show message
        public void showDialog2(String msg){

                final Dialog dialog = new Dialog(getView().getContext());
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.dialog_box_scavenger);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

                Button disbandBtn = (Button) dialog.findViewById(R.id.disbandBtn);
                disbandBtn.setVisibility(View.GONE);
                Button closeBtn = (Button) dialog.findViewById(R.id.closeBtn);
                TextView messageTV = (TextView) dialog.findViewById(R.id.message);
                final ImageView personIV = (ImageView) dialog.findViewById(R.id.person);

                final RelativeLayout.LayoutParams layout = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                layout.addRule(RelativeLayout.ALIGN_RIGHT, R.id.container);
                layout.addRule(RelativeLayout.ALIGN_TOP, R.id.container);
                layout.rightMargin = -1 *(int) getView().getContext().getResources().getDimension(R.dimen.scavenger_right_margin);

                personIV.setLayoutParams(layout);
                System.out.println("what is this margin " + layout.rightMargin );
                messageTV.setText(msg);

                closeBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                                dialog.dismiss();
                        }
                });

                dialog.show();
        }

        public void showResetDialog(String msg){
                final Dialog dialog = new Dialog(getView().getContext());
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.dialog_box_scavenger);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

                Button disbandBtn = (Button) dialog.findViewById(R.id.disbandBtn);
                disbandBtn.setVisibility(View.GONE);
                Button closeBtn = (Button) dialog.findViewById(R.id.closeBtn);
                TextView messageTV = (TextView) dialog.findViewById(R.id.message);
                final ImageView personIV = (ImageView) dialog.findViewById(R.id.person);

                final RelativeLayout.LayoutParams layout = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                layout.addRule(RelativeLayout.ALIGN_RIGHT, R.id.container);
                layout.addRule(RelativeLayout.ALIGN_TOP, R.id.container);
                layout.rightMargin = -1 *(int) getView().getContext().getResources().getDimension(R.dimen.scavenger_right_margin);

                personIV.setLayoutParams(layout);
                messageTV.setText(msg);

                closeBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                                dialog.dismiss();
                                Flow.get(getView().getContext()).goBack();
                        }
                });

                dialog.show();
        }

        private Bitmap drawRectToAvatar(){
                BitmapFactory.Options myOptions = new BitmapFactory.Options();
                myOptions.inDither = true;
                myOptions.inScaled = false;
                myOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;// important
                myOptions.inPurgeable = true;

                Bitmap bitmap = BitmapFactory.decodeResource(getView().getContext().getResources(), R.drawable.next,myOptions);
                Paint paint = new Paint();
                paint.setAntiAlias(true);
                paint.setColor(getView().getContext().getResources().getColor(R.color.dark_grey));

                Bitmap workingBitmap = Bitmap.createBitmap(bitmap);
                Bitmap mutableBitmap = workingBitmap.copy(Bitmap.Config.ARGB_8888, true);

                float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                        getView().getContext().getResources().getDimension(R.dimen.item_height_large),
                        getView().getContext().getResources().getDisplayMetrics());
                Canvas canvas = new Canvas(mutableBitmap);
                canvas.drawRect(0, 0, px, px, paint);
                return mutableBitmap;
        }









        /**
         * This function MUST be first call when comes to this page, if response return empty details means
         * the user have not form or is not in a group in this hunt
         */
        public void getHuntMember(){
                final Handler mainHandler = new Handler(getView().getContext().getMainLooper());
                restClient.getScavengerApi().getHuntMembers(Long.toString(me.getUID()), Long.toString(scavengerHunt.getHuntId()))
                        .subscribeOn(Schedulers.io())
                        .subscribe(new Action1<ScavengerGroupResponse>() {
                                @Override
                                public void call(final ScavengerGroupResponse response) {
                                        UIHelper.getInstance().dismissProgressDialog();

                                        if (response.status.equals("success")) {
                                                isLeader = false;
                                                isMeScanned = false;

                                                if (!isSubmitHuntResultCalled) {
                                                        waitForHowManyPpl = 3;
                                                }
                                                isHuntStarted = false;

                                                if (response.details != null && response.details.size() > 0) {
                                                        //clean up/reset
                                                        ScavengerService.getInstance().deleteAllGroupMemberOfAHunt(scavengerHunt.getHuntId());
                                                        if (response.details.size() > 1) {
                                                                for (ScavengerGroupItem s : response.details) {
                                                                        if (s.isHuntStarted.toLowerCase().equals("true")) {
                                                                                isHuntStarted = true;
                                                                        }

                                                                        groupId = s.groupId;
                                                                        if (me.getUID() != Long.parseLong(s.userId)) {
                                                                                ScavengerService.getInstance().addGroupMember(Long.parseLong(s.huntId)
                                                                                        , Long.parseLong(s.groupId)
                                                                                        , Long.parseLong(s.userId), s.name, s.avatarId,
                                                                                        false);
                                                                        } else {
                                                                                if (s.submitted.toLowerCase().equals("true")) { //indicate I already scanned
                                                                                        isMeScanned = true;
                                                                                }
                                                                        }

                                                                        //to count how many people we are waiting for
                                                                        if (s.submitted.toLowerCase().equals("true") && !isSubmitHuntResultCalled) {
                                                                                waitForHowManyPpl--;
                                                                        }


                                                                        if (me.getUID() == Long.parseLong(s.userId) && s.isLeader.equals("true")) {
                                                                                isLeader = true;
                                                                        }

                                                                        if (me.getUID() == Long.parseLong(s.userId)) {
                                                                                if (s.submitted.toLowerCase().equals("true"))
                                                                                        hasMeSubmitted = true;
                                                                                else
                                                                                        hasMeSubmitted = false;
                                                                        }


                                                                }
                                                        } else { //me only => no member at all

                                                                isLeader = true;

                                                        }

                                                        mainHandler.post(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                        if (isLeader) {
                                                                                loadLeaderContent(isHuntStarted);
                                                                        } else {
                                                                                loadMemberContent(isHuntStarted);
                                                                        }
                                                                }
                                                        });
                                                } else {
                                                        //clean up
                                                        ScavengerService.getInstance().deleteAllGroupMemberOfAHunt(scavengerHunt.getHuntId());
                                                        mainHandler.post(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                        loadLeaderContent(isHuntStarted);
                                                                }
                                                        });
                                                }


                                        }

                                }
                        }, new Action1<Throwable>() {
                                @Override
                                public void call(Throwable throwable) {
                                        Log.e("Mobisys: ", "cannot get hunt group", throwable);
                                }
                        });
        }

        public void addMemberToGroup(final int position, final long friendId, final String name, final String avatarId, final String email, final String desig){
                final Handler mainHandler = new Handler(getView().getContext().getMainLooper());

                restClient.getScavengerApi().addMemberToGroup(Long.toString(me.getUID()), Long.toString(scavengerHunt.getHuntId()),
                        Long.toString(friendId))
                        .subscribeOn(Schedulers.io())
                        .subscribe(new Action1<ScavengerGroupResponse>() {
                                @Override
                                public void call(final ScavengerGroupResponse response) {
                                        UIHelper.getInstance().dismissProgressDialog();
                                        if(response.status.equals("success")){
                                                if (response.details != null && response.details.size() > 0){
                                                        for(ScavengerGroupItem s: response.details){
                                                                if(me.getUID() != Long.parseLong(s.userId)) {
                                                                        groupId = s.groupId;
                                                                        //getHuntMember will clean up and re-add the members
                                                                        //later after calling add member to group need to update this group id
//                                                                        ScavengerService.getInstance().addGroupMember(scavengerHunt.getHuntId(), Long.parseLong(s.groupId),
//                                                                                Long.parseLong(response.details.get(0).userId),
//                                                                                name, avatar, false);


                                                                }
                                                        }

                                                        mainHandler.post(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                        if(getView() != null){
                                                                                getHuntMember();


                                                                                View view = getViewByPosition(position, getView().teamMemberGV);
                                                                                final ImageView avatar = (ImageView) view.findViewById(R.id.avatar_image);
                                                                                final TextView nameShortFormTV = (TextView) view.findViewById(R.id.name_short_form);
                                                                                TextView nameTV = (TextView) view.findViewById(R.id.name);
                                                                                Button removeBtn = (Button) view.findViewById(R.id.remove);
                                                                                Button addBtn = (Button) view.findViewById(R.id.add);



                                                                                String nameShortFormTmp = "";
                                                                                String[] tmp = name.split(" ");
                                                                                for (int i = 0; i < tmp.length; i++) {
                                                                                        if (i == 2) {//only capture the first char of the first 2 word/name
                                                                                                break;
                                                                                        }
                                                                                        nameShortFormTmp += tmp[i].charAt(0);

                                                                                }


                                                                                final String nameShortForm = nameShortFormTmp.toUpperCase();

                                                                                if (avatarId != null && !avatarId.isEmpty()) {
                                                                                        Picasso.with(getView().getContext())
                                                                                                .load(Util.getPhotoUrlFromId(avatarId, 96))
                                                                                                .memoryPolicy(MemoryPolicy.NO_CACHE)
                                                                                                .networkPolicy(NetworkPolicy.NO_CACHE)
                                                                                                .into(avatar, new com.squareup.picasso.Callback() {
                                                                                                        @Override
                                                                                                        public void onSuccess() {

                                                                                                        }

                                                                                                        @Override
                                                                                                        public void onError() {
                                                                                                                nameShortFormTV.setVisibility(View.VISIBLE);
                                                                                                                if (getView() != null) {
                                                                                                                        avatar.setImageBitmap(drawRectToAvatar());
                                                                                                                } else {
                                                                                                                        avatar.setImageResource(R.drawable.icon_no_profile);
                                                                                                                }
                                                                                                                nameShortFormTV.setText(nameShortForm);
                                                                                                        }
                                                                                                });
                                                                                } else {
                                                                                        nameShortFormTV.setVisibility(View.VISIBLE);
                                                                                        if(getView() != null) {
                                                                                                avatar.setImageBitmap(drawRectToAvatar());
                                                                                        }
                                                                                        else{
                                                                                                avatar.setImageResource(R.drawable.icon_no_profile);
                                                                                        }
                                                                                        nameShortFormTV.setText(nameShortForm);
                                                                                }


                                                                                nameTV.setText(name);
                                                                                removeBtn.setVisibility(View.VISIBLE);
                                                                                addBtn.setVisibility(View.GONE);

                                                                                User user = new User();
                                                                                user.setName(name);
                                                                                user.setAvatar(avatarId);
                                                                                user.setUID(Long.parseLong(response.details.get(0).userId));
                                                                                user.setEmail(email);
                                                                                user.setDesignation(desig);

                                                                                memberGridAdapter.addMember(new TeamMember(user, false), position);

                                                                                if (memberGridAdapter.getNumberOfMember() == 3) {
                                                                                        getView().startBtn.setBackgroundResource(R.drawable.custom_button);
                                                                                        getView().startBtn.setEnabled(true);
                                                                                }

                                                                                UIHelper.getInstance().dismissProgressDialog();
                                                                        }
                                                                }
                                                        });
                                                }
                                        }
                                        else{
                                                UIHelper.getInstance().dismissProgressDialog();
                                                if(getView() != null){
                                                        showDialog2("This member cannot be adde to your group");
                                                }

                                        }



                                }
                        }, new Action1<Throwable>() {
                                @Override
                                public void call(Throwable throwable) {
                                        mainHandler.post(new Runnable() {
                                                                 @Override
                                                                 public void run() {
                                                                         UIHelper.getInstance().dismissProgressDialog();
                                                                         if(getView() != null){
                                                                                 showDialog2("This member cannot be added to your group");
                                                                         }
                                                                 }
                                                         });

                                        Log.e("Mobisys: ", "cannot add member", throwable);
                                }
                        });

                UIHelper.getInstance().dismissProgressDialog();
        }

        /**
         * To remove a member from the group. Note that the group Id still remain unless we disband it using delete_group
         */
        public void removeMemberfromGroup(final long friendUserId){
//                final ScavengerGroupDetailEntity member = ScavengerService.getInstance().getMember(friendId, scavengerHunt.getHuntId());

                restClient.getScavengerApi().removeMemberFromGroup(Long.toString(me.getUID()), Long.toString(scavengerHunt.getHuntId()),
                        Long.toString(friendUserId), groupId)
                        .subscribeOn(Schedulers.io())
                        .subscribe(new Action1<ScavengerGroupResponse>() {
                                @Override
                                public void call(final ScavengerGroupResponse response) {
                                        UIHelper.getInstance().dismissProgressDialog();
                                        if (response.status.equals("success")){
                                                ScavengerService.getInstance().deleteGroupMemberOfAHunt(scavengerHunt.getHuntId(), friendUserId);

                                        }

                                }
                        }, new Action1<Throwable>() {
                                @Override
                                public void call(Throwable throwable) {
                                        Log.e("Mobisys: ", "cannot remove member", throwable);


                                }
                        });
        }

        public void disbandGroupAPI(){
                final Handler mainHandler = new Handler(getView().getContext().getMainLooper());
                restClient.getScavengerApi().deleteGroup(Long.toString(me.getUID()), Long.toString(scavengerHunt.getHuntId()), groupId)
                        .subscribeOn(Schedulers.io())
                        .subscribe(new Action1<SimpleResponse>() {
                                @Override
                                public void call(final SimpleResponse response) {
                                        UIHelper.getInstance().dismissProgressDialog();
                                        if (response.status.equals("success")){
                                                //clean up or reset
                                                ScavengerService.getInstance().deleteAllGroupMemberOfAHunt(scavengerHunt.getHuntId());
                                                ScavengerService.getInstance().updateIsStarted(scavengerHunt.getHuntId(), false);
                                                ScavengerService.getInstance().updateIsSubmitted(scavengerHunt.getHuntId(), false);
                                                ScavengerService.getInstance().updateIsComplete(scavengerHunt.getHuntId(), false);
                                                mainHandler.post(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                                if (getView() != null) {
//                                                                        List<TeamMember> teamMembers = new ArrayList<>();
//                                                                        teamMembers.add(new TeamMember(me, true));
//
//                                                                        for (int i = teamMembers.size(); i < maxGroupSize; i++) {
//                                                                                teamMembers.add(null);
//                                                                        }
//
//                                                                        memberGridAdapter.updates(teamMembers);

                                                                        //we are skipping i=0 (current user)
                                                                        for (int i = 1; i < maxGroupSize; i++) {
                                                                                View view = getViewByPosition(i, getView().teamMemberGV);
                                                                                ImageView avatar = (ImageView) view.findViewById(R.id.avatar_image);
                                                                                TextView nameTV = (TextView) view.findViewById(R.id.name);
                                                                                TextView nameShortFormTV = (TextView) view.findViewById(R.id.name_short_form);
                                                                                Button removeBtn = (Button) view.findViewById(R.id.remove);
                                                                                Button addBtn = (Button) view.findViewById(R.id.add);
                                                                                avatar.setImageResource(R.drawable.icon_no_profile);
                                                                                nameTV.setText("");
                                                                                nameShortFormTV.setVisibility(View.GONE);
                                                                                removeBtn.setVisibility(View.GONE);
                                                                                addBtn.setVisibility(View.VISIBLE);
                                                                                addBtn.setEnabled(true);
                                                                        }

                                                                        getView().startBtn.setBackgroundResource(R.drawable.custom_button_grey);
                                                                        getView().startBtn.setEnabled(false);
                                                                        getView().afterStartingLL.setVisibility(View.GONE);
                                                                        getView().beforeStartingLL.setVisibility(View.VISIBLE);


//                                                                        //deduct point if the user had previous submitted the QR code
//                                                                        if (hasMeSubmitted) {
////                                                                                MasterPointService.getInstance().deductPoint(MasterPointService.getInstance().SCAVENGER_HUNT);
//                                                                        }
                                                                }
                                                        }
                                                });
                                        }

                                }
                        }, new Action1<Throwable>() {
                                @Override
                                public void call(Throwable throwable) {
                                        Log.e("Mobisys: ", "cannot disband member", throwable);
                                }
                        });
        }


        public void submitHuntResult(){
                isSubmitHuntResultCalled = true;
                final Handler mainHandler = new Handler(getView().getContext().getMainLooper());
                restClient.getScavengerApi().submitHunt(Long.toString(me.getUID()), Long.toString(scavengerHunt.getHuntId()),
                        groupId, scavengerHunt.getType())
                        .subscribeOn(Schedulers.io())
                        .subscribe(new Action1<ScavengerGroupResponse>() {
                                @Override
                                public void call(final ScavengerGroupResponse response) {
                                        UIHelper.getInstance().dismissProgressDialog();
                                        if (response.status.equals("success")){
                                                mainHandler.post(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                                showDialog2("Congratulations, you have just found the answer!");
                                                                if(getView() != null){
                                                                        if(scavengerHunt.getType().equals("single")) {
                                                                                ScavengerService.getInstance().updateIsComplete(scavengerHunt.getHuntId(), true);
//                                                                                MasterPointService.getInstance().addPoint(MasterPointService.getInstance().SCAVENGER_HUNT, getView().containerRL);
                                                                                Flow.get(getView().getContext()).goBack();
                                                                        }
                                                                        else{ //group type
//                                                                                MasterPointService.getInstance().addPoint(MasterPointService.getInstance().SCAVENGER_HUNT, getView().containerRL);
                                                                                waitForHowManyPpl = 3;
                                                                                for(ScavengerGroupItem i: response.details){
                                                                                        boolean isSubmitted = false;

                                                                                        if(i.submitted.toLowerCase().equals("true")){
                                                                                                waitForHowManyPpl--;
                                                                                        }

                                                                                        if(Long.parseLong(i.userId) != me.getUID()){
                                                                                                ScavengerService.getInstance().updateGroupMember(scavengerHunt.getHuntId(), Long.parseLong(i.userId),
                                                                                                        Long.parseLong(i.groupId), isSubmitted);
                                                                                        }

                                                                                        if(Long.parseLong(i.userId) == me.getUID()){
                                                                                                if(i.submitted.toLowerCase().equals("true")) {
                                                                                                        hasMeSubmitted = true;
                                                                                                        ScavengerService.getInstance().updateIsSubmitted(scavengerHunt.getHuntId(), true);
                                                                                                }
                                                                                                else {
                                                                                                        hasMeSubmitted = false;
                                                                                                        ScavengerService.getInstance().updateIsSubmitted(scavengerHunt.getHuntId(), false);
                                                                                                }
                                                                                        }

                                                                                }

                                                                                if(waitForHowManyPpl > 0){ //still waiting for someone to scan
                                                                                        getView().scanBtn.setEnabled(false);
                                                                                        getView().scanBtn.setBackgroundResource(R.drawable.custom_button_grey);
                                                                                        getView().scanBtn.setText(waitForHowManyPpl + " member left");
                                                                                }
                                                                                else{//everyone submitted (completed)
                                                                                        ScavengerService.getInstance().updateIsComplete(scavengerHunt.getHuntId(), true);
                                                                                        Flow.get(getView().getContext()).goBack();
                                                                                }
                                                                        }


                                                                }
                                                        }
                                                });
                                        }
                                        else{
                                                mainHandler.post(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                                if ((response.details.size() > 0)) {
                                                                        showDialog2(response.details.get(0).error);
                                                                }
                                                        }
                                                });
                                        }

                                }
                        }, new Action1<Throwable>() {
                                @Override
                                public void call(Throwable throwable) {
                                        Log.e("Mobisys: ", "cannot submit hunt result", throwable);
                                }
                        });
        }


        /**
         * This function send to the API to tell the server that leader had started the hunt
         */
        public void storeStartHuntAPI(){

                final Handler mainHandler = new Handler(getView().getContext().getMainLooper());
                restClient.getScavengerApi().storeStartHunt(Long.toString(me.getUID()), Long.toString(scavengerHunt.getHuntId()), groupId)
                        .subscribeOn(Schedulers.io())
                        .subscribe(new Action1<SimpleResponse>() {
                                @Override
                                public void call(final SimpleResponse response) {
                                        UIHelper.getInstance().dismissProgressDialog();
                                        if (response.status.equals("success")){
                                                //clean up or reset
                                                ScavengerService.getInstance().deleteAllGroupMemberOfAHunt(scavengerHunt.getHuntId());

                                                mainHandler.post(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                                if (getView() != null) {

                                                                        getView().beforeStartingLL.setVisibility(View.GONE);
                                                                        getView().afterStartingLL.setVisibility(View.VISIBLE);
                                                                        ScavengerService.getInstance().updateIsStarted(scavengerHunt.getHuntId(), true);

                                                                        //disable remove btn
                                                                        int size = memberGridAdapter.getCount();
                                                                        for (int i = 0; i < size; i++) {
                                                                                View view = getViewByPosition(i, getView().teamMemberGV);
                                                                                Button removeBtn = (Button) view.findViewById(R.id.remove);
                                                                                Button addBtn = (Button) view.findViewById(R.id.add);

                                                                                addBtn.setBackgroundResource(R.drawable.custom_button);
                                                                                addBtn.setEnabled(false);

                                                                                removeBtn.setBackgroundResource(R.drawable.custom_button_grey);
                                                                                removeBtn.setEnabled(false);

                                                                        }
                                                                }
                                                        }
                                                });
                                        }

                                }
                        }, new Action1<Throwable>() {
                                @Override
                                public void call(Throwable throwable) {
                                        Log.e("Mobisys: ", "cannot store start hunt", throwable);
                                }
                        });
        }


        @Subscribe
        public void updateHuntDetail(ScavengerUpdateDetailEvent event){
                System.out.println("Hunt Event>>>> " + event.action);
                if(event.huntId == scavengerHunt.getHuntId()) {
                        if(event.action.equals("deleted hunt")){
                                ScavengerService.getInstance().updateIsComplete(scavengerHunt.getHuntId(), false);
                                ScavengerService.getInstance().updateIsSubmitted(scavengerHunt.getHuntId(), false);
                                ScavengerService.getInstance().updateIsStarted(scavengerHunt.getHuntId(), false);
                                Flow.get(getView().getContext()).goBack();
                        }
                        else if(event.action.equals("finished")){
                                ScavengerService.getInstance().updateIsComplete(scavengerHunt.getHuntId(), true);
                                Flow.get(getView().getContext()).goBack();
                        }
                        else{ //added user, removed user
                                getHuntMember();
                        }

                }
        }


        /**
         * This function is use as a timer to check if the game have ended or not
         */
        private Runnable updateRemainingTimeRunnable = new Runnable() {
                @Override
                public void run() {

                        Date date= new java.util.Date();
                        long currentTime = date.getTime();
                        //date.getTime Returns this {@code Date} as a millisecond value. The value is the number of
                        // milliseconds since Jan. 1, 1970, midnight GMT.
                        //System.currentTimeMillis(); > this method shouldn't be used for measuring timeouts or
                        // other elapsed time measurements, as changing the system time can affect the results.
                        //thus we need to use date instead
                        updateTimeRemaining(currentTime);


                }
        };

        public void updateTimeRemaining(long currentTime) {
                //else add the offset
                long timeDiff = scavengerHunt.getStartTime().getTime() - currentTime;
                long timeEndDiff =  currentTime - scavengerHunt.getEndTime().getTime() ;
//                System.out.println("Game ends in " + timeEndDiff);
                if(timeDiff <= 0 && timeEndDiff > 0){ //game ended
                        Flow.get(getView().getContext()).goBack();
                }
        }

        public void endScavengerTimer(){
                timer.cancel();
                timer.purge();
        }

        private void startUpdateTimer() {
                timer = new Timer();
                timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                                mTimerHandler.post(updateRemainingTimeRunnable);
                        }
                }, 1000, 1000);

        }


        /**
         * Bus event for internet service
         */
        @Subscribe
        public void updateNetwork(UpdateNetworkEvent event){
                if(event.isConnected){
                        if(scavengerHunt.getType().equals("group") && isFirstTimeCalling) {
                                getHuntMember();
                        }

                        if(getView().teamMemberGV.getAdapter() != null) {
                                for (int i = 0; i < maxGroupSize; i++) {
                                        View view = getViewByPosition(i, getView().teamMemberGV);
                                        Button removeBtn = (Button) view.findViewById(R.id.remove);
                                        Button addBtn = (Button) view.findViewById(R.id.add);

                                        addBtn.setBackgroundResource(R.drawable.custom_button);
                                        addBtn.setEnabled(true);

                                        if(!ScavengerService.getInstance().isHuntStarted(scavengerHunt.getHuntId())) {
                                                removeBtn.setBackgroundResource(R.drawable.custom_button);
                                                removeBtn.setEnabled(true);
                                        }
                                        else{
                                                removeBtn.setBackgroundResource(R.drawable.custom_button_grey);
                                                removeBtn.setEnabled(false);
                                        }

                                }
                        }

                        getView().disbandBtn.setBackgroundResource(R.drawable.custom_button);
                        getView().disbandBtn.setEnabled(true);

                        if(!ScavengerService.getInstance().hasUserSubmitted(scavengerHunt.getHuntId())) {
                                getView().scanBtn.setBackgroundResource(R.drawable.custom_button);
                                getView().scanBtn.setEnabled(true);
                        }
                        else{
                                getView().scanBtn.setBackgroundResource(R.drawable.custom_button_grey);
                                getView().scanBtn.setEnabled(false);
                        }

                        if(!ScavengerService.getInstance().isHuntStarted(scavengerHunt.getHuntId())) {
                                getView().startBtn.setBackgroundResource(R.drawable.custom_button_grey);
                                getView().startBtn.setEnabled(false);
                        }
                        else{
                                getView().startBtn.setBackgroundResource(R.drawable.custom_button);
                                getView().startBtn.setEnabled(true);
                        }

                }
                else{
                        if(getView().teamMemberGV.getAdapter() != null) {
                                for (int i = 0; i < maxGroupSize; i++) {
                                        View view = getViewByPosition(i, getView().teamMemberGV);
                                        Button removeBtn = (Button) view.findViewById(R.id.remove);
                                        Button addBtn = (Button) view.findViewById(R.id.add);

                                        addBtn.setBackgroundResource(R.drawable.custom_button_grey);
                                        addBtn.setEnabled(false);

                                        removeBtn.setBackgroundResource(R.drawable.custom_button_grey);
                                        removeBtn.setEnabled(false);

                                }
                        }

                        getView().scanBtn.setBackgroundResource(R.drawable.custom_button_grey);
                        getView().scanBtn.setEnabled(false);

                        getView().startBtn.setBackgroundResource(R.drawable.custom_button_grey);
                        getView().startBtn.setEnabled(false);

                        getView().disbandBtn.setBackgroundResource(R.drawable.custom_button_grey);
                        getView().disbandBtn.setEnabled(false);
                }
        }

        @Subscribe
        public void unregisterInternetReceiver(UnregisterScavengerEvent event){
                if(event.toUnregister) {
                        App.getInstance().stopNetworkMonitoringReceiver();
                }
                else{
                        App.getInstance().startNetworkMonitoringReceiver();
                }
        }

        @Subscribe
        public void badgeNotiEvent(BadgeNotiEvent event){
                MasterPointService.getInstance().getBadgesAPI();
                MasterPointService.getInstance().showToolTips(getView().containerRL, event.badgeName);
        }

}
