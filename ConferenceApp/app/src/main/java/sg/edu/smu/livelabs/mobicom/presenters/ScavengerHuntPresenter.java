package sg.edu.smu.livelabs.mobicom.presenters;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ScrollView;

import com.squareup.otto.Bus;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import autodagger.AutoComponent;
import automortar.AutoScreen;
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
import sg.edu.smu.livelabs.mobicom.adapters.ScavengerHuntListAdapter;
import sg.edu.smu.livelabs.mobicom.busEvents.EndScavengerTimerEvent;
import sg.edu.smu.livelabs.mobicom.flow.Layout;
import sg.edu.smu.livelabs.mobicom.models.User;
import sg.edu.smu.livelabs.mobicom.models.data.GameListEntity;
import sg.edu.smu.livelabs.mobicom.models.data.ScavengerEntity;
import sg.edu.smu.livelabs.mobicom.net.RestClient;
import sg.edu.smu.livelabs.mobicom.net.response.ScavengerHuntListResponse;
//import sg.edu.smu.livelabs.mobicom.presenters.screen.ScavengerHuntDetailScreen;
import sg.edu.smu.livelabs.mobicom.presenters.screen.ScavengerHuntDetailScreen;
import sg.edu.smu.livelabs.mobicom.services.DatabaseService;
import sg.edu.smu.livelabs.mobicom.services.GameService;
import sg.edu.smu.livelabs.mobicom.services.ScavengerService;
import sg.edu.smu.livelabs.mobicom.services.TrackingService;
import sg.edu.smu.livelabs.mobicom.views.ScavengerHuntView;

/**
 * Created by johnlee on 22/2/16.
 */
@AutoScreen(
        component = @AutoComponent(dependencies = MainActivity.class, superinterfaces = AppDependencies.class),
        screenAnnotations = Layout.class
)
@DaggerScope(ScavengerHuntPresenter.class)
@Layout(R.layout.scavenger_view)
public class ScavengerHuntPresenter extends ViewPresenter<ScavengerHuntView>{

        private ActionBarOwner actionBarOwner;
        private final RestClient restClient;
        private Bus bus;
        public static String NAME = "ScavengerHuntPresenter";
        private MainActivity mainActivity;
        private User me;
        private boolean isScrolledUpFirstTime = true;

        private ScavengerHuntListAdapter scavengerHuntListAdapter;
        private List<ScavengerEntity> scavengerHunts;

        public ScavengerHuntPresenter(RestClient restClient, Bus bus, MainActivity mainActivity, ActionBarOwner actionBarOwner) {
                this.restClient = restClient;
                this.bus = bus;
                scavengerHunts = new ArrayList<>();
                this.actionBarOwner = actionBarOwner;
                this.mainActivity = mainActivity;
        }

        @Override
        protected void onLoad(Bundle savedInstanceState) {
                super.onLoad(savedInstanceState);
                Log.d(App.APP_TAG, " ScavengerHuntPresenter onload");

                GameListEntity game = GameService.getInstance().getGameByKeyword("scavenger hunt");
                if(game != null && !game.getGameName().isEmpty()){
                        actionBarOwner.setConfig(new ActionBarOwner.Config(true, game.getGameName(), null));
                }
                else{
                        actionBarOwner.setConfig(new ActionBarOwner.Config(true, "MOBY \"Seek\"", null));
                }



                me = DatabaseService.getInstance().getMe();

//                GameListEntity hunt = GameService.getInstance().getGame(2);
                if(game.getDescription() != null && !game.getDescription().isEmpty())
                        getView().descriptionTV.setText(game.getDescription());

                try{

                        loadScavengerHuntList();
//                        if(scavengerHunts == null || scavengerHunts.isEmpty()) {
////                                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
////                                formatter.setTimeZone(TimeZone.getTimeZone("Asia/Singapore"));
////                                Date date = (Date) formatter.parse("10/04/2016 23:00:00");
////                                Date edate = (Date) formatter.parse("11/04/2016 23:00:00");
////
////                                Date date2 = (Date) formatter.parse("12/04/2016 20:26:10");
////                                Date edate2 = (Date) formatter.parse("15/04/2016 20:27:00");
////
////                                Date date3 = (Date) formatter.parse("14/04/2016 20:26:10");
////                                Date edate3 = (Date) formatter.parse("14/04/2016 20:27:00");
////
////                                scavengerHunts.add(new ScavengerEntity(1l, 1l, "Hunt 1", "https://lh5.googleusercontent.com/Fdeop4TTj025TszQZOXqGAmmIz6vz-f0y_rTfc8MDNXjR3cqaOUHTcLdPfstDnkzQohZ04p_sJC-DMVp8Cfh_t9qgxocWs1Vzv0UdWfW8mzGvWlCkmR7crfhc17JiPnS9Q",
////                                        date, edate, "Find a qr code at admin block... very long very long very long very long very long very long very long very long", false, false,
////                                        "http://indochine-group.com/home/img/Fountain-of-Wealth-Suntec-City1.jpg"));
////
////                                scavengerHunts.add(new ScavengerEntity(2l, 2l, "Hunt 2", "https://lh5.googleusercontent.com/Fdeop4TTj025TszQZOXqGAmmIz6vz-f0y_rTfc8MDNXjR3cqaOUHTcLdPfstDnkzQohZ04p_sJC-DMVp8Cfh_t9qgxocWs1Vzv0UdWfW8mzGvWlCkmR7crfhc17JiPnS9Q",
////                                        date2, edate2, "Find a qr code at admin block... very long very long very long very long very long very long very long very long", false, false,
////                                        "http://indochine-group.com/home/img/Fountain-of-Wealth-Suntec-City1.jpg"));
////
////                                scavengerHunts.add(new ScavengerEntity(3l, 3l, "Hunt 3", "https://lh5.googleusercontent.com/Fdeop4TTj025TszQZOXqGAmmIz6vz-f0y_rTfc8MDNXjR3cqaOUHTcLdPfstDnkzQohZ04p_sJC-DMVp8Cfh_t9qgxocWs1Vzv0UdWfW8mzGvWlCkmR7crfhc17JiPnS9Q",
////                                        date3, edate3, "Find a qr code at admin block... very long very long very long very long very long very long very long very long", false, false,
////                                        "http://indochine-group.com/home/img/Fountain-of-Wealth-Suntec-City1.jpg"));
////                                ScavengerService.getInstance().updateScavengerHuntList2(scavengerHunts);
//                        }



                        getView().scavengerHuntList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                        ScavengerEntity scavengerHunt = scavengerHuntListAdapter.getItem(position);
                                        Date current = Calendar.getInstance().getTime();

                                        if (scavengerHunt != null) {
                                                if (current.after(scavengerHunt.getStartTime()) && current.before(scavengerHunt.getEndTime())
                                                        && !scavengerHunt.getIsCompleted()) {
                                                        TrackingService.getInstance().sendTracking("407", "games",
                                                                "scav_hunt", Long.toString(scavengerHunt.getHuntId()), "", "");
                                                        Flow.get(getView().getContext()).set(new ScavengerHuntDetailScreen(scavengerHunt));
                                                }
                                        }
                                }
                        });


                }
                catch(Exception e){}

//                Runtime runtime = Runtime.getRuntime();
//                if(runtime.freeMemory()< 20000000)
//                        System.gc();

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
                bus.post(new EndScavengerTimerEvent());
                bus.unregister(this);
                if(!GameService.getInstance().fromGames(App.getInstance().previousPresenter)){
                        mainActivity.setVisibleBottombar(View.VISIBLE);
                }
                if (NAME.equals(App.getInstance().currentPresenter)){
                        App.getInstance().currentPresenter = "";
                }
        }

        private void loadScavengerHuntList(){
                ScavengerEntity s = ScavengerService.getInstance().getLastUpdateScavengerHunt();
//                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.ENGLISH);
                String lastTime = "2016-01-01";//s != null ? df.format(s.getLastUpdate()) : "2016-01-01";

                final Handler mainHandler = new Handler(getView().getContext().getMainLooper());
                restClient.getScavengerApi().getScavengerHuntList(lastTime, Long.toString(me.getUID()))
                        .subscribeOn(Schedulers.io())
                        .subscribe(new Action1<ScavengerHuntListResponse>() {
                                @Override
                                public void call(final ScavengerHuntListResponse response) {
                                        UIHelper.getInstance().dismissProgressDialog();

                                        if (response.details != null && response.details.size() > 0){
                                                scavengerHunts = ScavengerService.getInstance().updateScavengerHuntList(response.details);

                                                mainHandler.post(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                                //load web page
                                                                if (getView() != null && scavengerHunts != null) {

                                                                        final Date now = new Date();
                                                                        List<ScavengerEntity> sTmp = new ArrayList<ScavengerEntity>();
                                                                        for (ScavengerEntity s : scavengerHunts) {
                                                                                if (!(s.getStartTime().before(now) && s.getEndTime().after(now))) { //not current hunt
                                                                                        sTmp.add(s);
                                                                                }
                                                                        }

                                                                        scavengerHunts.removeAll(sTmp);

                                                                        Collections.sort(scavengerHunts, new Comparator<ScavengerEntity>
                                                                                () {
                                                                                public int compare(ScavengerEntity o1, ScavengerEntity o2) {
                                                                                        return o1.getStartTime().compareTo(o2.getStartTime());
                                                                                }
                                                                        });

                                                                        Collections.sort(sTmp, new Comparator<ScavengerEntity>
                                                                                () {
                                                                                public int compare(ScavengerEntity o1, ScavengerEntity o2) {
                                                                                        return o2.getStartTime().compareTo(o1.getStartTime());
                                                                                }
                                                                        });

                                                                        scavengerHunts.addAll(sTmp);

                                                                        scavengerHuntListAdapter = new ScavengerHuntListAdapter(getView(), scavengerHunts, bus);
                                                                        getView().scavengerHuntList.setAdapter(scavengerHuntListAdapter);


                                                                        getView().scrollView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                                                                                @Override
                                                                                public void onGlobalLayout() {
                                                                                        // Ready, move up
                                                                                        if (isScrolledUpFirstTime) {
                                                                                                Handler handler = new Handler();
                                                                                                handler.postDelayed(new Runnable() {
                                                                                                        @Override
                                                                                                        public void run() {
                                                                                                                if (getView() != null) {
                                                                                                                        isScrolledUpFirstTime = false;
                                                                                                                        getView().scrollView.fullScroll(ScrollView.FOCUS_UP);
                                                                                                                }
                                                                                                        }
                                                                                                }, 2000);
                                                                                        }

                                                                                }
                                                                        });
                                                                }
                                                        }
                                                });
                                        }
                                }
                        }, new Action1<Throwable>() {
                                @Override
                                public void call(Throwable throwable) {
                                        Log.e("Mobisys: ", "cannot get scavenger hunt", throwable);
                                }
                        });
        }

}
