package sg.edu.smu.livelabs.mobicom.net.item;

import com.google.gson.annotations.SerializedName;

/**
 * Created by smu on 15/1/16.
 */
public class StumpLeaderboardItem {
    @SerializedName("user_id")
    public String userId;
    @SerializedName("stump_id")
    public String stumpId;
    public String score;
    @SerializedName("avatar_id")
    public String avatar;
    public String name;

    public StumpLeaderboardItem() {
    }
}
