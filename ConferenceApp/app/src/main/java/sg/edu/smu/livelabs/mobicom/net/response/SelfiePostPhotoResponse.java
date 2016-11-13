package sg.edu.smu.livelabs.mobicom.net.response;

import com.google.gson.annotations.SerializedName;

/**
 * Created by smu on 9/11/15.
 */
public class SelfiePostPhotoResponse {
    public String status;
    @SerializedName("image_code")
    public String ImageCode;
    public String details;
}
