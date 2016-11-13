package sg.edu.smu.livelabs.mobicom.net.item;

import com.google.gson.annotations.SerializedName;

/**
 * Created by johnlee on 15/1/16.
 */
public class BadgeItem {
    public String id;
    @SerializedName("badge_name")
    public String badgeName;
    @SerializedName("badge_type")
    public String badgeType;
    public  String description;
    @SerializedName("game_id")
    public String gameId;
    @SerializedName("image_url")
    public String imageUrl;
    public String keyword;
    @SerializedName("star_count")
    public int starCount;
    @SerializedName("count_achieved")
    public int countAchieved;
    @SerializedName("last_modified_time")
    public String lastModifiedTime;
    @SerializedName("play_now")
    public String playNow;

    public BadgeItem() {
    }
}
