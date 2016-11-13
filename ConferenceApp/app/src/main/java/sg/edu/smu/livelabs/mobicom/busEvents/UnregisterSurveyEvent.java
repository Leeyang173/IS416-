package sg.edu.smu.livelabs.mobicom.busEvents;

/**
 * Created by smu on 20/7/15.
 */
public class UnregisterSurveyEvent {
    public boolean toUnregister;

    public UnregisterSurveyEvent(boolean toUnregister) {
        this.toUnregister = toUnregister;
    }
}
