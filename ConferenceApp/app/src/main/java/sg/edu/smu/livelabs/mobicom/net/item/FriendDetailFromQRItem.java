package sg.edu.smu.livelabs.mobicom.net.item;

import com.google.gson.annotations.SerializedName;

/**
 * Created by smu on 15/1/16.
 */
public class FriendDetailFromQRItem {
    @SerializedName("user_id")
    public String userId;
    @SerializedName("name")
    public String name;
    @SerializedName("email_id")
    public String email;
    @SerializedName("avatar_id")
    public String avatar;
    @SerializedName("desig")
    public String desig;
    @SerializedName("qr_code")
    public String qrCode;
    public String interests;
    public String organisation;

    public FriendDetailFromQRItem() {
    }
}
