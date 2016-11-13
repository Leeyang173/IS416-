package sg.edu.smu.livelabs.mobicom.presenters;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import autodagger.AutoComponent;
import automortar.AutoScreen;
import flow.Flow;
import flow.path.Path;
import mortar.MortarScope;
import mortar.ViewPresenter;
import sg.edu.smu.livelabs.mobicom.ActionBarOwner;
import sg.edu.smu.livelabs.mobicom.App;
import sg.edu.smu.livelabs.mobicom.AppDependencies;
import sg.edu.smu.livelabs.mobicom.DaggerScope;
import sg.edu.smu.livelabs.mobicom.MainActivity;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.adapters.SlidePagerAdapter;
import sg.edu.smu.livelabs.mobicom.busEvents.BadgeNotiEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.MemoriesListEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.MemoriesResetHomeEvent;
import sg.edu.smu.livelabs.mobicom.flow.Layout;
//import sg.edu.smu.livelabs.mobicom.presenters.screen.MemoriesHomeScreen;
//import sg.edu.smu.livelabs.mobicom.presenters.screen.MemoriesListScreen;
import sg.edu.smu.livelabs.mobicom.presenters.screen.MemoriesHomeScreen;
import sg.edu.smu.livelabs.mobicom.presenters.screen.MemoriesListScreen;
import sg.edu.smu.livelabs.mobicom.services.MasterPointService;
import sg.edu.smu.livelabs.mobicom.services.MemoriesService;
import sg.edu.smu.livelabs.mobicom.services.ScreenService;
import sg.edu.smu.livelabs.mobicom.views.MemoriesMainView;

/**
 * Created by smu on 26/10/15.
 */
@AutoScreen(
        component = @AutoComponent(dependencies = MainActivity.class, superinterfaces = AppDependencies.class),
        screenAnnotations = Layout.class
)
@DaggerScope(MemoriesMainPresenter.class)
@Layout(R.layout.memories_view)
public class MemoriesMainPresenter extends ViewPresenter<MemoriesMainView> implements View.OnClickListener{
    public static String NAME = "MemoriesMainPresenter";
    public static final int HOME_TAB = 0;
    public static final int LIST_TAB = 3;

    private int currentTab;
    private String currentTabName;
    private Bus bus;
    private ActionBarOwner actionBarOwner;
    private SlidePagerAdapter pagerAdapter;
    private MainActivity mainActivity;
    private ScreenService screenService;
    private Context context;

    public static final int DAYS_OF_WEEK = 7;
    private List<Date> eventDates;// just some days have event
    private List<Day> days; // all 7 days
    private int currentIndex; // all 7 days
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE");
    private SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("d");

    public MemoriesMainPresenter(ActionBarOwner actionBarOwner, Bus bus, MainActivity mainActivity, ScreenService screenService) {
        this.actionBarOwner = actionBarOwner;
        this.bus = bus;
        this.mainActivity = mainActivity;
        this.screenService = screenService;
        this.currentIndex = -1;
        days = new ArrayList<>();
    }

    @Override
    protected void onLoad(Bundle savedInstanceState) {
        super.onLoad(savedInstanceState);
        currentTab = -1;
        createDates();
        actionBarOwner.setConfig(new ActionBarOwner.Config(true, "Memories", null));

        context = getView().getContext();
        Path[] paths = new Path[]{
                new MemoriesHomeScreen(mainActivity),
                new MemoriesListScreen(screenService, mainActivity)
//                ,
//                new SelfieSearchScreen(screenService),
//                new SelfieListScreen(screenService, mainActivity),
//                new SelfieProfileScreen(screenService, mainActivity),
//                new SelfieLikersScreen(screenService)
        };
        pagerAdapter = new SlidePagerAdapter(getView().getContext(), paths);
        getView().pager.setOffscreenPageLimit(0);
        getView().pager.setAdapter(pagerAdapter);
        try {
            MemoriesListEvent event = screenService.pop(MemoriesListPresenter.class);
            if (event == null) {
                setSelected(HOME_TAB);
            } else {
                openSelfieListView(event);
            }
        }
        catch(ClassCastException e){
            setSelected(HOME_TAB);
        }
        MemoriesService.getInstance().setUserId();
    }


    @Override
    protected void onEnterScope(MortarScope scope) {
        super.onEnterScope(scope);
        bus.register(this);
//        App.getInstance().setPrevious();
        App.getInstance().currentPresenter = NAME;
        mainActivity.setVisibleBottombar(View.GONE);
    }

    @Override
    protected void onExitScope() {
        super.onExitScope();
        bus.unregister(this);

        if(!App.getInstance().currentPresenter.equals("MemoriesFullScreenPresenter")) {
            mainActivity.setVisibleBottombar(View.VISIBLE);
        }

        if (NAME.equals(App.getInstance().currentPresenter)){
            App.getInstance().currentPresenter = "";
        }
    }

    @Override
    public void onClick(View v) {
        try {
//            screenService.clearStack(SelfieLikersPresenter.class);
            screenService.clearStack(MemoriesListPresenter.class);
//            screenService.clearStack(SelfieProfilePresenter.class);
        }catch (Exception e){
//            Log.d("XXX: cannot clear stack", e.toString());
        }
        MemoriesListPresenter.previousPage = - 1;
//        SelfieProfilePresenter.previousPage = -1;
    }


    @Subscribe
    public void openSelfieListView(MemoriesListEvent event){
        if (currentTab == LIST_TAB) return;
        if (event.isFromMainPage || event.hasExecuted) return;
        event.isFromMainPage = true;
        setSelected(LIST_TAB);
        postDelay100(event);
    }

    private void postDelay100(final Object event){
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                bus.post(event);
            }
        }, 100);
    }

    public boolean goBack(){
        switch (currentTab){
            case LIST_TAB:
                setSelected(MemoriesListPresenter.previousPage);
                break;
            default:
                Flow.get(context).goBack();
                break;
        }
        return true;
    }

    private void setSelectedDate(Date date){
        int i = 0;
        for (Day day : days){
            if (day.date.getTime() == date.getTime()){
                getView().dateViews[currentIndex].setSelected(false);
                currentIndex = i;
                getView().dateViews[currentIndex].setSelected(true);
                return;
            }
            i++;
        }
    }

    private void setSelectedDateByIndex(int index){
        getView().dateViews[currentIndex].setSelected(false);
        currentIndex = index;
        getView().dateViews[index].setSelected(true);
        MemoriesService.getInstance().currentSelectedDate = days.get(currentIndex).date;
    }

    private class Day {
        Date date;
        String weekDay;
        String monthDay;
        boolean enable;

        public Day(Date date, boolean enable) {
            this.date = date;
            this.enable = enable;
            weekDay = simpleDateFormat.format(date);
            monthDay = simpleDateFormat1.format(date);
        }
    }

    private void createDates(){
        List<Date> dates = new ArrayList<>();
        Date startDate = MemoriesService.getInstance().startDate;
        if(startDate == null){
            Calendar cTmp = Calendar.getInstance();
            cTmp.set(Calendar.YEAR, 2016);
            cTmp.set(Calendar.MONTH, Calendar.OCTOBER); //0 is Jan
            cTmp.set(Calendar.DAY_OF_MONTH, 3);
            startDate = cTmp.getTime();
        }
        Calendar c = Calendar.getInstance();
        c.setTime(startDate);
        dates.add(c.getTime());
        c.add(Calendar.DAY_OF_MONTH, 1);
        dates.add(c.getTime());
        c.add(Calendar.DAY_OF_MONTH, 1);
        dates.add(c.getTime());
        c.add(Calendar.DAY_OF_MONTH, 1);
        dates.add(c.getTime());
        c.add(Calendar.DAY_OF_MONTH, 1);
        dates.add(c.getTime());
//        c.add(Calendar.DAY_OF_MONTH, 1);
//        dates.add(c.getTime());
//        c.add(Calendar.DAY_OF_MONTH, 1);
//        dates.add(c.getTime());


        if (dates == null || dates.size() == 0) return;
        this.days.clear();

        for (Date date: dates){
                this.days.add(new Day(date, true));
        }
        if (dates.size() < DAYS_OF_WEEK){
            int dayBefore = Math.round ((DAYS_OF_WEEK - dates.size())/2);
            Date firstDay = dates.get(0);
            long oneDay = 24*60*60*1000;
            for (int i = 1; i <= dayBefore; i++){
                Date date = new Date(firstDay.getTime() - i * oneDay);
                this.days.add(0, new Day(date, false));
            }
            Date lastDay = dates.get(dates.size() - 1);
            int dayAfter = DAYS_OF_WEEK - this.days.size();
            for (int i = 1; i <= dayAfter; i++){
                Date date = new Date(lastDay.getTime() + i * oneDay);
                this.days.add(new Day(date, false));
            }
        }

        resetData();
    }

    private void resetData(){
        for (int i = 0; i < DAYS_OF_WEEK; i++){
            Day day = days.get(i);
            if(getView().dateViews != null) {
                getView().dateViews[i].setData(day.weekDay, day.monthDay, day.enable, day.date);//day.enable);
            }

            if(day.date.equals(MemoriesService.getInstance().currentSelectedDate)) {
                if(currentIndex > -1) {
                    setSelectedDateByIndex(i);
                }
                else{
                    getView().dateViews[i].setSelected(true);
                }
                currentIndex = i;
            }
        }

    }

    public Bus getBus() {
        return bus;
    }

    private void setSelected(int tab) {
        switch (tab) {
            case HOME_TAB:
                getView().pager.setCurrentItem(0, false);
                currentTabName = "main";
                break;
            case LIST_TAB:
                if (currentTab != - 1){
                    MemoriesListPresenter.previousPage = currentTab;
                }

                getView().pager.setCurrentItem(2, false);
                currentTabName = "list";
                break;

        }
        currentTab = tab;
    }

    @Subscribe
    public void resetPhotos(MemoriesResetHomeEvent event){
        if(currentTab == LIST_TAB){
            setSelected(MemoriesListPresenter.previousPage);
        }
        setSelectedDateByIndex(event.index);
    }

    @Subscribe
    public void badgeNotiEvent(BadgeNotiEvent event){
        MasterPointService.getInstance().getBadgesAPI();
        MasterPointService.getInstance().showToolTips(getView().pager, event.badgeName);
    }
}
