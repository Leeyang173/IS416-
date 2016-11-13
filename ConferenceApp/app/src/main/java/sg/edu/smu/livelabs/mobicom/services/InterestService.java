package sg.edu.smu.livelabs.mobicom.services;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.squareup.otto.Bus;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.greenrobot.dao.query.Query;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import sg.edu.smu.livelabs.mobicom.UIHelper;
import sg.edu.smu.livelabs.mobicom.busEvents.InterestUpdateEvent;
import sg.edu.smu.livelabs.mobicom.models.User;
import sg.edu.smu.livelabs.mobicom.models.data.InterestsEntity;
import sg.edu.smu.livelabs.mobicom.models.data.InterestsEntityDao;
import sg.edu.smu.livelabs.mobicom.net.api.ProfileApi;
import sg.edu.smu.livelabs.mobicom.net.item.InterestItem;
import sg.edu.smu.livelabs.mobicom.net.response.InterestResponse;

/**
 * Created by smu on 28/2/16.
 */
public class InterestService extends GeneralService {
    private static final InterestService instance = new InterestService();
    public static InterestService getInstance(){return instance;}
    private InterestsEntityDao interestsEntityDao;
    private ProfileApi profileApi;
    private Bus bus;

    public void init(Context context, Bus bus, ProfileApi profileApi){
        this.context = context;
        interestsEntityDao = DatabaseService.getInstance().getInterestsEntityDao();
        this.profileApi = profileApi;
        this.bus = bus;
    }

    public void updateInterestsList(List<InterestItem> interestItems){
        List<InterestsEntity> interestsEntities = new ArrayList<InterestsEntity>();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
        Long lastId = getLastId();
        try{
            for(InterestItem interestItem: interestItems){
//                InterestsEntity existingInterest = getInterest(interestItem.interest);
//                if(existingInterest == null){ //new interest
                    interestsEntities.add(new InterestsEntity(interestItem.interests_id, interestItem.interest,
                            df.parse(interestItem.lastModifiedTime)));
//                    lastId++;
//                }
            }
            interestsEntityDao.deleteAll();
            interestsEntityDao.insertOrReplaceInTx(interestsEntities);
        }
        catch (Exception e){

        }
    }

    public List<InterestsEntity> getAllInterest(){
        return interestsEntityDao.queryBuilder().list();
    }

    public InterestsEntity getInterest(){
        Query<InterestsEntity> query = interestsEntityDao.queryBuilder()
                .orderDesc(InterestsEntityDao.Properties.LastUpdated).limit(1).build();
        List<InterestsEntity> interestsEntities = query.list();
        if (interestsEntities.size() > 0) {
            return interestsEntities.get(0);
        }
        return null;
    }

    public InterestsEntity getInterest(String interest){
        Query<InterestsEntity> query = interestsEntityDao.queryBuilder()
                .where(InterestsEntityDao.Properties.Interest.eq(interest))
                .orderDesc(InterestsEntityDao.Properties.LastUpdated).limit(1).build();
        List<InterestsEntity> interestsEntities = query.list();
        if (interestsEntities.size() > 0) {
            return interestsEntities.get(0);
        }
        return null;
    }

    public Long getLastId(){
        Query<InterestsEntity> query = interestsEntityDao.queryBuilder()
                .orderDesc(InterestsEntityDao.Properties.Id).limit(1).build();
        List<InterestsEntity> interestsEntities = query.list();
        if (interestsEntities.size() > 0) {
            return interestsEntities.get(0).getId();
        }
        return 0l;
    }

    /**
     * To get latest interest from server
     */
    public void loadInterestAPI(){
        InterestsEntity interest = getInterest();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
        String lastTime = "2016-01-01";

        User me = DatabaseService.getInstance().getMe();
        final Handler mainHandler = new Handler(context.getMainLooper());
        profileApi.getInterests(lastTime, Long.toString(me.getUID()))
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<InterestResponse>() {
                    @Override
                    public void call(InterestResponse interestResponse) {
                        //gotten the new interest list, update to local db
                        if (interestResponse != null && interestResponse.details != null) {
                            updateInterestsList(interestResponse.details);
                        }

                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                    bus.post(new InterestUpdateEvent());


//                                UIHelper.getInstance().dismissProgressDialog();
                            }
                        });

                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.e("Mobisys: ", "cannot get interest", throwable);
                    }
                });
    }

    /**
     * Update the interest to the server
     * @param userId
     * @param interests
     */
    public void updateInterestAPI(long userId, String interests, String userHandle, String sessionToken){
        final Handler mainHandler = new Handler(context.getMainLooper());
        profileApi.updateInterest(Long.toString(userId), interests.trim(), userHandle, sessionToken)
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<InterestResponse>() {
                    @Override
                    public void call(InterestResponse response) {

                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                    bus.post(new InterestUpdateEvent());

                                UIHelper.getInstance().dismissProgressDialog();
                            }
                        });
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.e("Mobisys: ", "cannot update interest", throwable);
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                    UIHelper.getInstance().dismissProgressDialog();
                                    UIHelper.getInstance().showAlert(context, "Please try again later.");
                            }
                        });


                    }
                });
    }

}
