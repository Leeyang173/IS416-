package sg.edu.smu.livelabs.mobicom.busEvents;

/**
 * Created by smu (Chau) on 24/6/16.
 */
public class DeleteCommentEvent {
    public boolean isSuccess;
    public DeleteCommentEvent(boolean isSuccess){
        this.isSuccess = isSuccess;
    }
}
