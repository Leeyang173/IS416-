package sg.edu.smu.livelabs.mobicom.net.item;

import com.google.gson.annotations.SerializedName;

/**
 * Created by smu on 15/1/16.
 */
public class IceBreakerLeaderBoardItem {
    @SerializedName("user_id")
    public String userId;
    public String name;
    @SerializedName("email_id")
    public String email;
    @SerializedName("avatar_id")
    public String avatar;
    public String desig;
    public String count;

    public IceBreakerLeaderBoardItem() {
    }
}
