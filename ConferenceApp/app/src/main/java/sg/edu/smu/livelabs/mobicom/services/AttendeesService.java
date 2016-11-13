package sg.edu.smu.livelabs.mobicom.services;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
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
import sg.edu.smu.livelabs.mobicom.busEvents.RecommendedUserEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.UpdatedAttendeesEvent;
import sg.edu.smu.livelabs.mobicom.models.User;
import sg.edu.smu.livelabs.mobicom.models.data.AttendeeEntity;
import sg.edu.smu.livelabs.mobicom.models.data.AttendeeEntityDao;
import sg.edu.smu.livelabs.mobicom.models.data.SearchKeyEntity;
import sg.edu.smu.livelabs.mobicom.models.data.SearchKeyEntityDao;
import sg.edu.smu.livelabs.mobicom.net.RestClient;
import sg.edu.smu.livelabs.mobicom.net.api.AttendeeApi;
import sg.edu.smu.livelabs.mobicom.net.response.AllAttendeesResponse;
import sg.edu.smu.livelabs.mobicom.net.response.AttendeeResponse;
import sg.edu.smu.livelabs.mobicom.net.response.SimpleResponse;

/**
 * Created by smu on 14/3/16.
 */
public class AttendeesService extends GeneralService {
    public static int RESULT_PER_PAGE = 30;
    private static final int MAX_RETRY = 3;
    private int syncAttendeeRetry = 0;

    private static final AttendeesService instance = new AttendeesService();
    public static TimeZone gmtTime = TimeZone.getTimeZone("GMT-4");
    public static final SimpleDateFormat simpleTimeFormat2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static AttendeesService getInstance(){return instance;}
    private AttendeeEntityDao attendeeEntityDao;
    private SearchKeyEntityDao searchKeyEntityDao;
    private AttendeeApi attendeeApi;
    public void init(Context context, Bus bus, AttendeeApi attendeeApi){
        this.context = context;
        this.bus = bus;
        this.attendeeApi = attendeeApi;
        attendeeEntityDao = DatabaseService.getInstance().getAttendeeEntityDao();
        searchKeyEntityDao = DatabaseService.getInstance().getSearchKeyEntityDao();
    }

    public AttendeeEntity createAttendee(AttendeeResponse attendeeResponse){
        AttendeeEntity entity = new AttendeeEntity();
        entity.setUID(attendeeResponse.UID);
        entity.setEmail(attendeeResponse.email);
        entity.setName(attendeeResponse.name);
        entity.setUserHandle(attendeeResponse.userHandle);
        entity.setFirstName(attendeeResponse.firstName);
        entity.setRole(attendeeResponse.role);
        entity.setDesignation(attendeeResponse.desig);
        entity.setDescription(attendeeResponse.description);
        entity.setAvatar(attendeeResponse.avatar);
        entity.setStatus(attendeeResponse.status);
        entity.setStar(false);
        entity.setInterests(attendeeResponse.interests);
        return entity;
    }

    public void insertAttendee(List<AttendeeEntity> attendees){
        attendeeEntityDao.insertInTx(attendees);
    }

    public LazyList<AttendeeEntity> getAllAttendees(){
//        long myUID = DatabaseService.getInstance().getMe().getUID();
        return attendeeEntityDao.queryBuilder()
                .where(AttendeeEntityDao.Properties.Status.eq(ChatService.ACTIVE))
                .orderAsc(AttendeeEntityDao.Properties.Name)
                .listLazy();
    }


    public AttendeeEntity getAttendeesByUID(Long UID) {
        return attendeeEntityDao
                .queryBuilder()
                .where(AttendeeEntityDao.Properties.UID.eq(UID))
                .build()
                .forCurrentThread()
                .unique();

    }

    public List<AttendeeEntity> getAttendeesByUIDs(List<Long> UID) {
        return attendeeEntityDao
                .queryBuilder()
                .where(AttendeeEntityDao.Properties.UID.in(UID))
                .build()
                .forCurrentThread().list();

    }

    public AttendeeEntity getAttendeesByUserHandle(String userHandle){
        List<AttendeeEntity>  result = attendeeEntityDao
                .queryBuilder()
                .where(AttendeeEntityDao.Properties.UserHandle.eq(userHandle))
                .build()
                .forCurrentThread()
                .list();
        if (result != null && result.size() > 0) return result.get(0);
        return null;
    }

    public void updateSearchKey(List<SearchKeyEntity> searchKeys){
        searchKeyEntityDao.updateInTx(searchKeys);
    }

    public void deleteSeachKey(List<String> keys){
        List<SearchKeyEntity> searchKeyEntities = searchKeyEntityDao
                .queryBuilder()
                .where(SearchKeyEntityDao.Properties.Key.in(keys))
                .list();
        searchKeyEntityDao.deleteInTx(searchKeyEntities);
    }

    public LazyList<AttendeeEntity> search(String key) { //, List<String> filters, int page){
        long myUID = DatabaseService.getInstance().getMe().getUID();
        String keyStr = "%"+key+"%";
        return attendeeEntityDao
                .queryBuilder()
                .where(
                        AttendeeEntityDao.Properties.Status.eq(ChatService.ACTIVE))
                .whereOr(AttendeeEntityDao.Properties.Name.like(keyStr),
                        AttendeeEntityDao.Properties.Role.like(keyStr),
                        AttendeeEntityDao.Properties.Description.like(keyStr),
                        AttendeeEntityDao.Properties.Interests.like(keyStr),
                        AttendeeEntityDao.Properties.Designation.like(keyStr)
                )
                .orderAsc(AttendeeEntityDao.Properties.Name)
                .listLazy();
    }

    public void syncAttendees(final boolean isFirst){
        final SharedPreferences sharedPreferences = context.getSharedPreferences(MainActivity.SHARE_PREFERENCES, Context.MODE_PRIVATE);
        String lastDate = sharedPreferences.getString(MainActivity.LAST_UPDATE_ATTENDEE, "");
        if (isFirst || "".equals(lastDate)){
            lastDate = RestClient.simpleDateFormat.format(new Date(0));
        }

        User me = DatabaseService.getInstance().getMe();
        attendeeApi.getAllAttendees(lastDate, me.getUID()+"")
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .subscribe(new Action1<AllAttendeesResponse>() {
                    @Override
                    public void call(AllAttendeesResponse allAttendeesResponse) {
                        if ("success".equals(allAttendeesResponse.status)){
                            if (allAttendeesResponse.details == null && allAttendeesResponse.details.isEmpty()) return;
                            if (isFirst){
                                attendeeEntityDao.deleteAll();
                                saveAll(allAttendeesResponse.details);
                            } else {
                                for (AttendeeResponse attendeeResponse: allAttendeesResponse.details) {
                                    saveOrUpdate(attendeeResponse);
                                }
                            }
//                            String nowStr = RestClient.simpleDateFormat.format(new Date());
                            simpleTimeFormat2.setTimeZone(gmtTime);
                            Calendar now = Calendar.getInstance(gmtTime);
                            now.add(Calendar.MINUTE, -20);
                            String nowStr = simpleTimeFormat2.format(now.getTime());//convert to GMT-7
                            sharedPreferences
                                    .edit()
                                    .putString(MainActivity.LAST_UPDATE_ATTENDEE, nowStr)
                                    .commit();
                            post(new UpdatedAttendeesEvent(true));
                        }
                        post(new UpdatedAttendeesEvent(false));
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.e(App.APP_TAG, "SyncAttendees ", throwable);
                        if (isFirst) {
                            if(syncAttendeeRetry < MAX_RETRY){
                                syncAttendeeRetry++;
                                syncAttendees(true);
                            }
                            else{
                                syncAttendeeRetry = 0;
                            }
                        }
                        else{
                            post(new UpdatedAttendeesEvent(false));
                        }

                    }
                });
    }

    private void saveOrUpdate(AttendeeResponse attendeeResponse) {
        AttendeeEntity entity = getAttendeesByUID(attendeeResponse.UID);
        if (entity == null){
            entity = createAttendee(attendeeResponse);
            attendeeEntityDao.insert(entity);
        } else {
            entity.setUID(attendeeResponse.UID);
            entity.setEmail(attendeeResponse.email);
            entity.setName(attendeeResponse.name);
            entity.setRole(attendeeResponse.role);
            entity.setDesignation(attendeeResponse.desig);
            entity.setDescription(attendeeResponse.description);
            entity.setAvatar(attendeeResponse.avatar);
            entity.setStatus(attendeeResponse.status);
            entity.setInterests(attendeeResponse.interests);
            attendeeEntityDao.update(entity);
        }
    }

    private void saveAll(List<AttendeeResponse> details) {
        List<AttendeeEntity> attendeeEntities = new ArrayList<>();
        for (AttendeeResponse attendeeResponse : details){
            attendeeEntities.add(createAttendee(attendeeResponse));
        }
        attendeeEntityDao.insertInTx(attendeeEntities);
    }

    public String exceptedUsers="";
    private int loop = 0;
    public void getRecommendedUser(){
        long userID = DatabaseService.getInstance().getMe().getUID();
        attendeeApi.getRecommendUsers(userID, exceptedUsers)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .subscribe(new Action1<SimpleResponse>() {
                    @Override
                    public void call(SimpleResponse simpleResponse) {
                        if ("success".equals(simpleResponse.status)){
                            if (simpleResponse.details == null || simpleResponse.details.isEmpty()){
                                exceptedUsers = "";
                                loop++;
                                if (loop < 2){
                                    getRecommendedUser();
                                }
                                else {
                                    loop = 0;
                                    post(new RecommendedUserEvent(null));
                                }
                            } else {
                                if (exceptedUsers.isEmpty()){
                                    exceptedUsers = simpleResponse.details;
                                } else {
                                    exceptedUsers = exceptedUsers +  ","+ simpleResponse.details;
                                }
                                String[] ids = simpleResponse.details.split(",");
                                if (ids != null && ids.length > 0){
                                    List<AttendeeEntity> attendeeEntities = attendeeEntityDao.queryBuilder()
                                            .where(AttendeeEntityDao.Properties.UID.in(ids))
                                            .list();
                                    post(new RecommendedUserEvent(attendeeEntities));
                                }
                            }
                        } else {
                            post(new RecommendedUserEvent(null));
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.d(App.APP_TAG, "getRecommendedUser", throwable);
                        post(new RecommendedUserEvent(null));
                    }
                });
    }
}
