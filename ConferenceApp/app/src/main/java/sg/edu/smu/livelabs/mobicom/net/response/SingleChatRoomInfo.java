package sg.edu.smu.livelabs.mobicom.net.response;

import com.google.gson.annotations.SerializedName;

/**
 * Created by smu on 9/3/16.
 */
public class SingleChatRoomInfo {
    @SerializedName("user_id_chat")
    public long friendId;
    @SerializedName("last_updated")
    public String lastUpdated;
    public String status;
}
