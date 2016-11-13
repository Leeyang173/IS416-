package sg.edu.smu.livelabs.mobicom.net.noties;

import com.google.gson.annotations.SerializedName;

/**
 * Created by smu on 8/4/16.
 */
public class ScavengerNoti {
    @SerializedName("hunt_id")
    public long huntId;
    @SerializedName("hunt_group_id")
    public long huntGroupId;
    @SerializedName("hunt_message")
    public String huntMessage;
    @SerializedName("hunt_action")
    public String huntAction;
}
