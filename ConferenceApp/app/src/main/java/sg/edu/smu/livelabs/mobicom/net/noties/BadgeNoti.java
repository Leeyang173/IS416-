package sg.edu.smu.livelabs.mobicom.net.noties;

import com.google.gson.annotations.SerializedName;

/**
 * Created by smu on 8/4/16.
 */
public class BadgeNoti {
    @SerializedName("badge_key")
    public String badgeKey;
    @SerializedName("count_achieved")
    public int countAchieved;
    @SerializedName("parent_badge_id")
    public long badgeId;
    @SerializedName("badge_name")
    public String badgeName;
}
