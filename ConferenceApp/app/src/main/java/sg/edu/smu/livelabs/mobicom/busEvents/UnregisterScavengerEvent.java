package sg.edu.smu.livelabs.mobicom.busEvents;

/**
 * Created by smu on 20/7/15.
 */
public class UnregisterScavengerEvent {
    public boolean toUnregister;

    public UnregisterScavengerEvent(boolean toUnregister) {
        this.toUnregister = toUnregister;
    }
}
