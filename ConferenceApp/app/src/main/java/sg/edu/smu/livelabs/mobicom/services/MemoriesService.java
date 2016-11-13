package sg.edu.smu.livelabs.mobicom.services;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.otto.Bus;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import rx.functions.Action1;
import rx.schedulers.Schedulers;
import sg.edu.smu.livelabs.mobicom.busEvents.MemoriesHomeEvent;
import sg.edu.smu.livelabs.mobicom.models.User;
import sg.edu.smu.livelabs.mobicom.net.api.MemoriesApi;
import sg.edu.smu.livelabs.mobicom.net.item.MemoriesItem;
import sg.edu.smu.livelabs.mobicom.net.response.MemoriesGetImagesResponse;
import sg.edu.smu.livelabs.mobicom.net.response.SimpleResponse;

/**
 * Created by smu on 27/10/15.
 */
public class MemoriesService extends GeneralService {
    public static final int TYPE_SELFIE_NEXT_COMING = 1;
    public static final int TYPE_SELFIE_PAST = 2;
    public static final int TYPE_SELFIE_CURRENT = 3;
    public static final int TYPE_LUCKY_DRAW = 4;
    public static final int SELFIE_PHOTOS_PER_PAGE = 20;

    private static final MemoriesService instance = new MemoriesService();
    private Context context;
    private Gson nullGson;
    private Gson notNullGson;
    private MemoriesApi memoriesApi;
    private SimpleDateFormat dateToStrServerFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private SimpleDateFormat dateToStrServerFormat2 = new SimpleDateFormat("yyyy-MM-dd");
    private SimpleDateFormat dateToStrFormat = new SimpleDateFormat("MMM dd");
    private SimpleDateFormat dateToStrFormat2 = new SimpleDateFormat("dd MMMM");
    private String userId;
    public Date currentSelectedDate;
    public Date startDate;

    private Date lastUpdateTime;

    public static MemoriesService getInstance() {
        return instance;
    }

    public void init(Context context, Bus bus, Gson gson, MemoriesApi memoriesApi) {
        this.context = context;
        this.bus = bus;
        this.notNullGson = gson;
        this.nullGson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd HH:mm:ss")
                .serializeNulls()
                .create();
        this.memoriesApi = memoriesApi;
        lastUpdateTime = new Date();
        setUserId();
    }

    public Date getCurrentSelectedDate() {
        return currentSelectedDate;
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

    public String getEventIds(List<MemoriesItem> items){
        String id ="";
        if(items.size() == 0){
            return "0";
        }

        int count = 0;
        for(MemoriesItem i: items) {
            if(count == items.size()-1){
                id += i.id;
            }
            else{
                id += i.id + ',';
            }

            count++;
        }

        return id;
    }

    /**
     *
     * @param time => last update time
     * @param isFirst
     */
    public void getMorePhoto(Date time, final boolean isFirst, String imagesId){
        try {
            memoriesApi.getImages(dateToStrServerFormat2.format(currentSelectedDate), userId, imagesId)
                    .subscribeOn(Schedulers.io())
                    .subscribe(new Action1<MemoriesGetImagesResponse>() {
                        @Override
                        public void call(MemoriesGetImagesResponse response) {
                            MemoriesHomeEvent event = new MemoriesHomeEvent();
                            event.isFirst = isFirst;
                            event.isNext = false;
                            event.canUplaod = false;
                            event.images = null;
                            if ("success".equals(response.status)) {
                                event.canUplaod = response.canUpload;
                                event.isNext = response.images.size() >= SELFIE_PHOTOS_PER_PAGE;
                                event.images = new ArrayList<MemoriesItem>();
                                for (MemoriesItem image : response.images) {
                                    if ("active".equals(image.status)) {
                                        event.images.add(image);
                                    }
                                }
                            } else {
                                Log.e("XXX: memories", "cannot get memories photos " + response.status);
                            }

                            post(event);
                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            Log.e("XXX: memories", "cannot get memories photos", throwable);
                            MemoriesHomeEvent event = new MemoriesHomeEvent();
                            event.isFirst = isFirst;
                            event.isNext = false;
                            event.canUplaod = false;
                            event.images = null;
                            post(event);
                        }
                    });

            lastUpdateTime = new Date();// update last time we call to server to now
        }
        catch(Throwable e){
            Log.d("AAA", "MemoriesService:getMorePohotos:" + e.toString());
        }
    }


    public void postPhoto(String imageId, String description) {
        memoriesApi.uploadImage(dateToStrServerFormat2.format(currentSelectedDate), userId, imageId, description)
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<SimpleResponse>() {
                    @Override
                    public void call(SimpleResponse response) {
                        post(response);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.e("XXX: memories", "cannot post photo", throwable);
                        SimpleResponse response = new SimpleResponse();
                        response.status = "fail";
                        post(response);
                    }
                });
    }


    public boolean checkIsMeAdmin(){
        boolean isMeAdmin = false;
        User me = DatabaseService.getInstance().getMe();
        if(me != null){
            for(String s: me.getRole()){
                if(s.trim().toLowerCase().equals("moderator")){
                    isMeAdmin = true;
                    break;
                }
            }
        }
        return isMeAdmin;
    }
}
