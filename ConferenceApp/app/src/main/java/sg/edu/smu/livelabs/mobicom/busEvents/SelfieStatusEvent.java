package sg.edu.smu.livelabs.mobicom.busEvents;

/**
 * Created by smu on 9/11/15.
 */
public class SelfieStatusEvent {
    public final static int LIKE = 1;
    public final static int UNLIKE = 2;
    public final static int REPORT = 3;
    public final static int UNREPORT = 4;
    public boolean isTopPhoto;
    public boolean isSuccess;
    public int type;
    public String details;//if fail;
    public SelfieStatusEvent(boolean isSuccess, String details, int type, boolean isTopPhoto){
        this.isSuccess = isSuccess;
        this.details = details;
        this.type = type;
        this.isTopPhoto = isTopPhoto;
    }
}
