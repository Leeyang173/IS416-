package sg.edu.smu.livelabs.mobicom.net.item;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by smu on 27/10/15.
 */
public class MemoriesItem implements Serializable {
    public String id;
    @SerializedName("user_id")
    public String userId;
    @SerializedName("image_id")
    public String image;
    @SerializedName("caption")
    public String caption;
    @SerializedName("insert_time")
    public Date insertTime;
    public String status;
}
