package sg.edu.smu.livelabs.mobicom.busEvents;

/**
 * Created by smu on 20/7/15.
 */
public class ScavengerUpdateDetailEvent {
    public String action;
    public long huntId;
    public long groupId;

    public ScavengerUpdateDetailEvent(String action, long huntId, long groupId) {
        this.action = action;
        this.huntId = huntId;
        this.groupId = groupId;
    }
}
