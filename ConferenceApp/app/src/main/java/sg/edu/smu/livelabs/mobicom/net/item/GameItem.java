package sg.edu.smu.livelabs.mobicom.net.item;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

/**
 * Created by johnlee on 15/1/16.
 */
public class GameItem {
    @SerializedName("game_id")
    public String id;
    public String name;
    public String title;
    public String description;
    @SerializedName("image_url")
    public String imageId;
    public String status;
    @SerializedName("insert_time")
    public Date insertTime;
    @SerializedName("last_modified_time")
    public Date lastUdpate;
    public String keyword;

    public GameItem() {
    }
}
