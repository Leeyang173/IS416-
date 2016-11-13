package sg.edu.smu.livelabs.mobicom.net.item;

import com.google.gson.annotations.SerializedName;

/**
 * Created by johnlee on 15/1/16.
 */
public class BadgeRuleItem {
    public String id; //badge_rule_id
    @SerializedName("badge_id")
    public String badgeId;
    public String count;
    @SerializedName("completed_badge_id_to_unlock")
    public String completedBadgeIdToUnlock;

    public BadgeRuleItem() {
    }
}
