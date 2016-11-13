package sg.edu.smu.livelabs.mobicom.services;

import android.content.Context;
import android.util.Log;

import java.text.SimpleDateFormat;

import rx.functions.Action1;
import rx.schedulers.Schedulers;
import sg.edu.smu.livelabs.mobicom.net.api.TrackingApi;
import sg.edu.smu.livelabs.mobicom.net.response.SimpleResponse;

/**
 * Created by smu on 28/2/16.
 */
public class TrackingService extends GeneralService {
    private static final TrackingService instance = new TrackingService();
    public static final SimpleDateFormat simpleDateFormatTracking = new SimpleDateFormat("yyyy-MM-dd");
    private TrackingApi trackingApi;
    public static TrackingService getInstance(){return instance;}

    public void init(Context context, TrackingApi trackingApi){
        this.context = context;
        this.trackingApi = trackingApi;
    }

    public void sendTracking(final String functionId, final String function, String post1, String post2, String post3,
                             String post4){
        if(post1.isEmpty())
            post1 = " ";
        if(post2.isEmpty())
            post2 = " ";
        if(post3.isEmpty())
            post3 = " ";
        if(post4.isEmpty())
            post4 = " ";

        trackingApi.sendTracking(Long.toString(DatabaseService.getInstance().getMe().getUID()), functionId, function, post1, post2,
                post3, post4, "android")
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<SimpleResponse>() {
                    @Override
                    public void call(SimpleResponse response) {

                        if(response.details.equals("success")){
                            System.out.println("tracking:"+response.details + " for " + functionId + " " + function);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.e("Mobisys: ", "cannot send tracking for functionId:" + functionId + " of function:"+function, throwable);
                    }
                });
    }

}
