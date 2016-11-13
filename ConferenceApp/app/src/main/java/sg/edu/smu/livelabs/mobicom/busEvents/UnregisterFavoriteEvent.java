package sg.edu.smu.livelabs.mobicom.busEvents;

/**
 * Created by smu on 20/7/15.
 */
public class UnregisterFavoriteEvent {
    public boolean toUnregister;

    public UnregisterFavoriteEvent(boolean toUnregister) {
        this.toUnregister = toUnregister;
    }
}
