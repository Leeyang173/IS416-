package sg.edu.smu.livelabs.mobicom.net.item;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

/**
 * Created by smu on 27/10/15.
 */
public class EVAPromotionItem {
    @SerializedName("comp_id")
    public String id;
    @SerializedName("comp_name")
    public String name;
    @SerializedName("image_url")
    public String image;
    @SerializedName("start_time")
    public Date startTime;
    @SerializedName("end_time")
    public Date endTime;
    @SerializedName("image_upload_count")
    public int imageCount;
    public String status;

    public String description;
    public String timeString;
    @SerializedName("type")
    public int promotionType;
    public String startStr;
}
