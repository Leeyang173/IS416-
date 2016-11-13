package sg.edu.smu.livelabs.mobicom.busEvents;

/**
 * Created by smu (Chau) on 24/6/16.
 */
public class DeleteTopicEvent {
    public boolean isSuccess;
    public DeleteTopicEvent(boolean isSuccess){
        this.isSuccess = isSuccess;
    }
}
