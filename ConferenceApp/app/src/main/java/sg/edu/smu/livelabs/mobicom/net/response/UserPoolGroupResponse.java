package sg.edu.smu.livelabs.mobicom.net.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by smu on 1/6/16.
 */
public class UserPoolGroupResponse {
    public String status;
    public List<String> details;
    @SerializedName("can_send")
    public boolean canSend;
}
