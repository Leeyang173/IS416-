package sg.edu.smu.livelabs.mobicom.net.item;

import com.google.gson.annotations.SerializedName;

/**
 * Created by smu on 15/1/16.
 */
public class BeaconItem {
    public String id;
    public String uuid;
    public String major;
    public  String minor;
    public String url;
    @SerializedName("paper_name")
    public String paperName;
    @SerializedName("last_modified_time")
    public String lastModifiedTime;
    @SerializedName("cap_char")
    public int capChar;
    @SerializedName("pdf_url")
    public String pdf;
    @SerializedName("user_rating")
    public int userRating;
    @SerializedName("average_rating")
    public double avgRating;

    public BeaconItem() {
    }
}
