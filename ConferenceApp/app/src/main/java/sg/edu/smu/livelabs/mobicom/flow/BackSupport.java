package sg.edu.smu.livelabs.mobicom.flow;

import android.view.View;

import flow.Flow;

/**
 * Created by Aftershock PC on 30/6/2015.
 */
public class BackSupport {

    public static boolean onBackPressed(View childView) {
        if (childView instanceof HandlesBack) {
            if (((HandlesBack) childView).onBackPressed()) {
                return true;
            }
        }
        return Flow.get(childView).goBack();
    }

    private BackSupport() {
    }
}
