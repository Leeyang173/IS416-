package sg.edu.smu.livelabs.mobicom.presenters;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.util.Log;
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
import automortar.ScreenParam;
import flow.Flow;
import flow.path.Path;
import mortar.MortarScope;
import mortar.ViewPresenter;
import rx.functions.Action0;
import sg.edu.smu.livelabs.mobicom.ActionBarOwner;
import sg.edu.smu.livelabs.mobicom.App;
import sg.edu.smu.livelabs.mobicom.AppDependencies;
import sg.edu.smu.livelabs.mobicom.DaggerScope;
import sg.edu.smu.livelabs.mobicom.MainActivity;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.UIHelper;
import sg.edu.smu.livelabs.mobicom.adapters.SlidePagerAdapter;
import sg.edu.smu.livelabs.mobicom.busEvents.AgendaLoadEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.AgendaReloadingEvent;
import sg.edu.smu.livelabs.mobicom.flow.Layout;
import sg.edu.smu.livelabs.mobicom.presenters.screen.AgendaPaperScreen;
import sg.edu.smu.livelabs.mobicom.presenters.screen.AgendaTabScreen;
import sg.edu.smu.livelabs.mobicom.services.AgendaService;
import sg.edu.smu.livelabs.mobicom.services.ScreenService;
import sg.edu.smu.livelabs.mobicom.services.TrackingService;
import sg.edu.smu.livelabs.mobicom.views.AgendaView;

/**
 * Created by smu on 22/2/16.
 */
@AutoScreen(
        component = @AutoComponent(dependencies = MainActivity.class, superinterfaces = AppDependencies.class),
        screenAnnotations = Layout.class
)
@DaggerScope(AgendaPresenter.class)
@Layout(R.layout.agenda_view)
public class AgendaPresenter extends ViewPresenter<AgendaView>{
    private static String NAME = "AgendaPresenter";
    public static final int DAYS_OF_WEEK = 7;
    public static final int FULL_AGENDA = ActionBarOwner.Config.MIDDLE_FOCUS;
    public static final int MY_AGENDA = ActionBarOwner.Config.LEFT_FOCUS;
    public static final int PAPER = ActionBarOwner.Config.RIGHT_FOCUS;
    public static int typeAgenda;

    private Bus bus;
    private Context context;
    private SlidePagerAdapter pagerAdapter;
    private ActionBarOwner actionBarOwner;

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE");
    private SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("d");

    private List<Date> eventDates;// just some days have event
    private List<Day> days; // all 7 days
    private int currentIndex; // all 7 days
    private ScreenService screenService;
    private MainActivity mainActivity;
    private MortarScope scope;

    public AgendaPresenter(Bus bus, ActionBarOwner actionBarOwner,
                           MainActivity mainActivity,
                           ScreenService screenService,
                           @ScreenParam int typeAgenda){
        this.bus = bus;
        this.mainActivity = mainActivity;
        this.actionBarOwner = actionBarOwner;
        this.typeAgenda = typeAgenda;
        this.screenService = screenService;
        this.currentIndex = -1;
        days = new ArrayList<>();
    }

    @Override
    protected void onLoad(Bundle savedInstanceState) {
        super.onLoad(savedInstanceState);
        if (!hasView()) return;
        context = getView().getContext();
        mainActivity.setVisibleBottombar(View.VISIBLE);
        //enable bottom bar
        mainActivity.messageBtn.setSelected(false);
        mainActivity.gamesBtn.setSelected(false);
        mainActivity.agendaBtn.setSelected(true);
        mainActivity.homeBtn.setSelected(false);
        mainActivity.moreBtn.setSelected(false);
        mainActivity.currentTab = MainActivity.AGENDA_TAB;
        //back to previous state if have
        try {
            currentIndex = screenService.pop(AgendaPresenter.class);
//            typeAgenda = screenService.pop(AgendaPresenter.class);
        } catch (Exception e){
            Log.d(App.APP_TAG, e.toString());
        }
        actionBarOwner.setConfig(new ActionBarOwner.Config(true,
                new ActionBarOwner.MenuAction("Bookmark", new Action0() {
                    @Override
                    public void call() {
                        typeAgenda = MY_AGENDA;
                        bus.post(new AgendaReloadingEvent());
                        TrackingService.getInstance().sendTracking("111", "agenda", "bookmark", "", "", "");
                    }
                }),
                new ActionBarOwner.MenuAction("Agenda", new Action0() {
                    @Override
                    public void call() {
                        typeAgenda = FULL_AGENDA;
                        bus.post(new AgendaReloadingEvent());
                        TrackingService.getInstance().sendTracking("102", "agenda", "agenda", "", "", "");
                    }
                }),
                new ActionBarOwner.MenuAction("Papers", new Action0() {
                    @Override
                    public void call() {
                        Flow.get(context).set(new AgendaPaperScreen());
                        TrackingService.getInstance().sendTracking("119", "agenda", "paper", "", "", "");
                    }
                }), typeAgenda));

//        actionBarOwner.setConfig(new ActionBarOwner.Config(true, "Agenda", null));
        bus.post(new AgendaReloadingEvent());
        UIHelper.getInstance().showProgressDialog(context, context.getString(R.string.loading), true);
        final SharedPreferences sharedPreferences = context.getSharedPreferences(MainActivity.SHARE_PREFERENCES, Context.MODE_PRIVATE);
        String lastUpdatedTimeZone = sharedPreferences.getString(MainActivity.TIMEZONE, "");
        if(Calendar.getInstance().getTimeZone().getID().equals(lastUpdatedTimeZone)) {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    loadData(true);
                }
            }, 300);
        }
        else{
            AgendaService.getInstance().syncEvent(false);
            //wait for reload from Agenda Service
        }
    }

    @Override
    protected void onEnterScope(MortarScope scope) {
        super.onEnterScope(scope);
        this.scope = scope;
        bus.register(this);
        if(App.getInstance().currentPresenter.equals(MorePresenter.NAME)){
            App.getInstance().setPrevious();
        }

        App.getInstance().currentPresenter = NAME;
    }

    @Override
    protected void onExitScope() {
        screenService.push(AgendaPresenter.class, typeAgenda);
        screenService.push(AgendaPresenter.class, currentIndex);
        super.onExitScope();
        bus.unregister(this);
        App.getInstance().previousPresenter = "";
    }

    public void changeTo(int index) {
        Date selectedDate = days.get(index).date;
        int i = 0;
        for (Date date: eventDates){
            if(date.getTime() == selectedDate.getTime()){
                getView().viewPager.setCurrentItem(i);
                return;
            }
            i++;
        }
    }

    private void setSelected(Date date){
        int i = 0;
        for (Day day : days){
            if (day.date.getTime() == date.getTime()){
                getView().dateViews[currentIndex].setSelected(false);
                currentIndex = i;
                getView().dateViews[currentIndex].setSelected(true);
                if (typeAgenda == FULL_AGENDA){
                    TrackingService.getInstance().sendTracking("103", "agenda", TrackingService.getInstance().simpleDateFormatTracking.format(date), "", "", "");
                } else {
                    TrackingService.getInstance().sendTracking("103", "agenda", "bookmark", TrackingService.getInstance().simpleDateFormatTracking.format(date), "", "");
                }
                return;
            }
            i++;
        }
    }

    private void createDates(List<Date> dates){
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
            getView().dateViews[i].setData(day.weekDay, day.monthDay, day.enable);
        }
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

    @Subscribe
    public void reloadNewAgendaUsingNewTimeZone(AgendaLoadEvent event){
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                loadData(false);
            }
        }, 300);
    }

    private void loadData(final boolean toLoadData){
        new AsyncTask<Void, Void, List<Date>>() {

            @Override
            protected List<Date> doInBackground(Void... params) {
                if(toLoadData) {
                    AgendaService.getInstance().syncEvent(false);
                }
                return AgendaService.getInstance().getAllDate();
            }

            @Override
            protected void onPostExecute(List<Date> result) {
                super.onPostExecute(result);
                eventDates = result;
                if (eventDates == null) return;
                try {
                    if (getView() == null|| scope == null || scope.isDestroyed()) return;
                    createDates(eventDates);
                    Path[] agendaTabScreens = new Path[eventDates.size()];
                    int i = 0;
                    for (Date date1 : eventDates) {
                        agendaTabScreens[i++] = new AgendaTabScreen(date1);
                    }
                    pagerAdapter = new SlidePagerAdapter(context, agendaTabScreens);
                    getView().viewPager.setAdapter(pagerAdapter);
                    getView().viewPager.setOffscreenPageLimit(1);

                    getView().viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                        @Override
                        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                        }

                        @Override
                        public void onPageSelected(int position) {
                            setSelected(eventDates.get(position));
                        }

                        @Override
                        public void onPageScrollStateChanged(int state) {

                        }
                    });
                    if (currentIndex != -1) {
                        getView().dateViews[currentIndex].setSelected(true);
                        changeTo(currentIndex);
                    } else {
                        //TODO Go to current day
                        currentIndex = 0;
                        Calendar today = Calendar.getInstance();
                        today.set(Calendar.MILLISECOND, 0);
                        today.set(Calendar.SECOND, 0);
                        today.set(Calendar.MINUTE, 0);
                        today.set(Calendar.HOUR_OF_DAY, 0);
                        i = 0;
                        boolean selected = false;
                        for (Date date : eventDates) {
                            if (date.getTime() == today.getTimeInMillis()) {
                                setSelected(date);
                                getView().viewPager.setCurrentItem(i);
                                selected = true;
                            }
                            i++;
                        }
                        if (!selected && eventDates.size() > 0) {
                            setSelected(eventDates.get(0));
                            getView().viewPager.setCurrentItem(0);
                        }
                    }
                }
                catch (Throwable e){
                    Log.e(App.APP_TAG, "AgendaPresenter " + e.toString());
                }
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        UIHelper.getInstance().dismissProgressDialog();
                    }
                }, 200);
            }
        }.execute();
    }
}
