package sg.edu.smu.livelabs.mobicom.busEvents;

/**
 * Created by smu on 20/7/15.
 */
public class UnregisterIceBreakerEvent {
    public boolean toUnregister;

    public UnregisterIceBreakerEvent(boolean toUnregister) {
        this.toUnregister = toUnregister;
    }
}
