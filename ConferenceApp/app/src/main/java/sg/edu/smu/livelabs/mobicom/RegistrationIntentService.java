package sg.edu.smu.livelabs.mobicom;

import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import rx.functions.Action1;
import rx.schedulers.Schedulers;
import sg.edu.smu.livelabs.mobicom.net.response.SimpleResponse;
import sg.edu.smu.livelabs.mobicom.services.DatabaseService;

/**
 * Created by smu on 18/1/16.
 */
public class RegistrationIntentService extends IntentService {
    private static final String TAG = "RegIntentService";

    public RegistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            // In the (unlikely) event that multiple refresh operations occur simultaneously,
            // ensure that they are processed sequentially.
            synchronized (TAG) {
                registerGCM();
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to complete token refresh", e);
        }
    }

    private void registerGCM() throws Exception {
        //get Token from google server
        InstanceID instanceID = InstanceID.getInstance(getApplicationContext());
        String senderId = getString(R.string.gcm_defaultSenderId);
        String token = instanceID.getToken(senderId,
                GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
        Log.d(App.APP_TAG, "REGID: " + token);
        String userID = DatabaseService.getInstance().getMe().getUID()+"";
        //sent token to app server
        App app = (App) getApplication();
        app.getRestClient().getChatApi().registerGCM(token, userID, "101")//deviceId = address (old) cannot use for android 6
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<SimpleResponse>() {
                    @Override
                    public void call(SimpleResponse simpleResponse) {
                        if ("success".equals(simpleResponse.status)) {
                            Log.d(App.APP_TAG, "Register GCM OK.");
                        } else {
                            Log.d(App.APP_TAG, "Register GCM Failed: " + simpleResponse.error + " " + simpleResponse.message);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.e(App.APP_TAG, "Cannot register GCM", throwable);
                        Handler h = new Handler(Looper.getMainLooper());
                        h.post(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    registerGCM();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                });
    }
}
