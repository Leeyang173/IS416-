package sg.edu.smu.livelabs.mobicom.net.item;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

/**
 * Created by smu on 27/10/15.
 */
public class FavoriteListItem {


    @SerializedName("comp_id")
    public String id;
    @SerializedName("title")
    public String name;
    @SerializedName("comp_image_url")
    public String image;
    @SerializedName("start_time")
    public Date startTime;
    @SerializedName("end_time")
    public Date endTime;
    @SerializedName("user_pool_type")
    public String userPoolType;
    public String status;
    @SerializedName("insert_time")
    public Date inserTime;
    @SerializedName("unique_voters_count")
    public String uniqueVotersCount;
    @SerializedName("last_modified_time")
    public Date lastUpdate;
}
