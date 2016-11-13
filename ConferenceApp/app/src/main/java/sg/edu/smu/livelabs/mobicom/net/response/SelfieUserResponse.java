package sg.edu.smu.livelabs.mobicom.net.response;

import com.google.gson.annotations.SerializedName;

/**
 * Created by smu on 11/11/15.
 */
public class SelfieUserResponse {
    @SerializedName("user_id")
    public long id;
    public String name;
    public String email;
    @SerializedName("avatar_id")
    public String avatar;
    @SerializedName("image_count")
    public int imageCount;

    public String getImageCount(){
        if (imageCount > 2){
            return imageCount + " photos";
        } else if (imageCount == 1){
            return imageCount + " photo";
        } else {
            return "";
        }
    }
    public String getImageCount0(){
        if (imageCount > 2){
            return imageCount + " photos";
        } else {
            return imageCount + " photo";
        }
    }
}
