package sg.edu.smu.livelabs.mobicom.presenters;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import autodagger.AutoComponent;
import automortar.AutoScreen;
import de.hdodenhof.circleimageview.CircleImageView;
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
import sg.edu.smu.livelabs.mobicom.adapters.BadgesListAdapter;
import sg.edu.smu.livelabs.mobicom.adapters.LeaderboardAdapter;
import sg.edu.smu.livelabs.mobicom.busEvents.RankingEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.UnregisterLeaderboardEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.UpdateNetworkEvent;
import sg.edu.smu.livelabs.mobicom.flow.Layout;
import sg.edu.smu.livelabs.mobicom.models.User;
import sg.edu.smu.livelabs.mobicom.models.data.AttendeeEntity;
import sg.edu.smu.livelabs.mobicom.models.data.BadgeEntity;
import sg.edu.smu.livelabs.mobicom.net.RestClient;
import sg.edu.smu.livelabs.mobicom.net.item.BadgeItem;
import sg.edu.smu.livelabs.mobicom.net.item.RankingItem;
import sg.edu.smu.livelabs.mobicom.net.response.BadgeResponse;
import sg.edu.smu.livelabs.mobicom.services.AttendeesService;
import sg.edu.smu.livelabs.mobicom.services.DatabaseService;
import sg.edu.smu.livelabs.mobicom.services.GameService;
import sg.edu.smu.livelabs.mobicom.services.TrackingService;
import sg.edu.smu.livelabs.mobicom.util.Util;
import sg.edu.smu.livelabs.mobicom.views.LeaderboardView;

/**
 * Created by smu on 22/2/16.
 */
@AutoScreen(
        component = @AutoComponent(dependencies = MainActivity.class, superinterfaces = AppDependencies.class),
        screenAnnotations = Layout.class
)
@DaggerScope(LeaderboardPresenter.class)
@Layout(R.layout.leaderboard_web_view)
public class LeaderboardPresenter extends ViewPresenter<LeaderboardView>{

        private ActionBarOwner actionBarOwner;
        private final RestClient restClient;
        private Bus bus;
        public static String NAME = "LeaderboardPresenter";
        private MainActivity mainActivity;
        private String url = RestClient.LEADERBOARD_BASE_URL;

        private long formId;
        private User me;
        private  ConnectivityManager cm;
        private boolean isFirsTime = true;

        private LeaderboardAdapter adapter;
        private int screenHeight;

        public LeaderboardPresenter(RestClient restClient, Bus bus, ActionBarOwner actionBarOwner, MainActivity mainActivity) {
                this.restClient = restClient;
                this.bus = bus;
                App.getInstance().currentPresenter = "LeaderboardPresenter";
                this.actionBarOwner = actionBarOwner;
                this.mainActivity = mainActivity;
        }

        @Override
        protected void onLoad(Bundle savedInstanceState) {
                super.onLoad(savedInstanceState);
                Log.d(App.APP_TAG, " LeaderboardPresenter onload");

                actionBarOwner.setConfig(new ActionBarOwner.Config(true, "Leaderboard", null));

                mainActivity.currentTab = MainActivity.OTHER_TAB;

                me = DatabaseService.getInstance().getMe();
                url += me.getUID();

                cm = (ConnectivityManager) getView().getContext().getSystemService(Context.CONNECTIVITY_SERVICE);

                WindowManager wm = (WindowManager) getView().getContext().getSystemService(Context.WINDOW_SERVICE);
                Display display = wm.getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);
                screenHeight = size.y;

                if(cm.getActiveNetworkInfo() != null) {
//                        loadPage(url);
                        GameService.getInstance().loadRanking(Long.toString(me.getUID()));
                        isFirsTime = false;
                }
                else{
                        getView().leaderboardLV.setVisibility(View.GONE);
//                        getView().progressBar.setVisibility(View.GONE);
                        getView().messageTV.setVisibility(View.VISIBLE);
                        getView().messageTV.setText(getView().getContext().getResources().getText(R.string.no_leaderboard));
                }

                App.getInstance().startNetworkMonitoringReceiver();
        }

//        private void loadPage(String surveyURL){
//                getView().leaderboardWV.setVisibility(View.VISIBLE);
//                getView().progressBar.setVisibility(View.GONE);
//                getView().messageTV.setVisibility(View.GONE);
//                getView().leaderboardWV.getSettings().setJavaScriptEnabled(true);
//                getView().leaderboardWV.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
//                getView().leaderboardWV.setWebViewClient(new WebViewClient());
//                getView().leaderboardWV.setWebChromeClient(new WebChromeClient());
//
////                String url = GOOGLE_FORM + surveyURL;
//                getView().leaderboardWV.loadUrl(surveyURL);
//                getView().leaderboardWV.setWebViewClient(new WebViewClient() {
//
//                        @Override
//                        public boolean shouldOverrideUrlLoading(WebView view, String url) {
//                                view.loadUrl(url);
//                                return false;
//                        }
//
//                        @Override
//                        public void onPageStarted(WebView view, String url, Bitmap favicon) {
//                                super.onPageStarted(view, url, favicon);
//                                if (getView() != null) {
//                                        getView().progressBar.setVisibility(View.VISIBLE);
//                                        getView().messageTV.setVisibility(View.VISIBLE);
//                                        getView().messageTV.setText("Loading");
//                                }
//
//
//                        }
//
//                        @Override
//                        public void onPageFinished(WebView view, String url) {
//                                super.onPageFinished(view, url);
//                                if (getView() != null) {
//                                        getView().progressBar.setVisibility(View.GONE);
//                                        getView().messageTV.setVisibility(View.GONE);
//                                }
//
//                        }
//
//                        @Override
//                        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
//                                handler.proceed(); // Ignore SSL certificate errors
//                        }
//                });
//        }

        @Override
        protected void onEnterScope(MortarScope scope) {
                super.onEnterScope(scope);
                bus.register(this);
                App.getInstance().setPrevious();
                App.getInstance().currentPresenter = NAME;
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
        public void unregisterLeaderboardReceiver(UnregisterLeaderboardEvent event){
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
                        if(isFirsTime) {
//                                loadPage(url);
                                getView().leaderboardLV.setVisibility(View.VISIBLE);
                                getView().messageTV.setVisibility(View.GONE);
                                GameService.getInstance().loadRanking(Long.toString(me.getUID()));
                                isFirsTime = false;
                        }
                }
        }

        @Subscribe
        public void rankingEvent(RankingEvent event) {
                List<RankingItem> ranking = event.rankingItems;
                List<RankingItem> rankingToRemove = new ArrayList<>();
                int myPositionTmp = 0;
                boolean isMeInside = false;
                for(RankingItem r: ranking){
                        if(r.rank < 0){
                                rankingToRemove.add(r);
                        }
                        if(r.userId.equals(Long.toString(me.getUID()))){
                                isMeInside = true;
                        }

                        if(!isMeInside)//stop when we found the user ranking position
                                myPositionTmp++;
                }
                ranking.removeAll(rankingToRemove);
                final int myPosition = myPositionTmp;

                adapter = new LeaderboardAdapter(getView().getContext(), ranking, me.getUID());
                getView().leaderboardLV.setAdapter(adapter);

                if(isMeInside){
                        getView().leaderboardLV.post(new Runnable() {
                                @Override
                                public void run() {
                                        if (getView() != null && getView().leaderboardLV.getChildAt(0) != null) {
                                                int h2 = getView().leaderboardLV.getChildAt(0).getMeasuredHeight();
                                                smoothScrollToPositionFromTopWithBugWorkAround(getView().leaderboardLV, myPosition,
                                                        (getView().leaderboardLV.getMeasuredHeight() / 2) - (h2 / 2), 0);
                                        }
                                }
                        });

//                        getView().leaderboardLV.setSelection(myPosition);
                }

                getView().leaderboardLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                RankingItem i = adapter.getItem(position);
                                showDialog(i);
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

        public void showDialog(final RankingItem i){
                final AttendeeEntity user = AttendeesService.getInstance().getAttendeesByUID(Long.parseLong(i.userId));
                final Dialog dialog = new Dialog(getView().getContext());
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.dialog_user_badge_info);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

                TrackingService.getInstance().sendTracking("404", "games", "leaderboard", i.userId, "", "");

                CircleImageView closeBtn = (CircleImageView) dialog.findViewById(R.id.close_btn);
                TextView nameTV = (TextView) dialog.findViewById(R.id.name_text);
                TextView desTV = (TextView) dialog.findViewById(R.id.description_text);
                final TextView nameShortFormTV = (TextView) dialog.findViewById(R.id.name_short_form);
                final TextView messageTV = (TextView) dialog.findViewById(R.id.message);
                final CircleImageView personIV = (CircleImageView) dialog.findViewById(R.id.avatar_image);
                final ListView badgesLV = (ListView) dialog.findViewById(R.id.badges_list);

                nameTV.setText(i.name);
                if(user != null && user.getDescription() != null)
                        desTV.setText(user.getDescription());
                if(i.avatarId != null && !i.avatarId.isEmpty()){
                        try {
                                Picasso.with(getView().getContext()).load(Util.getPhotoUrlFromId(i.avatarId, 256))
                                        .memoryPolicy(MemoryPolicy.NO_CACHE)
                                        .networkPolicy(NetworkPolicy.NO_CACHE)
                                        .placeholder(R.drawable.empty_profile).into(personIV, new com.squareup.picasso.Callback() {
                                        @Override
                                        public void onSuccess() {
                                                nameShortFormTV.setVisibility(View.GONE);
                                        }

                                        @Override
                                        public void onError() { //when failed to load, draw a rect on top of the noavatar and add the name short form
                                                GameService.getInstance().drawNameShort(i.name, personIV, nameShortFormTV);
                                        }
                                });
                        }
                        catch(Throwable e){
                                Log.d("AAA", "leaderboardPresenter:" + e.toString());
                        }
                }
                else{
                        GameService.getInstance().drawNameShort(i.name, personIV, nameShortFormTV);
                }

                closeBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                                dialog.dismiss();
                        }
                });

                if(cm.getActiveNetworkInfo() != null) {
                        final Handler mainHandler = new Handler(getView().getContext().getMainLooper());
                        restClient.getBadgeApi().getBadges(i.userId, "null").subscribeOn(Schedulers.io())
                                .observeOn(Schedulers.computation())
                                .subscribe(new Action1<BadgeResponse>() {
                                        @Override
                                        public void call(BadgeResponse response) {
                                                if ("success".equals(response.status)) {
                                                        if (response.badgeItems != null) {
                                                                final List<BadgeEntity> badgeEntities = new ArrayList<BadgeEntity>();
                                                                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.ENGLISH);

                                                                try {
                                                                        for (BadgeItem badgeItem : response.badgeItems) {
                                                                                BadgeEntity badgeEntity = new BadgeEntity();
                                                                                badgeEntity.setId(Long.parseLong(badgeItem.id));
                                                                                badgeEntity.setBadges(badgeItem.badgeName);
                                                                                badgeEntity.setBadgesType(Integer.parseInt(badgeItem.badgeType));
                                                                                badgeEntity.setDescription(badgeItem.description);
                                                                                badgeEntity.setGameId(Integer.parseInt(badgeItem.gameId));
                                                                                badgeEntity.setImageId(badgeItem.imageUrl);
                                                                                badgeEntity.setMax(badgeItem.starCount);
                                                                                badgeEntity.setCountAchieved(badgeItem.countAchieved);
                                                                                badgeEntity.setKeyword(badgeItem.keyword);
                                                                                badgeEntity.setLastUpdated(new Date(df.parse(badgeItem.lastModifiedTime).getTime() + 1000)); //add additional 1 sec to round the microsecond
                                                                                badgeEntities.add(badgeEntity);
                                                                        }


                                                                        //sort by max count and type
                                                                        Collections.sort(badgeEntities, new Comparator<BadgeEntity>() {
                                                                                public int compare(BadgeEntity b1, BadgeEntity b2) {
                                                                                        int c;
                                                                                        c = Integer.valueOf(b1.getMax()).compareTo(b2.getMax());
                                                                                        if (c == 0)
                                                                                                c = Integer.valueOf(b1.getBadgesType()).compareTo(b2.getBadgesType());

                                                                                        return c;
                                                                                }
                                                                        });


                                                                        mainHandler.post(new Runnable() {
                                                                                @Override
                                                                                public void run() {
                                                                                        BadgesListAdapter badgesListAdapter = new BadgesListAdapter(getView().getContext(), bus, badgeEntities);
                                                                                        badgesLV.setAdapter(badgesListAdapter);
                                                                                }
                                                                        });

                                                                } catch (Exception e) {

                                                                }

                                                        }
                                                }
                                        }
                                }, new Action1<Throwable>() {
                                        @Override
                                        public void call(Throwable throwable) {
                                                Log.e("Mobisys:", "Cannot get attendee badges", throwable);
                                                mainHandler.post(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                                messageTV.setVisibility(View.VISIBLE);
                                                                messageTV.setText(getView().getContext().getResources().getText(R.string.no_badge));
                                                                badgesLV.setVisibility(View.GONE);
                                                        }
                                                });
                                        }
                                });
                }
                else{
                        messageTV.setVisibility(View.VISIBLE);
                        messageTV.setText(getView().getContext().getResources().getText(R.string.no_internet_connection));
                        badgesLV.setVisibility(View.GONE);
                }

                dialog.show();
        }
}
