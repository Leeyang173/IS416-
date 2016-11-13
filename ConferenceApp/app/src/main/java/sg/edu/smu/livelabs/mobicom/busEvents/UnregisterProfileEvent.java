package sg.edu.smu.livelabs.mobicom.busEvents;

/**
 * Created by smu on 20/7/15.
 */
public class UnregisterProfileEvent {
    public boolean toUnregister;

    public UnregisterProfileEvent(boolean toUnregister) {
        this.toUnregister = toUnregister;
    }
}
