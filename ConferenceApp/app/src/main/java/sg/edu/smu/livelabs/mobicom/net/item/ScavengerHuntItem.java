package sg.edu.smu.livelabs.mobicom.net.item;

import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.List;

/**
 * Created by smu on 15/1/16.
 */
public class ScavengerHuntItem {
    public String id; //hunt id
    public String title;
    @SerializedName("start_time")
    public Date startTime;
    @SerializedName("end_time")
    public Date endTime;
    public String description;
    public String icon;
    public String type;
    @SerializedName("user_required_count")
    public String userRequiredCount;
    @SerializedName("hint_image")
    public String hintImage;
    @SerializedName("qr_code")
    public String qrCode;
    @SerializedName("insert_time")
    public Date insertTime;
    @SerializedName("last_modified_time")
    public Date lastModifiedTime;
    @SerializedName("recent_winners")
    public List<RecentWinnersItem> recentWinners;
    @SerializedName("has_completed")
    public String hasCompleted;

    public ScavengerHuntItem() {
    }
}
