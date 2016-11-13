package sg.edu.smu.livelabs.mobicom.net.response;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Aftershock PC on 1/8/2015.
 */
public class GroupDetailsResponse {
    public String status;

    @SerializedName("details")
    public GroupChatRoomInfo chatRoomInfo;
}
