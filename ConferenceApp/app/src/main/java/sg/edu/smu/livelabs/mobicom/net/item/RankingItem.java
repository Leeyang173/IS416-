package sg.edu.smu.livelabs.mobicom.net.item;

import com.google.gson.annotations.SerializedName;

/**
 * Created by johnlee on 15/1/16.
 */
public class RankingItem {
    @SerializedName("user_id")
    public String userId;
    public String count;
    public String name;
    public String desig;
    @SerializedName("email_id")
    public String emailId;
    @SerializedName("avatar_id")
    public String avatarId;
    public int rank;
    @SerializedName("first_name")
    public String firstName;

    public RankingItem() {
    }

}
