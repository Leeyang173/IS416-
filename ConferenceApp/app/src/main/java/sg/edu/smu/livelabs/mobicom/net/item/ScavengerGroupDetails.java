package sg.edu.smu.livelabs.mobicom.net.item;

import com.google.gson.annotations.SerializedName;

/**
 * Created by smu on 15/1/16.
 */
public class ScavengerGroupDetails {
    @SerializedName("user_id")
    public String userId;
    @SerializedName("avatar_id")
    public String avatarId;
    public String name;
    @SerializedName("hunt_id")
    public String huntId;
    @SerializedName("group_id")
    public String groupId;
    @SerializedName("leader")
    public String isLeader;
    public String submitted;
    @SerializedName("hunt_start_status")
    public String isHuntStarted;
    public String error;

    public ScavengerGroupDetails() {
    }
}
