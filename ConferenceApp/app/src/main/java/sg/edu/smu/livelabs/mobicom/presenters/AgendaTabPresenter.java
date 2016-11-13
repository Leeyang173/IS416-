package sg.edu.smu.livelabs.mobicom.presenters;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.View;

import com.marshalchen.ultimaterecyclerview.stickyheadersrecyclerview.StickyRecyclerHeadersDecoration;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import autodagger.AutoComponent;
import automortar.AutoScreen;
import automortar.ScreenParam;
import flow.Flow;
import mortar.MortarScope;
import mortar.ViewPresenter;
import sg.edu.smu.livelabs.mobicom.App;
import sg.edu.smu.livelabs.mobicom.AppDependencies;
import sg.edu.smu.livelabs.mobicom.DaggerScope;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.adapters.AgendaAdapter;
import sg.edu.smu.livelabs.mobicom.busEvents.AgendaReloadingEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.AgendaUpdatedTopicEvent;
import sg.edu.smu.livelabs.mobicom.flow.Layout;
import sg.edu.smu.livelabs.mobicom.models.AgendaEvent;
import sg.edu.smu.livelabs.mobicom.models.data.EventEntity;
//import sg.edu.smu.livelabs.mobicom.presenters.screen.AgendaTopicDetailScreen;
import sg.edu.smu.livelabs.mobicom.presenters.screen.AgendaTopicDetailScreen;
import sg.edu.smu.livelabs.mobicom.services.AgendaService;
import sg.edu.smu.livelabs.mobicom.views.AgendaTabView;

/**
 * Created by smu on 25/2/16.
 */

@AutoScreen(
        component = @AutoComponent(dependencies = AgendaPresenter.class, superinterfaces = AppDependencies.class),
        screenAnnotations = Layout.class
)
@DaggerScope(AgendaTabPresenter.class)
@Layout(R.layout.agenda_tab_view)
public class AgendaTabPresenter extends ViewPresenter<AgendaTabView> implements AgendaAdapter.AgendaListener {

    private Bus bus;
    private Context context;
    private AgendaAdapter agendaAdapter;
    private Date date;
    private HashMap<Long, AgendaEvent> mapEvents;
    private boolean needToSync;
    private int nowIndex = -1;

    public AgendaTabPresenter(Bus bus, @ScreenParam Date date){
        this.bus = bus;
        this.date = date;
    }

    @Override
    protected void onLoad(Bundle savedInstanceState) {
        super.onLoad(savedInstanceState);
        context = getView().getContext();
        agendaAdapter = new AgendaAdapter(context, this);
        getView().ultimateRecyclerView.setHasFixedSize(false);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        getView().ultimateRecyclerView.setLayoutManager(linearLayoutManager);
        getView().ultimateRecyclerView.setAdapter(agendaAdapter);
        StickyRecyclerHeadersDecoration headersDecor = new StickyRecyclerHeadersDecoration(agendaAdapter);
        getView().ultimateRecyclerView.addItemDecoration(headersDecor);
        getView().ultimateRecyclerView.disableLoadmore();
        needToSync = true;
        reloadData();
    }

    @Override
    protected void onEnterScope(MortarScope scope) {
        super.onEnterScope(scope);
        bus.register(this);
    }

    @Override
    protected void onExitScope() {
        super.onExitScope();
        bus.unregister(this);
    }

    @Subscribe
    public void reload(AgendaReloadingEvent event){
        needToSync = false;
        reloadData();
    }

    @Subscribe
    public void UpdateTopic(AgendaUpdatedTopicEvent event){
        switch (event.type){
            case AgendaUpdatedTopicEvent.ADD_OR_REMOVE_FROM_MY_AGENDA:
                needToSync = false;
                reloadData();
                break;
        }
    }

    private void reloadData(){
        new AsyncTask<Void, Void, List<AgendaEvent>>(){

            @Override
            protected List<AgendaEvent>  doInBackground(Void... params) {
                return loadData();
            }

            @Override
            protected void onPostExecute(List<AgendaEvent> events) {
                super.onPostExecute(events);
                try {
                    if (events.isEmpty()){
                        if (AgendaPresenter.typeAgenda == AgendaPresenter.MY_AGENDA){
                            getView().emptyTV.setHint(context.getString(R.string.empty_bookmark));
                        } else {
                            getView().emptyTV.setHint(context.getString(R.string.empty_agenda));
                        }
                        getView().emptyTV.setVisibility(View.VISIBLE);
                        getView().ultimateRecyclerView.setVisibility(View.GONE);
                    } else {
                        agendaAdapter.setData(events);
                        if (nowIndex != -1){
                            getView().ultimateRecyclerView.scrollVerticallyToPosition(nowIndex);
                        }
                        getView().ultimateRecyclerView.setVisibility(View.VISIBLE);
                        getView().emptyTV.setVisibility(View.GONE);
                    }
                }catch (Exception e){
                    Log.d(App.APP_TAG, e.toString());
                }

            }
        }.execute();
    }

    private List<AgendaEvent> loadData(){
        nowIndex = -1;
        StringBuilder ratedEvent = new StringBuilder();
        List<EventEntity>  eventEntities;
        if (AgendaPresenter.typeAgenda == AgendaPresenter.FULL_AGENDA){
            eventEntities = AgendaService.getInstance().getEventByDate(date);
        } else {
            eventEntities = AgendaService.getInstance().getMyEventByDate(date);
        }
        List<AgendaEvent> events = new ArrayList<>();
        mapEvents = new HashMap<Long, AgendaEvent>();
        AgendaEvent agendaEvent;
        long now = System.currentTimeMillis();
        if (eventEntities != null && eventEntities.size() > 0){
            for (EventEntity eventEntity: eventEntities){
                if (eventEntity.getParentId() == 0){
                    if (AgendaService.ACTIVE.equals(eventEntity.getStatus())){
                        if (mapEvents.containsKey(eventEntity.getServerId())){
                            agendaEvent = mapEvents.get(eventEntity.getServerId());
                            agendaEvent.setEventEntity(eventEntity);
                        } else {
                            agendaEvent = new AgendaEvent(eventEntity);
                            mapEvents.put(agendaEvent.getServerId(), agendaEvent);
                        }
                        if(eventEntity.getStartTime().getTime() <= now
                                && eventEntity.getEndTime().getTime() >= now){
                            agendaEvent.setCurrentEvent(true);
                            nowIndex = events.size();
                        }
                        events.add(agendaEvent) ;
                        if (AgendaService.MULTIPLE.equals(eventEntity.getEventType())){
                            ratedEvent.append(eventEntity.getServerId());
                            ratedEvent.append(",");
                        }
                    }
                } else {
                    if (AgendaService.ACTIVE.equals(eventEntity.getStatus())){
                        long parentId = eventEntity.getParentId();
                        if (mapEvents.containsKey(parentId)){
                            agendaEvent = mapEvents.get(parentId);
                            agendaEvent.addSubEvents(eventEntity);
                        } else {
                            agendaEvent = new AgendaEvent();
                            agendaEvent.addSubEvents(eventEntity);
                            mapEvents.put(parentId, agendaEvent);
                        }
                        ratedEvent.append(eventEntity.getServerId());
                        ratedEvent.append(",");
                    }
                }

            }
        }

        if (AgendaPresenter.typeAgenda == AgendaPresenter.FULL_AGENDA && needToSync
                && ratedEvent.length() > 0){
            ratedEvent.deleteCharAt(ratedEvent.length() - 1);
            AgendaService.getInstance().getRating(ratedEvent.toString());
        }
        return events;

    }


    @Override
    public void goToDetailPage(EventEntity subEvent) {
        if (mapEvents.containsKey(subEvent.getParentId())){
            Flow.get(context).set(new AgendaTopicDetailScreen(mapEvents.get(subEvent.getParentId()), subEvent));
        }
    }
}
