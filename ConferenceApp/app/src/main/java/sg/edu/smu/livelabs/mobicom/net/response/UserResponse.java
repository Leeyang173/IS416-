package sg.edu.smu.livelabs.mobicom.net.response;

import com.google.gson.annotations.SerializedName;

/**
 * Created by smu on 14/3/16.
 */
public class UserResponse {
    @SerializedName("user_id")
    public long  uid;

    @SerializedName("email_id")
    public String email;

    @SerializedName("cover_id")
    public String cover;

    @SerializedName("avatar_id")
    public String avatar;
    public String name;
    public String role;
    public String status;

    @SerializedName("desig")
    public String designation;

    @SerializedName("organisation")
    public String school;

    public String interests;

    @SerializedName("qr_code")
    public String qrCode;

    @SerializedName("session_token")
    public String sessionToken;
    @SerializedName("user_handle")
    public String userHandle;
}
