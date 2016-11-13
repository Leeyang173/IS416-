package sg.edu.smu.livelabs.mobicom.net.noties;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Aftershock PC on 4/8/2015.
 */
public class GroupMemberChangedNoti {
    @SerializedName("group_id")
    public long groupId;
    @SerializedName("user_id")
    public long userId;
    public String content;
    public String timestamp;
    @SerializedName("action_type")
    public String actionType;
}
