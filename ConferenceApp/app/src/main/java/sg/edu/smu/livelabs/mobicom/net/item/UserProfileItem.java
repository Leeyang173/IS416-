package sg.edu.smu.livelabs.mobicom.net.item;

import com.google.gson.annotations.SerializedName;

/**
 * Created by smu on 15/1/16.
 */
public class UserProfileItem {
    @SerializedName("user_id")
    public String user_id;
    public String name;

    @SerializedName("email_id")
    public String email;
    public  String role;
    public String desig;
    public String description;
    public String status;
    @SerializedName("avatar_id")
    public String avatar;
    public String password;
    public String interests;
    @SerializedName("last_modified_time")
    public String lastModifiedTime;

    public UserProfileItem() {
    }
}
