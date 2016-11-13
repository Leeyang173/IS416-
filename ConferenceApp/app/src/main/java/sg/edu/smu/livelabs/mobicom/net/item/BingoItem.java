package sg.edu.smu.livelabs.mobicom.net.item;

import com.google.gson.annotations.SerializedName;

import org.joda.time.DateTime;

import java.util.Date;

/**
 * Created by john on 15/1/16.
 */
public class BingoItem {
    @SerializedName("location_id")
    public String id;
    public String status;
    @SerializedName("insert_time")
    public Date insertTime;
    @SerializedName("image_id")
    public String imageId;
    public String text;

    public BingoItem() {
    }
}
