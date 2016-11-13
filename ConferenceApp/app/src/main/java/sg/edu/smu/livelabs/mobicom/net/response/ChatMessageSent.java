package sg.edu.smu.livelabs.mobicom.net.response;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Aftershock PC on 1/8/2015.
 */
public class ChatMessageSent {
    public String status;
    public String timestamp;
    @SerializedName("client_id")
    public long clientId;
    @SerializedName("msg_id")
    public long id;
    public String details;
    @SerializedName("is_warning")
    public boolean isWarning;

}
