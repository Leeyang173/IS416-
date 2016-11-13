package sg.edu.smu.livelabs.mobicom;

import android.content.Context;

/**
 * Created by Aftershock PC on 30/6/2015.
 */
public class DaggerService {

    public static final String SERVICE_NAME = "sg.edu.smu.livelabs.mobicom.DaggerService";

    /**
     * Caller is required to know the type of the component for this context.
     */
    @SuppressWarnings("unchecked")
    public static <T> T getDaggerComponent(Context context) {
        //noinspection ResourceType
        return (T) context.getSystemService(SERVICE_NAME);
    }
}
