package sg.edu.smu.livelabs.mobicom.busEvents;

/**
 * Created by smu on 20/7/15.
 */
public class UnregisterBeaconEvent {
    public boolean toUnregister;

    public UnregisterBeaconEvent(boolean toUnregister) {
        this.toUnregister = toUnregister;
    }
}
