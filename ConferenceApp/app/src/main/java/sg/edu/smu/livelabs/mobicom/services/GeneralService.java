package sg.edu.smu.livelabs.mobicom.services;

import android.content.Context;
import android.os.Looper;

import com.squareup.otto.Bus;

import sg.edu.smu.livelabs.mobicom.App;

/**
 * Created by smu on 19/1/16.
 */
public class GeneralService {
    protected String tagName = App.APP_TAG;
    protected Context context;
    protected Bus bus;
    public void post(final Object o){
        android.os.Handler handler = new android.os.Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                bus.post(o);
            }
        });
    }
}
