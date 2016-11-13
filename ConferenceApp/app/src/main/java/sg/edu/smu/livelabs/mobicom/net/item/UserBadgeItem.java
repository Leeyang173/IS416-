package sg.edu.smu.livelabs.mobicom.net.item;

import com.google.gson.annotations.SerializedName;

/**
 * Created by johnlee on 15/1/16.
 */
public class UserBadgeItem {
    public String id;
    @SerializedName("user_id")
    public String userId;
    @SerializedName("badge_id")
    public String badgeId;
    @SerializedName("badge_child_id")
    public String badgeChildId;
    @SerializedName("insert_time")
    public String insertTime;

    public UserBadgeItem() {
    }
}
