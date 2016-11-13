package sg.edu.smu.livelabs.mobicom.busEvents;

/**
 * Created by smu (Chau) on 24/6/16.
 */
public class UpdateCommentEvent {
    public boolean isSuccess;

    public UpdateCommentEvent(boolean isSuccess){
        this.isSuccess = isSuccess;
    }
}
