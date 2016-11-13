package sg.edu.smu.livelabs.mobicom.models;


/**
 * Created by johnlee on 22/2/16.
 */
public class TeamMember {

    private User user;
    private boolean isLeader;

    public TeamMember() {
    }

    public TeamMember(User user, boolean isLeader) {
        this.user = user;
        this.isLeader = isLeader;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public boolean isLeader() {
        return isLeader;
    }

    public void setIsLeader(boolean isLeader) {
        this.isLeader = isLeader;
    }
}
