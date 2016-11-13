package sg.edu.smu.livelabs.mobicom.services;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.otto.Bus;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import rx.functions.Action1;
import rx.schedulers.Schedulers;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.busEvents.SelfieHomeEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.SelfieLikerEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.SelfieProfileEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.SelfieStatusEvent;
import sg.edu.smu.livelabs.mobicom.models.User;
import sg.edu.smu.livelabs.mobicom.net.RestClient;
import sg.edu.smu.livelabs.mobicom.net.api.EVAPromotionAPI;
import sg.edu.smu.livelabs.mobicom.net.api.ProfileApi;
import sg.edu.smu.livelabs.mobicom.net.item.EVAPromotionItem;
import sg.edu.smu.livelabs.mobicom.net.item.Selfie;
import sg.edu.smu.livelabs.mobicom.net.response.EVAPromotionResponse;
import sg.edu.smu.livelabs.mobicom.net.response.ProfileResponse;
import sg.edu.smu.livelabs.mobicom.net.response.SelfieLeaderboardResponse;
import sg.edu.smu.livelabs.mobicom.net.response.SelfieLikersResponse;
import sg.edu.smu.livelabs.mobicom.net.response.SelfiePhotosResponse;
import sg.edu.smu.livelabs.mobicom.net.response.SelfiePostPhotoResponse;
import sg.edu.smu.livelabs.mobicom.net.response.SelfieSearchResponse;
import sg.edu.smu.livelabs.mobicom.net.response.SelfieStatusResponse;
import sg.edu.smu.livelabs.mobicom.net.response.SimpleResponse;

/**
 * Created by smu on 27/10/15.
 */
public class EVAPromotionService extends GeneralService {
    public static final int TYPE_SELFIE_NEXT_COMING = 1;
    public static final int TYPE_SELFIE_PAST = 2;
    public static final int TYPE_SELFIE_CURRENT = 3;
    public static final int TYPE_LUCKY_DRAW = 4;
    public static final int SELFIE_PHOTOS_PER_PAGE = 20;

    private static final EVAPromotionService instance = new EVAPromotionService();
    private Context context;
    private Gson nullGson;
    private Gson notNullGson;
    private EVAPromotionAPI evaPromotionAPI;
    private ProfileApi profileApi;
    private SimpleDateFormat dateToStrServerFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private SimpleDateFormat dateToStrFormat = new SimpleDateFormat("MMM dd");
    private SimpleDateFormat dateToStrFormat2 = new SimpleDateFormat("dd MMMM");
    private HashMap<String, EVAPromotionItem> promotionHashMap;
    private String userId;
    public EVAPromotionItem currentPromotion;
    private ConnectivityManager cm;

    private Date lastUpdateTime;

    public static EVAPromotionService getInstance() {
        return instance;
    }

    public void init(Context context, Bus bus, Gson gson, EVAPromotionAPI evaPromotionAPI, ProfileApi profileApi) {
        this.context = context;
        this.bus = bus;
        this.notNullGson = gson;
        this.nullGson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd HH:mm:ss")
                .serializeNulls()
                .create();
        this.evaPromotionAPI = evaPromotionAPI;
        this.profileApi = profileApi;
        cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        lastUpdateTime = new Date();
        setUserId();
    }

    public void isAPINull(RestClient restClient){
        if(evaPromotionAPI == null){
            evaPromotionAPI = restClient.getSelfieAPI();
        }

        if(profileApi == null){
            profileApi = restClient.getProfileApi();
        }
    }

    public void setUserId(){
        userId = String.valueOf(DatabaseService.getInstance().getMe().getUID());
    }

    public Date getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(Date lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public void getEVAPromotions(){
        promotionHashMap = new HashMap<>();
        userId = String.valueOf(DatabaseService.getInstance().getMe().getUID());
            evaPromotionAPI.getPromotions(userId)
                    .subscribeOn(Schedulers.io())
                    .subscribe(new Action1<EVAPromotionResponse>() {
                        @Override
                        public void call(EVAPromotionResponse evaPromotionResponse) {
                            if ("success".equals(evaPromotionResponse.status)) {

//                                CoolfieCacheService.getInstance().updateCompCacheList(evaPromotionResponse.promotions);

                                List<EVAPromotionItem> promotions = new ArrayList<EVAPromotionItem>();
                                List<EVAPromotionItem> oldPromotions = evaPromotionResponse.promotions;
                                evaPromotionResponse.promotions = promotions;
                                if (oldPromotions != null) {
                                    for (EVAPromotionItem promotion : oldPromotions) {
                                        if (!"active".equals(promotion.status)) {
                                            continue;
                                        }
                                        promotionHashMap.put(promotion.id, promotion);
                                        promotions.add(promotion);
                                        if (promotion.timeString == null || promotion.timeString.isEmpty()) {
                                            promotion.timeString = String.format("From %s To %s",
                                                    dateToStrFormat.format(promotion.startTime),
                                                    dateToStrFormat.format(promotion.endTime));
                                        }
                                        promotion.startStr = dateToStrFormat2.format(promotion.startTime);
                                        Date now = new Date();
                                        long currentTime = now.getTime();
                                        long startTime = promotion.startTime.getTime();
                                        long endTime = promotion.endTime.getTime();
                                        if (startTime > currentTime) {
                                            promotion.promotionType = TYPE_SELFIE_NEXT_COMING;
                                            promotion.description = context.getResources().getString(R.string.coming_soon);

                                        } else {
                                            if (endTime < currentTime) {
                                                promotion.promotionType = TYPE_SELFIE_PAST;
                                            } else {
                                                promotion.promotionType = TYPE_SELFIE_CURRENT;
                                            }
                                            if (promotion.imageCount > 1) {
                                                promotion.description = String.format("%d photos", promotion.imageCount);
                                            } else if (promotion.imageCount == 1) {
                                                promotion.description = String.format("%d photo", promotion.imageCount);
                                            }
                                        }

                                    }
                                }
                            }
                            post(evaPromotionResponse);
                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            EVAPromotionResponse evaPromotionResponse = new EVAPromotionResponse();
                            evaPromotionResponse.status = "fail";
                            post(evaPromotionResponse);
                            Log.e("XXX: selfie", "cannot get promotions", throwable);
                        }
                    });
    }

    public void getLeaderboard(){
        try {
            evaPromotionAPI.getLeaderboard(userId, currentPromotion.id)
                    .subscribeOn(Schedulers.io())
                    .subscribe(new Action1<SelfieLeaderboardResponse>() {
                        @Override
                        public void call(SelfieLeaderboardResponse selfieLeaderboardResponse) {
//                            CoolfieCacheService.getInstance().updateCoolfieCacheList(selfieLeaderboardResponse.details.get(0).selfies, true);
                            post(selfieLeaderboardResponse);
                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            SelfieLeaderboardResponse selfieLeaderboardResponse =  new SelfieLeaderboardResponse();
                            selfieLeaderboardResponse.status = "fail";
                            post(selfieLeaderboardResponse);
                            Log.e("XXX: selfie", "cannot get leaderboard", throwable);
                        }
                    });
        }
        catch(Throwable e){
            Log.d("AAA", "EveaPromotionService:getLeaderboard:::"+e.toString());
        }
    }


    public void getMorePhoto(Date time, final boolean isFirst){
        try {
            evaPromotionAPI.getSelfies(userId, currentPromotion.id, dateToStrServerFormat.format(time))
                    .subscribeOn(Schedulers.io())
                    .subscribe(new Action1<SelfiePhotosResponse>() {
                        @Override
                        public void call(SelfiePhotosResponse selfiePhotosResponse) {
                            SelfieHomeEvent event = new SelfieHomeEvent();
                            event.isFirst = isFirst;
                            event.isNext = selfiePhotosResponse.selfies.size() >= SELFIE_PHOTOS_PER_PAGE;
                            event.selfies = null;
                            if ("success".equals(selfiePhotosResponse.status)) {
                                event.selfies = new ArrayList<Selfie>();
                                for (Selfie selfie : selfiePhotosResponse.selfies) {
                                    if ("active".equals(selfie.status)) {
                                        event.selfies.add(selfie);
                                    }
                                }
                                event.selfiesToRemove = selfiePhotosResponse.deletedSelfies;
                            }

                            post(event);
                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            Log.e("XXX: selfie", "cannot get photos", throwable);
                            SelfieHomeEvent event = new SelfieHomeEvent();
                            event.isFirst = isFirst;
                            event.isNext = false;
                            event.selfies = null;
                            post(event);
                        }
                    });

            lastUpdateTime = new Date();// update last time we call to server to now
        }
        catch(Throwable e){
            Log.d("AAA", "EveaPromotionService:getMorePhoto:::"+e.toString());
        }
    }

    public void getLikesPhotoDetail(Selfie selfie){
        EVAPromotionItem promotion = promotionHashMap.get(selfie.promotionId);
        if (promotion == null) promotion = currentPromotion;
        evaPromotionAPI.getLikesSelfieDetails(userId, selfie.id, promotion.id)
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<SelfieLikersResponse>() {
                    @Override
                    public void call(SelfieLikersResponse selfieLikerResponse) {
                        SelfieLikerEvent event = new SelfieLikerEvent();
                        if ("success".equals(selfieLikerResponse.status)){
                            event.users = selfieLikerResponse.details;
                        }
                        post(event);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.e("XXX: selfie", "cannot get getLikesPhotoDetail", throwable);
                        post(new SelfieLikerEvent());
                    }
                });
    }

    public void postPhoto(String imageId, String description) {
        evaPromotionAPI.postPhoto(userId, imageId, currentPromotion.id, description)
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<SelfiePostPhotoResponse>() {
                    @Override
                    public void call(SelfiePostPhotoResponse selfiePostPhotoResponse) {
                        post(selfiePostPhotoResponse);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.e("XXX: selfie", "cannot post photo", throwable);
                        SelfiePostPhotoResponse response = new SelfiePostPhotoResponse();
                        response.status = "fail";
                        post(response);
                    }
                });
    }

//    public void postCachedPhoto(final Long id, String imageId, String description) {
//        evaPromotionAPI.postPhoto(userId, imageId, currentPromotion.id, description)
//                .subscribeOn(Schedulers.io())
//                .subscribe(new Action1<SelfiePostPhotoResponse>() {
//                    @Override
//                    public void call(SelfiePostPhotoResponse selfiePostPhotoResponse) {
//                        CoolfieCacheService.getInstance().deleteCachePhoto(id);
//                    }
//                }, new Action1<Throwable>() {
//                    @Override
//                    public void call(Throwable throwable) {
//                        Log.e("XXX: selfie", "cannot post cached photo", throwable);
////                        SelfiePostPhotoResponse response = new SelfiePostPhotoResponse();
////                        response.status = "fail";
////                        post(response);
//                    }
//                });
//    }

    public void editPhoto(final Selfie selfie, final String description){
        evaPromotionAPI.editSelfie(userId, selfie.id, currentPromotion.id, description)
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<SimpleResponse>() {
                    @Override
                    public void call(SimpleResponse simpleResponse) {
                        if ("success".equals(simpleResponse.status)){
                            selfie.description = description;
                        }
                        post(simpleResponse);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.e("XXX: selfie", "You cannot edit photo", throwable);
                        SimpleResponse simpleResponse = new SimpleResponse();
                        simpleResponse.status = "fail";
                        post(simpleResponse);
                    }
                });
    }

    public void deletePhoto(String selfieId){
        evaPromotionAPI.deleteSelfie(userId, selfieId, currentPromotion.id)
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<SimpleResponse>() {
                    @Override
                    public void call(SimpleResponse simpleResponse) {
                        post(simpleResponse);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.e("XXX: selfie", "You cannot delete photo.", throwable);
                        SimpleResponse simpleResponse = new SimpleResponse();
                        simpleResponse.status = "fail";
                        post(simpleResponse);
                    }
                });
    }

    public void like(final Selfie selfie, final boolean isTopPhoto, final View v){
        EVAPromotionItem promotion = promotionHashMap.get(selfie.promotionId);
        if (promotion == null || promotion.endTime.getTime() < System.currentTimeMillis()){
            post(new SelfieStatusEvent(false, context.getResources().getString(R.string.like_disable), SelfieStatusEvent.LIKE, isTopPhoto));
            return;
        }
        String promotionId = selfie.promotionId;
        if (selfie.promotionId == null || selfie.promotionId.isEmpty()){
            promotionId = currentPromotion.id;
        }

        final Handler mainHandler = new Handler(context.getMainLooper());
        evaPromotionAPI.likeSelfie(userId, selfie.id, promotionId, "like")
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<SelfieStatusResponse>() {
                    @Override
                    public void call(SelfieStatusResponse response) {
                        if ("success".equals(response.status)){

                            selfie.likes = response.likeCount;
                            selfie.likeStatus="yes";
                            post(new SelfieStatusEvent(true, "", SelfieStatusEvent.LIKE, isTopPhoto));
                        }else{
                            post(new SelfieStatusEvent(false, context.getResources().getString(R.string.like_fail), SelfieStatusEvent.LIKE, isTopPhoto));
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        post(new SelfieStatusEvent(false, context.getResources().getString(R.string.like_fail), SelfieStatusEvent.LIKE, isTopPhoto));
                        Log.e("XXX: selfie", "You cannot vote photo", throwable);
                    }
                });
    }

    public void unlike(final Selfie selfie, final boolean isTopPhoto){//"unlike"
        EVAPromotionItem promotion = promotionHashMap.get(selfie.promotionId);
        if (promotion == null || promotion.endTime.getTime() < System.currentTimeMillis()){
            post(new SelfieStatusEvent(false, context.getResources().getString(R.string.like_disable), SelfieStatusEvent.LIKE, isTopPhoto));
            return;
        }
        String promotionId = selfie.promotionId;
        if (selfie.promotionId == null || selfie.promotionId.isEmpty()){
            promotionId = currentPromotion.id;
        }
        evaPromotionAPI.likeSelfie(userId, selfie.id, promotionId, "unlike")
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<SelfieStatusResponse>() {
                    @Override
                    public void call(SelfieStatusResponse response) {
                        if ("success".equals(response.status)){
                            selfie.likes = response.likeCount;
                            selfie.likeStatus="no";
                            post(new SelfieStatusEvent(true, "", SelfieStatusEvent.UNLIKE, isTopPhoto));
                        }else{
                            post(new SelfieStatusEvent(false, context.getResources().getString(R.string.like_fail), SelfieStatusEvent.UNLIKE, isTopPhoto));
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        post(new SelfieStatusEvent(false, context.getResources().getString(R.string.like_fail), SelfieStatusEvent.UNLIKE, isTopPhoto));
                        Log.e("XXX: selfie", "You can not vote photo.", throwable);
                    }
                });
    }

    public void report(final Selfie selfie, final boolean isTopPhoto){//"report"
        String promotionId = selfie.promotionId;
        if (selfie.promotionId == null || selfie.promotionId.isEmpty()){
            promotionId = currentPromotion.id;
        }
        evaPromotionAPI.likeSelfie(userId, selfie.id, promotionId, "report")
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<SelfieStatusResponse>() {
                    @Override
                    public void call(SelfieStatusResponse response) {
                        if ("success".equals(response.status)){
                            selfie.report++;
                            selfie.reportStatus = "yes";
                            selfie.likes = response.likeCount;
                            post(new SelfieStatusEvent(true, "", SelfieStatusEvent.REPORT, isTopPhoto));
                        }else{
                            post(new SelfieStatusEvent(false, "", SelfieStatusEvent.REPORT, isTopPhoto));
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        post(new SelfieStatusEvent(false, "", SelfieStatusEvent.REPORT, isTopPhoto));
                        Log.e("XXX: selfie", "You cannot report photo.", throwable);
                    }
                });
    }

    public void getUserPhotos(final User user, final int currentPage){

        evaPromotionAPI.getUserSelfie(userId, currentPromotion.id, String.valueOf(user.getUID()))
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<SelfiePhotosResponse>() {
                    @Override
                    public void call(SelfiePhotosResponse selfiePhotosResponse) {
                        if ("success".equals(selfiePhotosResponse.status)) {
                            List<Selfie> selfies = new ArrayList<Selfie>();
                            long id = DatabaseService.getInstance().getMe().getUID();
                            if (id == user.getUID()) {
                                for (Selfie selfie : selfiePhotosResponse.selfies) {
                                    if ("active".equals(selfie.status) || "inactive".equals(selfie.status)) {
                                        selfie.username = user.getName();
                                        selfie.email = user.getEmail();
                                        selfie.userAvatar = user.getAvatar();
                                        selfie.userId = user.getUID();
                                        selfies.add(selfie);
                                    }
                                }
                            } else {
                                for (Selfie selfie : selfiePhotosResponse.selfies) {
                                    if ("active".equals(selfie.status)) {
                                        selfie.username = user.getName();
                                        selfie.email = user.getEmail();
                                        selfie.userAvatar = user.getAvatar();
                                        selfie.userId = user.getUID();
                                        selfies.add(selfie);
                                    }
                                }
                            }

                            post(new SelfieProfileEvent(user, selfies.size(), selfies, false, currentPage));

                        } else {

                            post(new SelfieProfileEvent(user, 0, null, false, currentPage));
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        post(new SelfieProfileEvent(user, 0, null, false, currentPage));
                        Log.e("XXX: selfie", "You cannot get user photos", throwable);
                    }
                });

//        post(new SelfieProfileEvent(user, 0, null, false, currentPage));
    }

    public void search(String searchText){
        evaPromotionAPI.search(userId, currentPromotion.id, searchText)
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<SelfieSearchResponse>() {
                    @Override
                    public void call(SelfieSearchResponse selfieSearchResponse) {
                        post(selfieSearchResponse);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        SelfieSearchResponse selfieSearchReponse = new SelfieSearchResponse();
                        selfieSearchReponse.status = "fail";
                        post(selfieSearchReponse);
                        Log.e("XXX: selfie", "You cannot search user photos", throwable);
                    }
                });
    }

    public EVAPromotionItem getEVAPromotion(String promotionId) {
        return promotionHashMap.get(promotionId);
    }

//    public void selfieTracking(String function, String post2, String post3, String post4){
//        if (userId == null){
//            userId = String.valueOf(DatabaseService.getInstance().getMe().getUid());
//        }
//        evaPromotionAPI.tracking(userId, function, userId, post2, post3, post4)
//                .subscribeOn(Schedulers.io())
//                .subscribe(new Action1<SimpleResponse>() {
//                    @Override
//                    public void call(SimpleResponse response) {
//
//                    }
//                }, new Action1<Throwable>() {
//                    @Override
//                    public void call(Throwable throwable) {
//                        Log.e("Tracking error", throwable.toString());
//                    }
//                });
//    }

    public void setUserAvatar(String imgId){
        DatabaseService.getInstance().getMe().setAvatar(imgId);
        User me = DatabaseService.getInstance().getMe();
        profileApi.updateAvatar(Long.toString(me.getUID()), imgId, me.getUserHandle(), me.getSessionToken()) //TODO change user id
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<ProfileResponse>() {
                    @Override
                    public void call(ProfileResponse response) {
                    }
                });
    }
}
