package sg.edu.smu.livelabs.mobicom.net.item;

import com.google.gson.annotations.SerializedName;

/**
 * Created by smu on 15/1/16.
 */
public class BeaconRatingItem {
    public String id;
    @SerializedName("beacon_id")
    public String beaconId;
    @SerializedName("user_id")
    public String userId;
    public  String rating;
    @SerializedName("insert_time")
    public String insertTime;
    @SerializedName("last_modified_time")
    public String lastModifiedTime;

    public BeaconRatingItem() {
    }
}
