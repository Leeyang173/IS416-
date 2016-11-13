package sg.edu.smu.livelabs.mobicom.busEvents;

/**
 * Created by smu on 20/7/15.
 */
public class UnregisterGameEvent {
    public boolean toUnregister;

    public UnregisterGameEvent(boolean toUnregister) {
        this.toUnregister = toUnregister;
    }
}
