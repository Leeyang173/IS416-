package sg.edu.smu.livelabs.mobicom.services;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import com.squareup.otto.Bus;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import de.greenrobot.dao.query.LazyList;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import sg.edu.smu.livelabs.mobicom.App;
import sg.edu.smu.livelabs.mobicom.MainActivity;
import sg.edu.smu.livelabs.mobicom.busEvents.AgendaLoadEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.AgendaPaperReloadEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.AgendaReloadingEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.AgendaUpdatedTopicEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.InboxEvent;
import sg.edu.smu.livelabs.mobicom.models.AgendaEvent;
import sg.edu.smu.livelabs.mobicom.models.User;
import sg.edu.smu.livelabs.mobicom.models.data.EventEntity;
import sg.edu.smu.livelabs.mobicom.models.data.EventEntityDao;
import sg.edu.smu.livelabs.mobicom.models.data.GameEventEntity;
import sg.edu.smu.livelabs.mobicom.models.data.GameEventEntityDao;
import sg.edu.smu.livelabs.mobicom.models.data.InboxEntity;
import sg.edu.smu.livelabs.mobicom.models.data.InboxEntityDao;
import sg.edu.smu.livelabs.mobicom.models.data.PaperEventEntity;
import sg.edu.smu.livelabs.mobicom.models.data.PaperEventEntityDao;
import sg.edu.smu.livelabs.mobicom.models.data.TopicEntity;
import sg.edu.smu.livelabs.mobicom.net.RestClient;
import sg.edu.smu.livelabs.mobicom.net.api.AgendaApi;
import sg.edu.smu.livelabs.mobicom.net.response.AllEventsResponse;
import sg.edu.smu.livelabs.mobicom.net.response.AllPaperResponse;
import sg.edu.smu.livelabs.mobicom.net.response.EventGameResponse;
import sg.edu.smu.livelabs.mobicom.net.response.EventMyRatingResponse;
import sg.edu.smu.livelabs.mobicom.net.response.EventRatingResponse1;
import sg.edu.smu.livelabs.mobicom.net.response.EventRatingsResponse;
import sg.edu.smu.livelabs.mobicom.net.response.EventResponse;
import sg.edu.smu.livelabs.mobicom.net.response.InboxNotificationResponse;
import sg.edu.smu.livelabs.mobicom.net.response.MessageResponse;
import sg.edu.smu.livelabs.mobicom.net.response.MyEventResponse;
import sg.edu.smu.livelabs.mobicom.net.response.PaperResponse;
import sg.edu.smu.livelabs.mobicom.net.response.SessionPaperResponse;
import sg.edu.smu.livelabs.mobicom.net.response.SimpleResponse;
import sg.edu.smu.livelabs.mobicom.net.response.SimpleResponse2;
import sg.edu.smu.livelabs.mobicom.net.response.UserPoolGroupResponse;
import sg.edu.smu.livelabs.mobicom.presenters.InboxPresenter;

/**
 * Created by smu on 28/2/16.
 */
public class AgendaService extends GeneralService {

    public static TimeZone gmtTime = TimeZone.getTimeZone("GMT-4");
    public static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM");
    public static final SimpleDateFormat simpleTimeFormat = new SimpleDateFormat("hh:mm a");

    private static final AgendaService instance = new AgendaService();

    public static AgendaService getInstance(){return instance;}

    //event status
    public static final String ACTIVE = "active";
    public static final String INACTIVE = "inactive";
    public static final String REMOVED = "removed";
    public static final String DELETED = "deleted";
    public static final String NONE = "none"; // disable all (rating and quiz)
    public static final String RATING = "rating"; //enable rating
    public static final String QUIZ = "quiz"; //enable quiz
    public static final String RATING_QUIZ = "rating_quiz"; //enable quiz & rating
    //event type
    public static final String BREAK = "break";
    public static final String SINGLE = "single";
    public static final String MULTIPLE = "multiple";
    public static final String KEYNOTE = "keynote";
    public static final String WORKSHOP_MULTIPLE = "workshop_multiple";
    public static final String WORKSHOP_SINGLE = "workshop_single";
    public static final String OTHER = "other";
    //inbox type
    public static final int INBOX = 0;
    public static final int OUTBOX = 1;

    private static final int MAX_RETRY = 3;
    private int syncEventRetry = 0;
    private int syncPaperRetry = 0;

    private AgendaApi agendaApi;
    private EventEntityDao eventEntityDao;
    private PaperEventEntityDao paperEventEntityDao;
    private GameEventEntityDao gameEventEntityDao;
    private InboxEntityDao inboxEntityDao;

    public boolean isReLoadingAgenda =false; //states on whether the agenda is been reloaded when user timezone changes
    private boolean isSameTimeZone = true;

    public void init(Context context, Bus bus, AgendaApi agendaApi){
        this.context = context;
        this.bus = bus;
        this.agendaApi = agendaApi;
        eventEntityDao = DatabaseService.getInstance().getEventEntityDao();
        paperEventEntityDao = DatabaseService.getInstance().getPaperEventEntityDao();
        gameEventEntityDao = DatabaseService.getInstance().getGameEventEntityDao();
        inboxEntityDao = DatabaseService.getInstance().getInboxEntityDao();
    }

    public EventEntity createEvent(EventResponse eventResponse){
        EventEntity eventEntity = new EventEntity();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(eventResponse.startTime);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.SECOND, 0);
        Date dateTime = new Date(calendar.getTimeInMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        eventEntity.setId(eventResponse.eventId);
        eventEntity.setServerId(eventResponse.eventId);
        eventEntity.setTitle(eventResponse.title);
        eventEntity.setDescription(eventResponse.description);
        eventEntity.setKeynoteUserId(eventResponse.keynoteUser);
        eventEntity.setRatingQuizStatus(eventResponse.ratingQuizStatus);
        eventEntity.setEventDate(calendar.getTimeInMillis());
        eventEntity.setStartTime(dateTime);
        eventEntity.setEndTime(eventResponse.endTime);
        eventEntity.setRoom(eventResponse.location);
        eventEntity.setParentId(eventResponse.parentID);
        eventEntity.setPaperId(eventResponse.paperId);
        eventEntity.setEventType(eventResponse.eventType);
        eventEntity.setStatus(eventResponse.status);
        eventEntity.setRating(eventResponse.rating.rating);
        eventEntity.setCorrectAnswers(eventResponse.rating.correctAnswers);
        eventEntity.setCommentCount(eventResponse.rating.commentsCount);
        eventEntity.setMyRate(0);
        eventEntity.setChecked(false);
        eventEntity.setSynced(true);
        eventEntity.setTopicHandle(eventResponse.topicHandle);
        eventEntity.setKeynoteUserDetail(eventResponse.keynoteUserDetail);
        eventEntity.setMyLike(eventResponse.myLikeStatus);
        eventEntity.setLikesCount(eventResponse.totalLikeCount);

        if (eventResponse.games != null){
            long eventID = eventResponse.eventId;
            List<GameEventEntity> oldGame = gameEventEntityDao.queryBuilder()
                    .where(GameEventEntityDao.Properties.EventServerID.eq(eventID))
                    .list();
            gameEventEntityDao.deleteInTx(oldGame);
            List<GameEventEntity> games = new ArrayList<>();
            for (EventGameResponse eventGame: eventResponse.games) {
                GameEventEntity game = new GameEventEntity();
                game.setGameId(eventGame.gameID);
                game.setEventServerID(eventID);
                game.setKeyWork(eventGame.keyword);
                game.setImage(eventGame.image);
                games.add(game);
            }
            gameEventEntityDao.insertInTx(games);
        }
        return eventEntity;
    }

    public void updateEvent(EventEntity eventEntity){
        eventEntityDao.update(eventEntity);
        post(new AgendaReloadingEvent());
    }

    public PaperEventEntity createPaperEvent(PaperResponse paperResponse, SessionPaperResponse sessionPaperResponse){
        PaperEventEntity entity = new PaperEventEntity();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(paperResponse.eventTime);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.SECOND, 0);
        entity.setEventTime(calendar.getTime());
        entity.setServerId(paperResponse.serverID);
        entity.setTitle(paperResponse.title);
        entity.setAuthors(paperResponse.authors);
        entity.setPdfLink(paperResponse.pdf);
        entity.setEpubLink(paperResponse.epub);
        entity.setSessionServerID(sessionPaperResponse.eventId);
        entity.setSessionTile(sessionPaperResponse.title);
        entity.setSessionDescription(sessionPaperResponse.description);
        return entity;
    }

    public List<PaperEventEntity> getAllPaper() {
        return paperEventEntityDao.queryBuilder()
                .orderAsc(PaperEventEntityDao.Properties.EventTime)
                .list();
    }

    public List<Date> getAllDate(){
        List<Date> dates = new ArrayList<>();
        String str = "SELECT DISTINCT " + EventEntityDao.Properties.EventDate.columnName
                + " FROM " + EventEntityDao.TABLENAME
                + " ORDER BY " + EventEntityDao.Properties.EventDate.columnName + " ASC; ";
        Cursor c = DatabaseService.getInstance()
                .getDaoSession().getDatabase()
                .rawQuery(str, null);
        try{
            if (c.moveToFirst()) {
                do {
                    dates.add(new Date(c.getLong(0)));
                } while (c.moveToNext());
            }
        } finally {
            c.close();
        }
        return dates;
    }

    public List<EventEntity> getEventByDate(Date date){
        return eventEntityDao.queryBuilder()
                .where(EventEntityDao.Properties.EventDate.eq(date.getTime()))
                .orderAsc(EventEntityDao.Properties.StartTime)
                .orderAsc(EventEntityDao.Properties.ParentId)
                .list();
    }

    public List<EventEntity> getParentEventByDate(Date date){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        return eventEntityDao.queryBuilder()
                .where(EventEntityDao.Properties.EventDate.eq(calendar.getTimeInMillis()),
                        EventEntityDao.Properties.ParentId.eq(0))
                .orderAsc(EventEntityDao.Properties.StartTime)
                .list();
    }

    public List<EventEntity> getMyEventByDate(Date date){
        return eventEntityDao.queryBuilder()
                .where(EventEntityDao.Properties.EventDate.eq(date.getTime()))
                .where(EventEntityDao.Properties.Checked.eq(true))
                .orderAsc(EventEntityDao.Properties.StartTime)
                .orderAsc(EventEntityDao.Properties.ParentId)
                .list();
    }

    public EventEntity getEventByServerId(long serverId){
        return eventEntityDao.queryBuilder()
                .where(EventEntityDao.Properties.ServerId.eq(serverId))
                .build()
                .unique();
    }

    public PaperEventEntity getPaperEventByServerId(Long paperId) {
        List<PaperEventEntity> papers = paperEventEntityDao.queryBuilder()
                .where(PaperEventEntityDao.Properties.ServerId.eq(paperId))
                .build()
                .list();
        if (papers != null && papers.size() > 0){
            return papers.get(0);
        }
        return null;
    }

    public void rateSubEvent(final EventEntity subEvent, final int rate){
        long UID = DatabaseService.getInstance().getMe().getUID();
        agendaApi.rate(UID, subEvent.getServerId(), rate)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .subscribe(new Action1<EventRatingResponse1>() {
                    @Override
                    public void call(EventRatingResponse1 eventRatingResponse) {
                        if ("success".equals(eventRatingResponse.status)) {
                            subEvent.setMyRate(rate);
                            subEvent.setRating(eventRatingResponse.details.rating);
                            subEvent.setCorrectAnswers(eventRatingResponse.details.correctAnswers);
                            subEvent.setCommentCount(eventRatingResponse.details.commentsCount);
                            eventEntityDao.update(subEvent);
                            AgendaUpdatedTopicEvent updatedTopic = new AgendaUpdatedTopicEvent();
                            updatedTopic.type = AgendaUpdatedTopicEvent.RATE_TOPIC;
                            post(updatedTopic);
                        } else {
                            subEvent.setMyRate(rate);
                            eventEntityDao.update(subEvent);
                            AgendaUpdatedTopicEvent updatedTopic = new AgendaUpdatedTopicEvent();
                            updatedTopic.type = AgendaUpdatedTopicEvent.RATE_TOPIC;
                            post(updatedTopic);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.d(App.appName, "rateSubEvent", throwable);
                        subEvent.setMyRate(rate);
                        eventEntityDao.update(subEvent);
                        AgendaUpdatedTopicEvent updatedTopic = new AgendaUpdatedTopicEvent();
                        updatedTopic.type = AgendaUpdatedTopicEvent.RATE_TOPIC;
                        post(updatedTopic);
                    }
                });

        TrackingService.getInstance().sendTracking("108", "agenda",
                Long.toString(subEvent.getServerId()), "rating", "", "");
    }

    public List<GameEventEntity> getGamesByEventID(long eventID){
        return gameEventEntityDao.queryBuilder()
                .where(GameEventEntityDao.Properties.EventServerID.eq(eventID))
                .list();
    }

    //add/remove all session
    public void updateMyAgenda(AgendaEvent event, boolean isAdded) {
        StringBuilder eventIds = new StringBuilder();
        List<EventEntity> eventEntities = new ArrayList<>();
        event.getEventEntity().setChecked(isAdded);
        event.getEventEntity().setMyLike(isAdded);
        if(isAdded) {
            event.getEventEntity().setLikesCount(event.getEventEntity().getLikesCount() + 1);
        }
        else{
            event.getEventEntity().setLikesCount(event.getEventEntity().getLikesCount() - 1);
        }
        event.getEventEntity().setSynced(false);
        eventIds.append(event.getServerId());
        eventEntities.add(event.getEventEntity());
        for (EventEntity eventEntity : event.getSubEvents()){
            eventEntity.setChecked(isAdded);
            eventEntity.setSynced(false);
            eventEntities.add(eventEntity);
            eventIds.append(",");
            eventIds.append(eventEntity.getServerId());
        }
        eventEntityDao.updateInTx(eventEntities);
        updateMyEventToServer(eventEntities, eventIds.toString(), isAdded);
    }

    private void updateSyncEventDAO(List<EventEntity> eventEntities){
        for (EventEntity eventEntity : eventEntities){
            eventEntity.setSynced(true);
        }
        eventEntityDao.updateInTx(eventEntities);
    }

    private void updateMyEventToServer(final List<EventEntity> eventEntities, String eventIds, boolean isAdded){
        long UID = DatabaseService.getInstance().getMe().getUID();
        final  SharedPreferences sharedPreferences = context
                .getSharedPreferences(MainActivity.SHARE_PREFERENCES, Context.MODE_PRIVATE);
        if (isAdded){
            agendaApi.AddToMyEvent(UID, eventIds.toString())
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.computation())
                    .subscribe(new Action1<MyEventResponse>() {
                        @Override
                        public void call(MyEventResponse myEventResponse) {
                            if ("success".equals(myEventResponse.status)) {
                                updateSyncEventDAO(eventEntities);
                                sharedPreferences.edit().putBoolean(MainActivity.SYNC_OFFLINE_MY_EVENT, false).commit();
                            } else {
                                sharedPreferences.edit().putBoolean(MainActivity.SYNC_OFFLINE_MY_EVENT, true).commit();
                            }
                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            SharedPreferences sharedPreferences = context.getSharedPreferences(MainActivity.SHARE_PREFERENCES, Context.MODE_PRIVATE);
                            sharedPreferences.edit().putBoolean(MainActivity.SYNC_OFFLINE_MY_EVENT, true).commit();
                            Log.e(App.APP_TAG, "updateMyAgenda", throwable);
                        }
                    });
        } else {
            agendaApi.removeMyEvent(UID, eventIds.toString())
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.computation())
                    .subscribe(new Action1<MyEventResponse>() {
                        @Override
                        public void call(MyEventResponse myEventResponse) {
                            if ("success".equals(myEventResponse.status)) {
                                updateSyncEventDAO(eventEntities);
                                sharedPreferences.edit().putBoolean(MainActivity.SYNC_OFFLINE_MY_EVENT, false).commit();
                            } else {
                                sharedPreferences.edit().putBoolean(MainActivity.SYNC_OFFLINE_MY_EVENT, true).commit();
                            }
                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            Log.e(App.APP_TAG, "updateMyAgenda", throwable);
                        }
                    });
        }
    }

    public void syncMyEventToServer(){
        List<EventEntity> unSyncEvents = eventEntityDao.queryBuilder()
                .where(EventEntityDao.Properties.Synced.eq(false))
                .list();
        if (unSyncEvents != null && !unSyncEvents.isEmpty()){
            List<EventEntity> addedEvents = new ArrayList<>();
            List<EventEntity> removedEvents = new ArrayList<>();
            StringBuilder addedEventIds = new StringBuilder();
            StringBuilder removedEventIds = new StringBuilder();
            for (EventEntity eventEntity: unSyncEvents) {
                if (eventEntity.getChecked()){
                    addedEvents.add(eventEntity);
                    addedEventIds.append(eventEntity.getServerId());
                    addedEventIds.append(",");
                } else {
                    removedEvents.add(eventEntity);
                    removedEventIds.append(eventEntity.getServerId());
                    removedEventIds.append(",");
                }
            }
            if (!addedEvents.isEmpty()){
                updateMyEventToServer(addedEvents,
                        addedEventIds.substring(0, addedEventIds.length() - 1),
                        true);
            }
            if (!removedEvents.isEmpty()){
                updateMyEventToServer(removedEvents,
                        removedEventIds.substring(0, removedEventIds.length() - 1),
                        false);
            }
        }
    }

    private void syncMyEventFromServer(){ // just for the first time
        agendaApi.getMyEvents(DatabaseService.getInstance().getMe().getUID())
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .subscribe(new Action1<MyEventResponse>() {
                    @Override
                    public void call(MyEventResponse myEventResponse) {
                        if ("success".equals(myEventResponse.status)){
                            if (myEventResponse.details != null && !myEventResponse.details.isEmpty()){
                                List<Long> myEventIds = new ArrayList<>();
                                for (EventResponse event:myEventResponse.details) {
                                    myEventIds.add(event.eventId);
                                }
                                List<EventEntity> myEvents =  eventEntityDao.queryBuilder()
                                        .where(EventEntityDao.Properties.ServerId.in(myEventIds))
                                        .list();
                                for (EventEntity myEvent : myEvents){
                                    myEvent.setChecked(true);
//                                    myEvent.setLikesCount();
                                }
                                eventEntityDao.updateInTx(myEvents);
                            }
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.e(App.APP_TAG, "syncMyEventFromServer", throwable);
                    }
                });
    }


    public void syncEvent(final boolean isFirst){
        long UID = DatabaseService.getInstance().getMe().getUID();
        final SharedPreferences sharedPreferences = context.getSharedPreferences(MainActivity.SHARE_PREFERENCES, Context.MODE_PRIVATE);
        final Handler mainHandler = new Handler(context.getMainLooper());
        String lastUpdateTime = sharedPreferences.getString(MainActivity.LAST_UPDATE_AGENDA, "");
        String lastUpdatedTimeZone = sharedPreferences.getString(MainActivity.TIMEZONE, "");

        if(!Calendar.getInstance().getTimeZone().getID().equals(lastUpdatedTimeZone)){ //user changed timezone, reload to ensure date are all correct
            lastUpdateTime = "";
            isSameTimeZone = false;
            isReLoadingAgenda = true;
        }
        else{
            isSameTimeZone = true;
        }

        if (isFirst || lastUpdateTime.equals("")){
            lastUpdateTime = RestClient.simpleDateFormat.format(new Date(0)); //no need to convert since it's the start of time
            Calendar c = Calendar.getInstance();
            c.setTime(new Date(0));

            sharedPreferences
                    .edit()
                    .putString(MainActivity.TIMEZONE, c.getTimeZone().getID())
                    .commit();
        }

        if (isFirst || !isSameTimeZone){
            lastUpdateTime = RestClient.simpleDateFormat.format(new Date(0)); //no need to convert since it's the start of time
        }

        final String nowString = RestClient.simpleDateFormat.format(new Date());//simpleTimeFormat2.format(Calendar.getInstance(gmtTime).getTime());//convert to GMT-4

        agendaApi.getAllEvents(UID, lastUpdateTime)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .subscribe(new Action1<AllEventsResponse>() {
                    @Override
                    public void call(AllEventsResponse allEventsResponse) {
                        if ("success".equals(allEventsResponse.status)) {
                            if (allEventsResponse.details == null && allEventsResponse.details.isEmpty())
                                return;
                            if (isFirst || !isSameTimeZone) {
                                eventEntityDao.deleteAll();
                                addAll(allEventsResponse.details);
                                syncMyEventFromServer();
                            } else {
                                for (EventResponse eventEntity : allEventsResponse.details) {
                                    addOrUpdate(eventEntity);
                                }
                                syncMyEventToServer();
                            }
                            sharedPreferences
                                    .edit()
                                    .putString(MainActivity.LAST_UPDATE_AGENDA, nowString)
                                    .commit();
                            post(new AgendaReloadingEvent());
                            //support offline

                        }
                        isReLoadingAgenda = false;
                        if(!isSameTimeZone) {

                            mainHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    bus.post(new AgendaLoadEvent());
                                }
                            });
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        isReLoadingAgenda = false;
                        Log.e(App.APP_TAG, "get all event", throwable);
                        if (isFirst) {
                            if(syncEventRetry < MAX_RETRY) {
                                syncEventRetry++;
                                syncEvent(true);
                            }
                            else{
                                syncEventRetry = 0;
                            }
                        }
                    }
                });
    }

    private void addOrUpdate(EventResponse eventResponse){
//        EventEntity eventEntity = getEventByServerId(eventResponse.eventId);
//        if (eventEntity == null){
            if (!ACTIVE.equals(eventResponse.status)){
                return;
            }
            EventEntity eventEntity = createEvent(eventResponse);
            eventEntityDao.insertOrReplace(eventEntity);
//        } else {
//            EventEntity updatedEventEntity = createEvent(eventResponse);
//            updatedEventEntity.setId(eventEntity.getId());
//            updatedEventEntity.setSynced(eventEntity.getSynced());
//            updatedEventEntity.setMyRate(eventEntity.getMyRate());
//            updatedEventEntity.setChecked(eventEntity.getChecked());
//            eventEntityDao.update(updatedEventEntity);
//        }
    }

    private void addAll(List<EventResponse> eventResponses){
        eventEntityDao.deleteAll();
        List<EventEntity> events = new ArrayList<EventEntity>();
        for (EventResponse eventResponse: eventResponses){
            if (!ACTIVE.equals(eventResponse.status)){
                continue;
            }
            EventEntity eventEntity = createEvent(eventResponse);
            if (eventEntity != null){
                events.add(eventEntity);
            }
        }
        eventEntityDao.insertInTx(events);
    }

    public void syncPaper(final boolean isFirst){
        long UID = DatabaseService.getInstance().getMe().getUID();
        final SharedPreferences sharedPreferences = context.getSharedPreferences(MainActivity.SHARE_PREFERENCES, Context.MODE_PRIVATE);
        String lastUpdateTime = sharedPreferences.getString(MainActivity.LAST_UPDATE_PAPERS, "");
        if (isFirst || lastUpdateTime.equals("")){
            lastUpdateTime = RestClient.simpleDateFormat.format(new Date(0));
        }
        agendaApi.getEventPapers(UID, lastUpdateTime)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .subscribe(new Action1<AllPaperResponse>() {
                    @Override
                    public void call(AllPaperResponse allPaperResponse) {
                        if ("success".equals(allPaperResponse.status)) {
                            if (allPaperResponse.details == null && allPaperResponse.details.isEmpty()) {
                                post(new AgendaPaperReloadEvent(false));
                                return;
                            }
                            if (isFirst) {
                                paperEventEntityDao.deleteAll();
                                addAllPapers(allPaperResponse.details);
                            } else {
                                for (SessionPaperResponse sessionPaperResponse : allPaperResponse.details) {
                                    addOrUpdateAPaper(sessionPaperResponse);
                                }
                            }
                            String nowString = RestClient.simpleDateFormat.format(new Date());
                            sharedPreferences
                                    .edit()
                                    .putString(MainActivity.LAST_UPDATE_PAPERS, nowString)
                                    .commit();
                            post(new AgendaPaperReloadEvent(true));
                        } else {
                            post(new AgendaPaperReloadEvent(false));
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.e(App.APP_TAG, "get all Event Papers", throwable);
                        post(new AgendaPaperReloadEvent(false));
                        if (isFirst) {
                            if(syncPaperRetry < MAX_RETRY) {
                                syncPaperRetry++;
                                syncPaper(true);
                            }
                            else{
                                syncPaperRetry = 0;
                            }
                        }
                    }
                });
    }

    private void addOrUpdateAPaper(SessionPaperResponse sessionPaperResponse){
        if (sessionPaperResponse.papers == null || sessionPaperResponse.papers.isEmpty()) return;
        for (PaperResponse paperResponse : sessionPaperResponse.papers){
            PaperEventEntity paper = getPaperEventByServerId(paperResponse.serverID);
            if (paper == null){
                paper = createPaperEvent(paperResponse, sessionPaperResponse);
                paperEventEntityDao.insert(paper);
            } else {
                PaperEventEntity updatedPaper = createPaperEvent(paperResponse, sessionPaperResponse);
                updatedPaper.setId(paper.getId());
                paperEventEntityDao.update(updatedPaper);
            }
        }

    }

    private void addAllPapers(List<SessionPaperResponse> allSessionPaperResponses){
        paperEventEntityDao.deleteAll();
        List<PaperEventEntity> papers = new ArrayList<>();
        for (SessionPaperResponse sessionPaperResponse : allSessionPaperResponses){
            if (sessionPaperResponse.papers != null && !sessionPaperResponse.papers.isEmpty()){
                for (PaperResponse paperResponse : sessionPaperResponse.papers){
                    PaperEventEntity paperEntity = createPaperEvent(paperResponse, sessionPaperResponse);
                    if (paperEntity != null){
                        papers.add(paperEntity);
                    }
                }
            }
        }
        paperEventEntityDao.insertInTx(papers);
    }

    public void getRating(String EventIdList){

        agendaApi.getRating(DatabaseService.getInstance().getMe().getUID(),
                EventIdList)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .subscribe(new Action1<EventRatingsResponse>() {
                    @Override
                    public void call(EventRatingsResponse eventRatingsResponse) {
                        if ("success".equals(eventRatingsResponse.status)) {
                            for (EventMyRatingResponse response : eventRatingsResponse.details) {
                                EventEntity eventEntity = getEventByServerId(response.eventId);
                                eventEntity.setRating(response.rating);
                                eventEntity.setMyRate(response.myRate);
                                eventEntity.setCorrectAnswers(response.correctAnswers);
                                eventEntity.setCommentCount(response.commentsCount);
                                eventEntityDao.update(eventEntity);
                            }
                            post(new AgendaReloadingEvent());
                        }

                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.d(App.APP_TAG, "Agenda getRating", throwable);
                    }
                });
    }

    public void getRating(final EventEntity eventEntity){
        agendaApi.getRating(DatabaseService.getInstance().getMe().getUID(), String.valueOf(eventEntity.getServerId()))
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .subscribe(new Action1<EventRatingsResponse>() {
                    @Override
                    public void call(EventRatingsResponse eventRatingsResponse) {
                        if ("success".equals(eventRatingsResponse.status)
                                && eventRatingsResponse.details != null && !eventRatingsResponse.details.isEmpty()){
                            EventMyRatingResponse response = eventRatingsResponse.details.get(0);
                            eventEntity.setRating(response.rating);
                            eventEntity.setCorrectAnswers(response.correctAnswers);
                            eventEntity.setMyRate(response.myRate);
                            eventEntity.setCommentCount(response.commentsCount);
                            eventEntityDao.update(eventEntity);
                            post(new AgendaUpdatedTopicEvent());
                        }

                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.d(App.APP_TAG, "Agenda getRating", throwable);
                    }
                });
    }

    public void storeComment(String topicHandle, String commentHandle, String action){
        agendaApi.storeComment(DatabaseService.getInstance().getMe().getUID(),
                topicHandle, commentHandle, action)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .subscribe();
    }

    public void storeLikeComment( String commentHandle, String action){
        agendaApi.storeLikeComment(DatabaseService.getInstance().getMe().getUID(),
                commentHandle, action)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .subscribe();
    }

    // moderator delete a topic
    public void deleteATopic(final TopicEntity entity){
        agendaApi.deleteTopic(DatabaseService.getInstance().getMe().getUID(), entity.getTopicHandle())
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .subscribe(new Action1<SimpleResponse>() {
                    @Override
                    public void call(SimpleResponse simpleResponse) {
                        if ("success".equals(simpleResponse.status)){
                            DatabaseService.getInstance().getTopicEntityDao().delete(entity);
                        }
                        post(simpleResponse);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        post(new SimpleResponse());
                        Log.d(App.APP_TAG, "moderator can't delete a topic", throwable);
                    }
                });
    }

    public void getMainTopic(){
        agendaApi.getUserHandleForMainTopic()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .subscribe(new Action1<SimpleResponse>() {
                    @Override
                    public void call(SimpleResponse simpleResponse) {
                        if ("success".equals(simpleResponse.status)){
                            ForumService.getInstance().getUserTopic(simpleResponse.details);
                        }

                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {

                        Log.d(App.APP_TAG, "moderator can't delete a topic", throwable);
                    }
                });
    }

//    inbox/outbox

    public LazyList<InboxEntity> getAllInbox() {
        return inboxEntityDao.queryBuilder()
                .where(InboxEntityDao.Properties.Type.eq(INBOX))
                .orderDesc(InboxEntityDao.Properties.DateTime)
                .listLazy();
    }

    public long getUnreadInbox(){
        return inboxEntityDao.queryBuilder()
                .where(InboxEntityDao.Properties.Type.eq(INBOX))
                .where(InboxEntityDao.Properties.Read.eq(false))
                .count();
    }

    public void markAllAsRead(){
        new AsyncTask<Void, Void, Void>(){

            @Override
            protected Void doInBackground(Void... params) {
                List<InboxEntity> inboxEntities = inboxEntityDao.queryBuilder()
                        .where(InboxEntityDao.Properties.Type.eq(INBOX))
                        .where(InboxEntityDao.Properties.Read.eq(false))
                        .list();;
                for (InboxEntity inboxEntity: inboxEntities){
                    inboxEntity.setRead(true);
                }
                inboxEntityDao.updateInTx(inboxEntities);
                post(new InboxEvent());
                return null;
            }
        }.execute();
    }

    public void updateInbox(InboxEntity inboxEntity){
        inboxEntityDao.update(inboxEntity);
    }

    public void saveInbox(String content, long notificationID, Date date){
        InboxEntity inboxEntity = new InboxEntity();
        if (date == null){
            inboxEntity.setDateTime(new Date());
        } else {
            inboxEntity.setDateTime(date);
        }
        inboxEntity.setNotificationID(notificationID);
        inboxEntity.setRead(false);
        if (content.length() > InboxPresenter.MAX_LENGHT_MSG ){
            inboxEntity.setTitle(content.substring(0, InboxPresenter.MAX_LENGHT_MSG) + "...");
            inboxEntity.setMessage(content);
        } else {
            inboxEntity.setTitle(content);
            inboxEntity.setMessage("");
        }
        inboxEntity.setType(AgendaService.INBOX);
        inboxEntityDao.insert(inboxEntity);
    }

    private void addInbox(MessageResponse messageResponse){
        long msg = inboxEntityDao.queryBuilder()
                .where(InboxEntityDao.Properties.NotificationID.eq(messageResponse.id))
                .count();
        if (msg <= 0) {
            saveInbox(messageResponse.message, messageResponse.id, messageResponse.time);
        }
    }

    public void saveOutbox(String message){
        InboxEntity outboxEntity = new InboxEntity();
        if (message.length() > InboxPresenter.MAX_LENGHT_MSG ){
            outboxEntity.setTitle(message.substring(0, InboxPresenter.MAX_LENGHT_MSG) + "...");
            outboxEntity.setMessage(message);
        } else {
            outboxEntity.setTitle(message);
            outboxEntity.setMessage("");
        }
        outboxEntity.setMessage(message);
        outboxEntity.setDateTime(new Date());
        outboxEntity.setType(AgendaService.OUTBOX);
        outboxEntity.setRead(true);
        inboxEntityDao.insert(outboxEntity);
    }

    public LazyList<InboxEntity> getAllOutbox() {
        return inboxEntityDao.queryBuilder()
                .where(InboxEntityDao.Properties.Type.eq(OUTBOX))
                .orderDesc(InboxEntityDao.Properties.DateTime)
                .listLazy();
    }

    public void getUserPoolGroup(){
        agendaApi.getUserPoolGroup(DatabaseService.getInstance().getMe().getUID())
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .subscribe(new Action1<UserPoolGroupResponse>() {
                    @Override
                    public void call(UserPoolGroupResponse userPoolGroupResponse) {
                        post(userPoolGroupResponse);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.d(App.APP_TAG, " can not getUserPoolGroup ", throwable);
                        UserPoolGroupResponse userPoolGroupResponse = new UserPoolGroupResponse();
                        userPoolGroupResponse.status  = "fail";
                        post(userPoolGroupResponse);
                    }
                });
    }

    public void sendNotifyUser(String msg, String groups){
        agendaApi.notifyUsers(DatabaseService.getInstance().getMe().getUID(),
                msg, groups)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .subscribe(new Action1<SimpleResponse2>() {
                    @Override
                    public void call(SimpleResponse2 simpleResponse2) {
                        post(simpleResponse2);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        SimpleResponse2 simpleResponse2 = new SimpleResponse2();
                        simpleResponse2.status = "success"; //server will take alot of time to response back, since it will always return success
                        post(simpleResponse2);
//                        Log.d(App.APP_TAG, " can not sendNotifyUser ", throwable);
                    }
                });
    }

    public void getInbox(){
        final SharedPreferences sharedPreferences = context.getSharedPreferences(MainActivity.SHARE_PREFERENCES, Context.MODE_PRIVATE);
        String lastUpdateTime = sharedPreferences.getString(MainActivity.SYNC_INBOX, "");
//        if (lastUpdateTime.isEmpty()){
//            lastUpdateTime = "2016-01-01";//RestClient.simpleDateFormat.format(new Date(0));
//        }
        lastUpdateTime = "2016-01-01";
        agendaApi.getInboxMsg(DatabaseService.getInstance().getMe().getUID(), lastUpdateTime)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .subscribe(new Action1<InboxNotificationResponse>() {
                    @Override
                    public void call(InboxNotificationResponse inboxNotificationResponse) {
                        if ("success".equals(inboxNotificationResponse.status)){
                            List<MessageResponse> messageResponses = inboxNotificationResponse.details;
                            if (messageResponses != null){
                                for (MessageResponse messageResponse : messageResponses){
                                    addInbox(messageResponse);
                                }
                                post(new InboxEvent());
                            }
                            sharedPreferences.edit()
                                    .putString(MainActivity.SYNC_INBOX, RestClient.simpleDateFormat.format(new Date()))
                                    .commit();
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.d(App.APP_TAG, " can not sendNotifyUser ", throwable);
                    }
                });
    }
}
