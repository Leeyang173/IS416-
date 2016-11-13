package sg.edu.smu.livelabs.mobicom.net.item;

import com.google.gson.annotations.SerializedName;

/**
 * Created by smu on 15/1/16.
 */
public class IceBreakerDetailItem {
    @SerializedName("friend_id")
    public String friendId;
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
    public String organisation;

    public IceBreakerDetailItem() {
    }
}
