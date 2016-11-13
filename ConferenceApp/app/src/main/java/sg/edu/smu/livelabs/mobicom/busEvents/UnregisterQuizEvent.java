package sg.edu.smu.livelabs.mobicom.busEvents;

/**
 * Created by smu on 20/7/15.
 */
public class UnregisterQuizEvent {
    public boolean toUnregister;

    public UnregisterQuizEvent(boolean toUnregister) {
        this.toUnregister = toUnregister;
    }
}
