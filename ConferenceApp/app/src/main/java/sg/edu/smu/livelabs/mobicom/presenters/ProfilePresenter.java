package sg.edu.smu.livelabs.mobicom.presenters;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.isseiaoki.simplecropview.CropImageView;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import autodagger.AutoComponent;
import automortar.AutoScreen;
import automortar.ScreenParam;
import flow.Flow;
import me.kaede.tagview.OnTagClickListener;
import me.kaede.tagview.OnTagDeleteListener;
import me.kaede.tagview.Tag;
import me.kaede.tagview.TagView;
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
import sg.edu.smu.livelabs.mobicom.adapters.BadgesListAdapter;
import sg.edu.smu.livelabs.mobicom.adapters.InterestGridAdapter;
import sg.edu.smu.livelabs.mobicom.busEvents.BadgesEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.BadgeNotiEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.InterestEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.InterestUpdateEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.UnregisterProfileEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.UpdateNetworkEvent;
import sg.edu.smu.livelabs.mobicom.flow.Layout;
import sg.edu.smu.livelabs.mobicom.models.Interest;
import sg.edu.smu.livelabs.mobicom.models.User;
import sg.edu.smu.livelabs.mobicom.models.data.BadgeEntity;
import sg.edu.smu.livelabs.mobicom.models.data.InterestsEntity;
import sg.edu.smu.livelabs.mobicom.net.RestClient;
import sg.edu.smu.livelabs.mobicom.net.response.ProfileResponse;
import sg.edu.smu.livelabs.mobicom.presenters.screen.BeaconScreen;
import sg.edu.smu.livelabs.mobicom.presenters.screen.BingoScreen;
import sg.edu.smu.livelabs.mobicom.presenters.screen.EVAPromotionScreen;
import sg.edu.smu.livelabs.mobicom.presenters.screen.FavoriteScreen;
import sg.edu.smu.livelabs.mobicom.presenters.screen.IceBreakerScreen;
import sg.edu.smu.livelabs.mobicom.presenters.screen.ScavengerHuntScreen;
import sg.edu.smu.livelabs.mobicom.presenters.screen.StumpListScreen;
import sg.edu.smu.livelabs.mobicom.presenters.screen.SurveyScreen;
import sg.edu.smu.livelabs.mobicom.presenters.screen.VotingListScreen;
import sg.edu.smu.livelabs.mobicom.presenters.screen.VotingScreen;
import sg.edu.smu.livelabs.mobicom.services.DatabaseService;
import sg.edu.smu.livelabs.mobicom.services.GameService;
import sg.edu.smu.livelabs.mobicom.services.InterestService;
import sg.edu.smu.livelabs.mobicom.services.MasterPointService;
import sg.edu.smu.livelabs.mobicom.util.Util;
import sg.edu.smu.livelabs.mobicom.views.MyCropImageView;
import sg.edu.smu.livelabs.mobicom.views.MyDialog;
import sg.edu.smu.livelabs.mobicom.views.ProfileView;

/**
 * Created by smu on 22/2/16.
 */
@AutoScreen(
        component = @AutoComponent(dependencies = MainActivity.class, superinterfaces = AppDependencies.class),
        screenAnnotations = Layout.class
)
@DaggerScope(ProfilePresenter.class)
@Layout(R.layout.profile_view)
public class ProfilePresenter extends ViewPresenter<ProfileView> implements  View.OnClickListener, OnTagClickListener
        , OnTagDeleteListener, MyCropImageView.ReloadingImageListener {

        private final RestClient restClient;
        private Bus bus;
        private MainActivity mainActivity;
        private ActionBarOwner actionBarOwner;
        public static String NAME = "ProfilePresenter";


        private boolean isMeAdmin;
        private  int defaultAvatar;
        private InterestGridAdapter interestGridAdapter;
        private InterestGridAdapter interestGridAdapter2;
        private Boolean isCurrentUser;
        private String email;
        private List<InterestsEntity> interests;
        private String lastTime;
        private  ConnectivityManager cm;
        private User me;
        private boolean isFirstTime; //is this to indicate whether on every opening of this page, is it first time loading
        private MyDialog alertDialog;
        private TagView tagView;
        private HashMap<Long, Interest> selectedMap;
        private String avatarId;
        private int screenWidth;

        private List<BadgeEntity> badges;
        private BadgesListAdapter badgesListAdapter;

        float scale=1;
        public Context c;
        public ListView list ;
        public MaterialDialog progress;
        private Dialog dialog;
        /**
         *
         * @param restClient
         * @param bus
         * @param mainActivity
         * @param isCurrentUser: true to load the user profile, false to load attendee profile
         */
        public ProfilePresenter(RestClient restClient, Bus bus, ActionBarOwner actionBarOwner, MainActivity mainActivity, @ScreenParam Boolean isCurrentUser,
                                @ScreenParam String email) {
                this.restClient = restClient;
                this.bus = bus;
                this.mainActivity = mainActivity;
                this.defaultAvatar = R.drawable.empty_profile;
                this.isCurrentUser = isCurrentUser;
                this.interests = new ArrayList<InterestsEntity>();
                this.email = email;
                this.actionBarOwner = actionBarOwner;
                this.isFirstTime = true;
                this.selectedMap = new HashMap<>();
                this.isMeAdmin = false;

        }

        @Override
        protected void onLoad(Bundle savedInstanceState) {
                super.onLoad(savedInstanceState);
                Log.d(App.APP_TAG, " ProfilePresenter onload");

                c = getView().getContext();
//                list = getView().badgesLV;

                progress = new MaterialDialog.Builder(getView().getContext())
                        .title(getView().getContext().getString(R.string.app_name))
                        .content("Loading Profile...")
                        .progress(true, 0)
                        .cancelable(false)
                        .show();


                actionBarOwner.setConfig(new ActionBarOwner.Config(true, "Profile", null));

                WindowManager wm = (WindowManager) getView().getContext().getSystemService(Context.WINDOW_SERVICE);
                Display display = wm.getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);
                screenWidth = size.x;

                mainActivity.currentTab = MainActivity.OTHER_TAB;

                //to calculate for check box size, max 8 loops
                final int REQUIRED_SIZE=(int)(screenWidth * 0.25);
                float width_tmp= (float) screenWidth;
                scale=1f;
                while(true){
                        if(scale < 1f && width_tmp <REQUIRED_SIZE ) {
                                break;
                        }

                        if(scale > 0.2) {
                                scale -= 0.1;
                        }
                        else{
                                break;
                        }
                        width_tmp =width_tmp * scale;

                }

                tagView = getView().tagView;
                tagView.setOnTagDeleteListener(this);
                tagView.setOnTagClickListener(this);


                cm = (ConnectivityManager) getView().getContext().getSystemService(Context.CONNECTIVITY_SERVICE);

                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
                InterestsEntity interest = InterestService.getInstance().getInterest();
                lastTime = interest != null ? df.format(interest.getLastUpdated()) : "2016-01-01";


                if(isCurrentUser){
                        //setting to register to local onclick listener
                        getView().profileCIV.setOnClickListener(this);

                        me = DatabaseService.getInstance().getMe();
                        for(String s: me.getRole()){
                                if(s.trim().toLowerCase().equals("admin")){
                                        isMeAdmin = true;
                                        break;
                                }
                        }

                        avatarId = me.getAvatar();
//                        UploadFileService.getInstance().loadImage(getView().profileCIV, R.drawable.icon_no_profile, avatarId, 96);
                        getView().myCropImageView.setListener(this, mainActivity, CropImageView.CropMode.CIRCLE);

                        if (me != null) {
                                //get user details
                                if(getView() != null && me.getAvatar() != null && !me.getAvatar().isEmpty()){
                                        try {
                                                Picasso.with(getView().getContext()).load(Util.getPhotoUrlFromId(me.getAvatar(), 256))
                                                        .memoryPolicy(MemoryPolicy.NO_CACHE)
                                                        .networkPolicy(NetworkPolicy.NO_CACHE)
                                                        .into(getView().profileCIV, new com.squareup.picasso.Callback() {
                                                                @Override
                                                                public void onSuccess() {
                                                                }

                                                                @Override
                                                                public void onError() { //when failed to load, draw a rect on top of the noavatar and add the name short form
                                                                        if(getView() != null)
                                                                                GameService.getInstance().drawNameShort(me.getName(), getView().profileCIV, getView().nameShortFormTV);
                                                                }
                                                        });
                                        }
                                        catch(Throwable e){
                                                Log.d("AAA", "ProfilePresenter:"+e.toString());
                                        }
                                }
                                else{
                                        GameService.getInstance().drawNameShort(me.getName(), getView().profileCIV, getView().nameShortFormTV);
                                }

                                //combine suffix and name
                                getView().nameTV.setText(me.getName() );
                                getView().institutionTV.setText(me.getSchool());
                                getView().designationTV.setText(me.getDesignation());

                        }

                        interests = InterestService.getInstance().getAllInterest();

                        //getInterest from db server and check whether got latest
                        if(cm.getActiveNetworkInfo() != null){
                                updateInterestsList(true);
                        }

                        //continue to load current user profile, while the update of interests list gets updated
                        loadUserProfile();
                }
                else{ //for attendee profile

                        if(cm.getActiveNetworkInfo() == null){
                                getView().scrollMainContainer.setVisibility(View.GONE);
                                getView().mainContainer.setVisibility(View.GONE);
                                getView().noNetworkContainer.setVisibility(View.VISIBLE);
                                return;
                        }
//                        getView().avatarCameraIV.setVisibility(View.GONE);

                        updateInterestsList(true);
                }

                MasterPointService.getInstance().getBadgesAPI();
                App.getInstance().startNetworkMonitoringReceiver();

        }

        @Override
        public void onClick(View v) {
                int id = v.getId();
                switch (id) {
                        case R.id.avatar_image:
                                if(cm.getActiveNetworkInfo() != null) {
                                        getView().myCropImageView.uploadImage();

                                }
                                else{
                                        UIHelper.getInstance().showAlert(getView().getContext(), "Opps, did you turn on your Internet Connection?");
                                }
                                break;
                }
        }




        //To download the list of interests from database server
        public void updateInterestsList(Boolean isFirstTime){
                if(!isFirstTime ){
                        if(cm.getActiveNetworkInfo() == null){
//                                getView().scrollMainContainer.setVisibility(View.GONE);
                                getView().mainContainer.setVisibility(View.GONE);
                                getView().noNetworkContainer.setVisibility(View.VISIBLE);
                                return;
                        }
                        else{
//                                getView().scrollMainContainer.setVisibility(View.VISIBLE);
                                getView().mainContainer.setVisibility(View.VISIBLE);
                                getView().noNetworkContainer.setVisibility(View.GONE);
                        }
                }

                InterestService.getInstance().loadInterestAPI();

        }

        public void loadUserProfile(){
                if(interests.size() > 0){
                        tagView.removeAllTags();
                        if(me.getInterests() != null) { //empty (most of the time is first time user login)
                                for (String i : me.getInterests()) {
                                        if (!i.equals(" "))
                                                tagView.addTag(createTag(i));
                                }
                        }
                }

                progress.dismiss();

                //add a dummy tag called "+ add more"" to allow user to open the dialog box
                tagView.addTag(createTag("+ add interest"));

                badges = MasterPointService.getInstance().getBadges();

                if(getView() != null && badges != null && badges.size() > 0){
                        final Handler mainHandler = new Handler(getView().getContext().getMainLooper());
                        Thread thread = new Thread() {
                                @Override
                                public void run() {

                                        badgesListAdapter = new BadgesListAdapter(getView().getContext(), bus, badges);
                                        mainHandler.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                        try {
                                                                getView().badgesLV.setAdapter(badgesListAdapter);
                                                        }
                                                        catch(Throwable e){
                                                                Log.d("AAA", "ProfilePresenter:loadUserProfile" +e.toString());
                                                        }

                                                        Handler handler = new Handler();
                                                        handler.postDelayed(new Runnable() {
                                                                @Override
                                                                public void run() {

                                                                        progress.dismiss();
                                                                }
                                                        }, 1000);
                                                }
                                        });

                                }
                        };
//
                        thread.start();


                        getView().badgesLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                        showDialog(position);
                                }
                        });
                }

                getView().scrollMainContainer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                                // Ready, move up
                                if (getView() != null) {
                                        UIHelper.getInstance().dismissProgressDialog();
                                        getView().scrollMainContainer.fullScroll(ScrollView.FOCUS_UP);
                                        getView().scrollMainContainer.scrollTo(0, 0);
                                }
                        }
                });

        }


        @Override
        protected void onEnterScope(MortarScope scope) {
                super.onEnterScope(scope);
                bus.register(this);
                App.getInstance().currentPresenter = NAME;
                mainActivity.setVisibleBottombar(View.VISIBLE);
        }

        @Override
        protected void onExitScope() {
                super.onExitScope();
                if(dialog != null && dialog.isShowing()){
                        dialog.dismiss();
                }
                bus.unregister(this);
                if (NAME.equals(App.getInstance().currentPresenter)){
                        App.getInstance().currentPresenter = "";
                }
        }

        @Subscribe
        public void unregisterProfileReceiver(UnregisterProfileEvent event){
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
                MasterPointService.getInstance().showToolTips(getView().scrollMainContainer, event.badgeName);
        }

        /**
         * To reload the badge when user there is any achievement update
         */
        @Subscribe
        public void badgeUpdateEvent(BadgesEvent event){
                badges = MasterPointService.getInstance().getBadges();
                if(badgesListAdapter != null) {
                        badgesListAdapter.update(badges);
                        if(getView() !=null && getView().badgesLV.getAdapter() == null){
                                getView().badgesLV.setAdapter(badgesListAdapter);
                        }
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                        progress.dismiss();
                                }
                        }, 1000);
                }
                else{
                        final Handler mainHandler = new Handler(getView().getContext().getMainLooper());
                        Thread thread = new Thread() {
                                @Override
                                public void run() {
                                        badgesListAdapter = new BadgesListAdapter(getView().getContext(), bus, badges);
                                        mainHandler.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                        getView().badgesLV.setAdapter(badgesListAdapter);

                                                        Handler handler = new Handler();
                                                        handler.postDelayed(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                        progress.dismiss();
                                                                }
                                                        }, 1000);
                                                }
                                        });

                                }
                        };
                }
        }

        @Subscribe
        public void updateNetwork(UpdateNetworkEvent event){
                if(event.isConnected){
                        if(isCurrentUser){
//                                getView().interestGV.setVisibility(View.VISIBLE);
//                                getView().noInterestTV.setVisibility(View.GONE);
                        }
                        else{
                                getView().scrollMainContainer.setVisibility(View.VISIBLE);
                                getView().mainContainer.setVisibility(View.VISIBLE);
                                getView().noNetworkContainer.setVisibility(View.GONE);
//                                getView().avatarCameraIV.setVisibility(View.GONE);
                        }

                        updateInterestsList(true);
                }
        }

        @Subscribe
        public void interestEvent(InterestEvent event){
                loadUserProfile();
                interests = InterestService.getInstance().getAllInterest();
        }


        @Subscribe
        public void interesUpdatetEvent(InterestUpdateEvent event){
                if (isFirstTime == true) {
                        isFirstTime = false;
                }
        }

        @Override
        public void onTagClick(Tag tag, int position) {
                if(tag.text.equals("+ add interest")){
                        showInterestDialog();
                }
        }

        @Override
        public void onTagDeleted(Tag tag, int position) {
                selectedMap.remove(tag.id);
        }

        private Tag createTag(String text){
                if(getView()!= null){
                        Tag tag = new Tag(text);

                        tag.tagTextColor = getView().getResources().getColor(R.color.white);

                        if(tag.text.equals("+ add interest")){
                                tag.tagTextColor = getView().getResources().getColor(R.color.colorPrimaryLight2);
                        }
                        tag.layoutColor =  getView().getResources().getColor(R.color.colorPrimary);
                        tag.layoutColorPress = getView().getResources().getColor(R.color.white);
                        tag.radius = 5f;
                        tag.tagTextSize = 14f;
                        tag.layoutBorderSize = 0f;
//                tag.isDeletable = true;
                        return tag;
                }
                return new Tag("");
        }

        public void showInterestDialog(){
                if(interests == null || interests.size() <= 0){
                        return;
                }

                dialog = new Dialog(getView().getContext());
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.dialog_box_interest);


                ListView interestLV = (ListView) dialog.findViewById(R.id.interest_list);
                Button closeBtn = (Button) dialog.findViewById(R.id.closeBtn);
                Button updateBtn = (Button) dialog.findViewById(R.id.updateBtn);

                interestGridAdapter = new InterestGridAdapter(getView().getContext(), interests, false, me.getInterests(), scale);
                interestLV.setAdapter(interestGridAdapter);
                closeBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                                dialog.dismiss();
                        }
                });

                updateBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                                List<String> selectedInterest = interestGridAdapter.getSelectedInterestInListForm();
                                if(selectedInterest.size() > 0) {
                                        updateInterest(interestGridAdapter.getSelectedInterest());
                                        tagView.removeAllTags();
                                        for (String i : selectedInterest) {
                                                if (!i.equals(" "))
                                                        tagView.addTag(createTag(i));
                                        }
                                        tagView.addTag(createTag("+ add interest"));
                                        dialog.dismiss();
                                }
                                else{
                                        UIHelper.getInstance().showAlert(getView().getContext(), "Please choose at least 1 interest");
                                }
                        }
                });

                dialog.show();
        }

        private void updateInterest(final String userInterest){
                if(cm.getActiveNetworkInfo() != null){

                        me.setInterestsStr(userInterest);
                        DatabaseService.getInstance().setMe(me);

                        UIHelper.getInstance().showProgressDialog(getView().getContext(), "Updating Interest...", false);
                        InterestService.getInstance().updateInterestAPI(me.getUID(), userInterest, me.getUserHandle(), me.getSessionToken());
                }
                else{

                        UIHelper.getInstance().showAlert(getView().getContext(), getView().getContext().getString(R.string.no_internet_connection));
                }
        }



        @Override
        public void setNewImage(String avatarId) {
                this.avatarId = avatarId;
                getView().scrollMainContainer.setVisibility(View.VISIBLE);
                getView().scrollMainContainer.smoothScrollTo(0, 0);
                me.setAvatar(avatarId);
                DatabaseService.getInstance().setMe(me);

//                UploadFileService.getInstance().loadImage(getView().profileCIV,
//                        R.drawable.icon_no_profile, avatarId, 96);

                try {
                        Picasso.with(getView().getContext()).load(Util.getPhotoUrlFromId(avatarId, 96))
                                .memoryPolicy(MemoryPolicy.NO_CACHE)
                                .networkPolicy(NetworkPolicy.NO_CACHE)
                                .placeholder(R.drawable.icon_no_profile).into(getView().profileCIV, new com.squareup.picasso.Callback() {
                                @Override
                                public void onSuccess() {
                                        getView().nameShortFormTV.setVisibility(View.GONE);
                                }

                                @Override
                                public void onError() { //when failed to load, draw a rect on top of the noavatar and add the name short form
                                        GameService.getInstance().drawNameShort(me.getName(), getView().profileCIV, getView().nameShortFormTV);
                                }
                        });
                }
                catch (OutOfMemoryError e){

                }

                getView().nameShortFormTV.setVisibility(View.GONE);

                restClient.getProfileApi().updateAvatar(Long.toString(me.getUID()), avatarId, me.getUserHandle(), me.getSessionToken())
                        .subscribeOn(Schedulers.io())
                        .subscribe(new Action1<ProfileResponse>() {
                                @Override
                                public void call(ProfileResponse response) {
                                        MasterPointService.getInstance().getBadgesAPI();
                                }
                        }, new Action1<Throwable>() {
                                @Override
                                public void call(Throwable throwable) {
                                        Log.e("Mobisys: ", "cannot update avatar", throwable);
                                }
                        });
        }

        @Override
        public void hideMainLayout() {
                getView().scrollMainContainer.setVisibility(View.GONE);
        }


        public void showDialog(int position){
                final BadgeEntity b = badges.get(position);
                int color = getView().getContext().getResources().getColor(R.color.blue);
                if(badgesListAdapter != null) {
                        color = badgesListAdapter.getRandomColor(position);
                }

                dialog = new Dialog(getView().getContext());
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.dialog_box_badge_details);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

                LinearLayout containerLL = (LinearLayout)dialog.findViewById(R.id.container);
                ImageView starOne = (ImageView)dialog.findViewById(R.id.star_one);
                ImageView starTwo = (ImageView)dialog.findViewById(R.id.star_two);
                ImageView starThree = (ImageView)dialog.findViewById(R.id.star_three);
                ImageView trophy = (ImageView)dialog.findViewById(R.id.trophy);
                final Button playBtn = (Button) dialog.findViewById(R.id.playBtn);
                Button closeBtn = (Button) dialog.findViewById(R.id.closeBtn);
                TextView titleTV = (TextView) dialog.findViewById(R.id.title);
                TextView descriptionTV = (TextView) dialog.findViewById(R.id.description);

                containerLL.setLayoutParams(new LinearLayout.LayoutParams((int)(screenWidth * 0.8), ViewGroup.LayoutParams.WRAP_CONTENT));

                Drawable mDrawable2 = getView().getContext().getResources().getDrawable(R.drawable.star_filled);
                Drawable mDrawable = getView().getContext().getResources().getDrawable(R.drawable.star);
                Drawable mDrawableTrophy = getView().getContext().getResources().getDrawable(R.drawable.trophy_white);
                Drawable mDrawableTrophy2 = getView().getContext().getResources().getDrawable(R.drawable.trophy_white_fill);

                titleTV.setText(b.getBadges());
                descriptionTV.setText(b.getDescription() + "\n");

                starOne.setColorFilter(color);
                starTwo.setColorFilter(color);
                starThree.setColorFilter(color);
                trophy.setColorFilter(color);

                if(b.getMax() == 1 && b.getBadgesType() == 2){ //for special badge
                        starOne.setVisibility(View.GONE);
                        starTwo.setVisibility(View.GONE);
                        starThree.setVisibility(View.GONE);
                        trophy.setVisibility(View.VISIBLE);
                        if(b.getCountAchieved() == 1){
                                trophy.setImageDrawable(mDrawableTrophy2);
                        }
                        else{
                                trophy.setImageDrawable(mDrawableTrophy);
                        }

                }
                else if(b.getMax() == 1 && b.getBadgesType() == 1){ //for normal badge with only 1 star (profile badge)
                        starOne.setVisibility(View.GONE);
                        starTwo.setVisibility(View.GONE);
                        starThree.setVisibility(View.VISIBLE);
                        trophy.setVisibility(View.GONE);
                }

                //this area highlight the star (filled star) depending on the count
                if(b.getCountAchieved() == 0){
                        starOne.setImageDrawable(mDrawable);
                        starTwo.setImageDrawable(mDrawable);
                        starThree.setImageDrawable(mDrawable);
                }
                else if(b.getCountAchieved() == 1){ //this part is tricky, we need to show 2 type, 1 star for profile and normal badge 3-4 stars
                        if(b.getMax() == 1 && b.getBadgesType() == 1){
                                starThree.setImageDrawable(mDrawable2);
                        }
                        else {
                                starOne.setImageDrawable(mDrawable2);
                                starTwo.setImageDrawable(mDrawable);
                                starThree.setImageDrawable(mDrawable);
                        }
                }
                else if(b.getCountAchieved() == 2) {
                        starOne.setImageDrawable(mDrawable2);
                        starTwo.setImageDrawable(mDrawable2);
                        starThree.setImageDrawable(mDrawable);
                }
                else if(b.getCountAchieved() == 3){
                        starOne.setImageDrawable(mDrawable2);
                        starTwo.setImageDrawable(mDrawable2);
                        starThree.setImageDrawable(mDrawable2);
                }
                else if(b.getCountAchieved() == 4){
                        starOne.setVisibility(View.GONE);
                        starTwo.setVisibility(View.GONE);
                        starThree.setVisibility(View.GONE);
                        trophy.setVisibility(View.VISIBLE);
                        trophy.setImageDrawable(mDrawableTrophy2);
                }

                closeBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                                dialog.dismiss();
                        }
                });

                if(b.getPlayNow()!= null && b.getPlayNow().toLowerCase().equals("inactive")) {
                        playBtn.setVisibility(View.GONE);
                }
                else{
                        if (b.getKeyword().isEmpty()) {
                                playBtn.setVisibility(View.GONE);
                        }
                }
//                playBtn.setVisibility(View.GONE);

//                final String gameName = MasterPointService.getInstance().getGameNameById(b.getGameId());

                playBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                                if (b.getKeyword().equals("ice breaker")) {
                                        Flow.get(getView()).set(new IceBreakerScreen());
                                }
                                else if (b.getKeyword().equals("scavenger hunt")) {
                                        Flow.get(getView()).set(new ScavengerHuntScreen());
                                }
                                else if (b.getKeyword().equals("polling")) {
                                        if(isMeAdmin)
                                                Flow.get(getView()).set(new VotingScreen(0, "Polling"));
                                        else
                                                Flow.get(getView()).set(new VotingListScreen());
                                }
                                else if (b.getKeyword().equals("survey")) {
                                        Flow.get(getView()).set(new SurveyScreen());
                                }
                                else if (b.getKeyword().equals("demo")) {
                                        Flow.get(getView()).set(new BeaconScreen());
                                }
                                else if (b.getKeyword().equals("coolfie")) {
                                        Flow.get(getView()).set(new EVAPromotionScreen());
                                }
                                else if (b.getKeyword().equals("favourite")) {
                                        Flow.get(getView()).set(new FavoriteScreen());
                                }
                                else if (b.getKeyword().equals("stump")) {
                                        Flow.get(getView()).set(new StumpListScreen());
                                }else if (b.getKeyword().equals("photo_bingo")) {
                                        Flow.get(getView()).set(new BingoScreen());
                                }

                                dialog.dismiss();
                        }
                });

                dialog.show();
        }
}
