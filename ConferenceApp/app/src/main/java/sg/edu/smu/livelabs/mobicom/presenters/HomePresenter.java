package sg.edu.smu.livelabs.mobicom.presenters;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import autodagger.AutoComponent;
import automortar.AutoScreen;
import flow.Flow;
import mortar.MortarScope;
import mortar.ViewPresenter;
import rx.functions.Action0;
import sg.edu.smu.livelabs.mobicom.ActionBarOwner;
import sg.edu.smu.livelabs.mobicom.App;
import sg.edu.smu.livelabs.mobicom.AppDependencies;
import sg.edu.smu.livelabs.mobicom.DaggerScope;
import sg.edu.smu.livelabs.mobicom.MainActivity;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.adapters.HomeCurrentActivityAdapter;
import sg.edu.smu.livelabs.mobicom.adapters.HomeEventAdapter;
import sg.edu.smu.livelabs.mobicom.adapters.HomeLeaderboardAdapter;
import sg.edu.smu.livelabs.mobicom.busEvents.BadgeNotiEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.BannerEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.BannerPostBackEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.RankingEvent;
import sg.edu.smu.livelabs.mobicom.flow.Layout;
import sg.edu.smu.livelabs.mobicom.models.AgendaEvent;
import sg.edu.smu.livelabs.mobicom.models.User;
import sg.edu.smu.livelabs.mobicom.models.data.EventEntity;
import sg.edu.smu.livelabs.mobicom.models.data.ScavengerEntity;
import sg.edu.smu.livelabs.mobicom.net.item.BannerItem;
//import sg.edu.smu.livelabs.mobicom.presenters.screen.FavoriteScreen;
//import sg.edu.smu.livelabs.mobicom.presenters.screen.InboxScreen;
//import sg.edu.smu.livelabs.mobicom.presenters.screen.ScavengerHuntDetailScreen;
//import sg.edu.smu.livelabs.mobicom.presenters.screen.SelfieScreen;
import sg.edu.smu.livelabs.mobicom.presenters.screen.InboxScreen;
import sg.edu.smu.livelabs.mobicom.services.AgendaService;
import sg.edu.smu.livelabs.mobicom.services.DatabaseService;
import sg.edu.smu.livelabs.mobicom.services.GameService;
import sg.edu.smu.livelabs.mobicom.services.MasterPointService;
import sg.edu.smu.livelabs.mobicom.services.ScavengerService;
import sg.edu.smu.livelabs.mobicom.services.TrackingService;
import sg.edu.smu.livelabs.mobicom.views.HomeView;

/**
 * Created by smu on 21/1/16.
 */
@AutoScreen(
        component = @AutoComponent(dependencies = MainActivity.class, superinterfaces = AppDependencies.class),
        screenAnnotations = Layout.class
)
@DaggerScope(HomePresenter.class)
@Layout(R.layout.home_view)
public class HomePresenter extends ViewPresenter<HomeView> {
    private static final int NUM_EVENT = 4;
    private ActionBarOwner actionBarOwner;
    private MainActivity mainActivity;
    private Context context;
    private Bus bus;
    private  ConnectivityManager cm;
    public static String NAME = "HomePresenter";
    private User me;
    private HomeLeaderboardAdapter homeLeaderboardAdapter;
    private HomeCurrentActivityAdapter homeCurrentActivityAdapter;
    private int currentIndexEvent;

    public HomePresenter(ActionBarOwner actionBarOwner, MainActivity mainActivity, Bus bus) {
        this.actionBarOwner = actionBarOwner;
        this.mainActivity = mainActivity;
        this.bus = bus;
    }

    @Override
    protected void onLoad(Bundle savedInstanceState) {
        super.onLoad(savedInstanceState);
        if (!hasView()){
            return;
        }
        Log.d(App.APP_TAG, "HomePresenter onload");
        mainActivity.messageBtn.setSelected(false);
        mainActivity.gamesBtn.setSelected(false);
        mainActivity.agendaBtn.setSelected(false);
        mainActivity.homeBtn.setSelected(true);
        mainActivity.moreBtn.setSelected(false);
        mainActivity.currentTab = MainActivity.HOME_TAB;
        mainActivity.setVisibleBottombar(View.VISIBLE);
        mainActivity.setVisibleToolbar(View.VISIBLE);
        context = getView().getContext();
        long unreadMsg = AgendaService.getInstance().getUnreadInbox();
        actionBarOwner.setConfig(new ActionBarOwner.Config(false, "MobiCom 2016",
                new ActionBarOwner.MenuAction(String.format("Inbox(%d)", unreadMsg), new Action0() {
                    @Override
                    public void call() {
                        Flow.get(context).set(new InboxScreen());
                    }
                })));

        me = DatabaseService.getInstance().getMe();

        getView().viewAllTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainActivity.setCurrentTab(MainActivity.AGENDA_TAB, AgendaPresenter.FULL_AGENDA);
            }
        });
        HomeEventAdapter homeEventAdapter = new HomeEventAdapter(context);
        getView().currentEventsList.setAdapter(homeEventAdapter);
        homeEventAdapter.setData(getCurrentDateEvents());
        if (currentIndexEvent != - 1){
            getView().currentEventsList.smoothScrollToPosition(currentIndexEvent);
        }
        getView().currentEventsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mainActivity.setCurrentTab(MainActivity.AGENDA_TAB, AgendaPresenter.FULL_AGENDA);
            }
        });
        cm = (ConnectivityManager) getView().getContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        GameService.getInstance().initMe();
        GameService.getInstance().loadHomeRanking(Long.toString(me.getUID()));
        GameService.getInstance().loadBanners(Long.toString(me.getUID()));
        GameService.getInstance().loadGameAPI(Long.toString(me.getUID()));
        MasterPointService.getInstance().getBadgesAPI();

        Runtime runtime = Runtime.getRuntime();
        if(runtime.freeMemory()< 20000000)
            System.gc();
    }

    @Override
    protected void onEnterScope(MortarScope scope) {
        super.onEnterScope(scope);
        bus.register(this);
        mainActivity.setVisibleBottombar(View.VISIBLE);
        App.getInstance().currentPresenter = NAME;
    }

    @Override
    protected void onExitScope() {
        super.onExitScope();
        GameService.getInstance().closeDialog();
        bus.unregister(this);
    }

    // get all events of a date
    private List<AgendaEvent> getCurrentDateEvents(){
        currentIndexEvent = -1;
        Date now = new Date();
        List<AgendaEvent> currentEvents = new ArrayList<>();
        List<Date> eventDates = AgendaService.getInstance().getAllDate();
        if(eventDates != null && eventDates.size() > 0){ //this portion ensure if now is before or after the event dates, it will show either the first day or last day of the event agenda
            if(now.before(eventDates.get(0))){
                now = eventDates.get(0);
            }
            else if(now.after(eventDates.get(eventDates.size()-1))){
                now = eventDates.get(eventDates.size()-1);
            }
        }

        List<EventEntity> dateEvents = AgendaService.getInstance().getParentEventByDate(now);
        if (dateEvents != null && dateEvents.size() > 0) {
            int count = dateEvents.size();
            AgendaEvent currentEvent = null;
            for (int i = 0; i < count; i++) {
                EventEntity eventEntity = dateEvents.get(i);
                if (now.getTime() >= eventEntity.getStartTime().getTime()
                        && now.getTime() <= eventEntity.getEndTime().getTime()) {
                    currentEvent = new AgendaEvent(eventEntity);
                    currentEvent.setCurrentEvent(true);
                    currentEvents.add(currentEvent);
                    currentIndexEvent = i;
                } else {
                    currentEvents.add(new AgendaEvent(eventEntity));
                }

            }
            return currentEvents;
        } else {
            // get event of first day
            List<Date> dates = AgendaService.getInstance().getAllDate();
            if (dates != null && dates.size() > 0){
                Date firstDate = dates.get(0);
                dateEvents = AgendaService.getInstance().getParentEventByDate(firstDate);
                if (dateEvents != null && dateEvents.size() > 0) {
                    int count = dateEvents.size();
                    for (int i = 0; i < count; i++) {
                        EventEntity eventEntity = dateEvents.get(i);
                        currentEvents.add(new AgendaEvent(eventEntity));
                    }
                    return currentEvents;
                }
            }
        }
        return currentEvents;
    }

    //get 4 events of date
    private List<AgendaEvent> getCurrentEvents(){
        Date now = new Date();
        List<AgendaEvent> currentEvents = new ArrayList<>();
        List<EventEntity> dateEvents = AgendaService.getInstance().getParentEventByDate(now);
        if (dateEvents != null && dateEvents.size() > 0){
            int count = dateEvents.size();
            int index = 0;
            AgendaEvent currentEvent = null;
            for (int i = 0; i < count; i++){
                EventEntity eventEntity = dateEvents.get(i);
                if (now.getTime() >= eventEntity.getStartTime().getTime()
                        && now.getTime() <= eventEntity.getEndTime().getTime()){
                    currentEvent = new AgendaEvent(eventEntity);
                    currentEvent.setCurrentEvent(true);
                    currentEvents.add(currentEvent);
                    break;
                }
                index++;
            }
            if (currentEvent != null){
                if (index == 0){
                    while (index < NUM_EVENT && index < dateEvents.size()){
                        index++;
                        currentEvents.add(new AgendaEvent(dateEvents.get(index)));
                    }
                } else {
                    int first = index - 1;
                    int more = NUM_EVENT - 2;
                    currentEvents.add(0, new AgendaEvent(dateEvents.get(first)));
                    while (index < dateEvents.size() && more > 0){
                        currentEvents.add(new AgendaEvent(dateEvents.get(index)));
                        index++;
                        more--;
                    }
                }
            } else {
                //today have event. but all haven't started or over.
                Calendar calendar = Calendar.getInstance();
                if (calendar.HOUR_OF_DAY < 12){
                    // in the morning get first 4 events of the day
                    index = 0;
                    for (EventEntity eventEntity : dateEvents){
                        if (index > 3) break;
                        currentEvents.add(new AgendaEvent(eventEntity));
                        index++;
                    }
                } else {
                    // at night get 4 event of next day
                    calendar.add(Calendar.DATE, 1);
                    currentEvents = getFirst4Events(calendar.getTime());
                    if (currentEvents == null || currentEvents.size() == 0){
                        currentEvents = getFirst4Events();
                    }
                }
            }
        } else {
            currentEvents = getFirst4Events();
        }
        return currentEvents;
    }

    private List<AgendaEvent> getFirst4Events() {
        List<Date> dates = AgendaService.getInstance().getAllDate();
        if (dates != null && dates.size() > 0){
            return getFirst4Events(dates.get(0));
        }
        return null;
    }

    private List<AgendaEvent> getFirst4Events(Date date){
        List<AgendaEvent> currentEvents = new ArrayList<>();
        List<EventEntity> dateEvents = AgendaService.getInstance().getParentEventByDate(date);
        int index = 0;
        for (EventEntity eventEntity : dateEvents){
            if (index > 3) break;
            currentEvents.add(new AgendaEvent(eventEntity));
            index++;
        }
        return currentEvents;
    }

    @Subscribe
    public void rankingEvent(final RankingEvent event){
        if(event.status.equals("success")) {
            homeLeaderboardAdapter = new HomeLeaderboardAdapter(context, event.rankingItems, me.getUID());
            getView().leaderboardList.setAdapter(homeLeaderboardAdapter);
//            getView().leaderboardList.setSelection(event.myPosition);

            getView().leaderboardList.post(new Runnable() {
                @Override
                public void run() {
                    if (getView() != null && getView().leaderboardList.getChildAt(0) != null) {
                        int h2 = getView().leaderboardList.getChildAt(0).getMeasuredHeight();
                        smoothScrollToPositionFromTopWithBugWorkAround(getView().leaderboardList, event.myPosition,
                                (getView().leaderboardList.getMeasuredHeight() / 2) - (h2 / 2), 0);
                    }
                }
            });

            getView().leaderboardList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    GameService.getInstance().showDialog(homeLeaderboardAdapter.getItem(position), getView().getContext(), cm, mainActivity);
                    TrackingService.getInstance().sendTracking("309", "mobisys",
                            "leaderboard", homeLeaderboardAdapter.getItem(position).userId, "", "");
                }
            });
        }
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
    public void loadPageEvent(BannerPostBackEvent event){
        if(event.status.equals("success")) {
            switch(event.keyword){
//                case "scavenger hunt":
//                    Date current = Calendar.getInstance().getTime();
//                    ScavengerEntity s = ScavengerService.getInstance().getScavengerHunt(event.id);
//                    if(s != null && s.getStartTime() != null && s.getEndTime() != null) {
//                        if (current.after(s.getStartTime()) && current.before(s.getEndTime())
//                                && !s.getIsCompleted()) {
//                            Flow.get(context).set(new ScavengerHuntDetailScreen(s));
//                        }
//                    }
//                    break;
//                case "favourite":
//                    Flow.get(getView().getContext()).set(new FavoriteScreen());
//                    break;
//                case "coolfie":
//                    Flow.get(getView().getContext()).set(new SelfieScreen());
//                    break;
            }
        }
    }

    @Subscribe
    public void bannerEvent(BannerEvent event){
        if(event.status.equals("success")) {
            List<BannerItem> bannerItems = new ArrayList<>();
            for(BannerItem b: event.bannerItems){
                if(b.status.toLowerCase().equals("active")){
                    bannerItems.add(b);
                }
            }
            if(bannerItems.size() > 0) {
                homeCurrentActivityAdapter = new HomeCurrentActivityAdapter(context, bannerItems);
                getView().currentActivityList.setAdapter(homeCurrentActivityAdapter);

                getView().currentActivityList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        BannerItem b =homeCurrentActivityAdapter.getItem(position);
                        TrackingService.getInstance().sendTracking("308", "mobisys",
                                "activities", b.id, b.keyword, "");
                        GameService.getInstance().openGamePage(b.keyword, b.targetId, mainActivity);
                    }
                });
            }
        }
    }

    @Subscribe
    public void badgeNotiEvent(final BadgeNotiEvent event){
        MasterPointService.getInstance().getBadgesAPI();
        new Handler().postDelayed(new Runnable() {
            public void run() {
                if(hasView()){
                    MasterPointService.getInstance().showToolTips(getView().content, event.badgeName);
                }
            }
        }, 1000);
    }



}
