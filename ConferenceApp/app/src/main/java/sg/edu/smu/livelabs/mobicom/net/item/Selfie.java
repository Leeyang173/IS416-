package sg.edu.smu.livelabs.mobicom.net.item;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by smu on 2/11/15.
 */
public class Selfie implements Serializable {
    public String id;
    @SerializedName("comp_id")
    public String promotionId;
    @SerializedName("comp_name")
    public String promotionName;

    @SerializedName("user_id")
    public long userId;
    @SerializedName("name")
    public String username;
    @SerializedName("avatar_id")
    public String userAvatar;
    @SerializedName("email_id")
    public String email;

    public String description;
    public String status;
    @SerializedName("image_id")
    public String imageId;
    public String token;
    @SerializedName("insert_time")
    public Date createdTime;
    @SerializedName("last_update_time")
    public Date lastUpdated;
    @SerializedName("like_count")
    public int likes;
    @SerializedName("report_count")
    public int report;
    @SerializedName("like_status")
    public String likeStatus;
    @SerializedName("report_status")
    public String reportStatus;

    public String getLikesCount(){
        if (likes > 2){
            return likes + " likes";
        } else if (likes < 1){
            return "";
        } else {
            return likes + " like";
        }
    }

    public Selfie() {
    }

    public Selfie(String id) {
        this.id = id;
    }
}
