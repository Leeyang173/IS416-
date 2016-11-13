package sg.edu.smu.livelabs.mobicom.net.item;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

/**
 * Created by johnlee on 15/1/16.
 */
public class BannerItem {
    @SerializedName("banner_id")
    public String id;
    public String keyword;
    @SerializedName("target_id")
    public long targetId;
    @SerializedName("banner_image")
    public String bannerImage;
    @SerializedName("insert_time")
    public Date insertTime;
    @SerializedName("last_modified_time")
    public Date lastUpdate;
    public String status;

    public BannerItem() {
    }
}
