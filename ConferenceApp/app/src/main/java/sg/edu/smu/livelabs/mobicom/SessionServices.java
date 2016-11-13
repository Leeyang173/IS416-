package sg.edu.smu.livelabs.mobicom;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.widget.Toast;

import sg.edu.smu.livelabs.mobicom.models.User;
import sg.edu.smu.livelabs.mobicom.services.ChatService;
import sg.edu.smu.livelabs.mobicom.services.DatabaseService;

/**
 * Created by johnlee on 18/1/16.
 */
public class SessionServices extends Service {
    //this service helps the app to have a background service that login user for every 1hr to refresh the session token in server
    private Handler handler;
    private Runnable runnable;
    private static final int ONE_HOUR = 3600000;
    private static final int FIVE_MIN = 300000;
    private User me;

    // Binder given to clients
    private final IBinder binder = new LocalBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    // Class used for the client Binder.
    public class LocalBinder extends Binder {
        public SessionServices getService() {
            // Return this instance of MyService so clients can call public methods
            return SessionServices.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
//        Toast.makeText(this, "Service created." + App.getInstance().callingLoginFromOnResume, Toast.LENGTH_SHORT).show();

        handler = new Handler();
        runnable = new Runnable() {
            public void run() {
//                Toast.makeText(SessionServices.this, "Service is still running"  + App.getInstance().callingLoginFromOnResume, Toast.LENGTH_LONG).show();
                if(App.getInstance().callingLoginFromOnResume){
                    handler.postDelayed(runnable, 30000); //call it 30sec later after the login from onresume settle
                }
                else {
                    me = DatabaseService.getInstance().getMe();
                    ChatService.getInstance().login2(me.getQrCode(), false, false);
                    handler.postDelayed(runnable, App.getInstance().loginFail == false ? ONE_HOUR : FIVE_MIN);
                }
            }
        };

        handler.postDelayed(runnable, 1000); //for initial start up when app destoryed
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        Toast.makeText(this, "Service destoryed by user.", Toast.LENGTH_LONG).show();
        handler.removeCallbacks(runnable);
    }
}
