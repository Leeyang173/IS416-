package sg.edu.smu.livelabs.mobicom.busEvents;

/**
 * Created by smu on 20/7/15.
 */
public class UpdateNetworkEvent {
    public boolean isConnected;

    public UpdateNetworkEvent(boolean isConnected) {
        this.isConnected = isConnected;
    }
}
