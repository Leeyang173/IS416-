package sg.edu.smu.livelabs.mobicom.net.response;

import com.google.gson.annotations.SerializedName;

/**
 * Created by smu on 19/11/15.
 */
public class SelfieStatusResponse {
    public String status;
    @SerializedName("like_count")
    public int likeCount;
}
