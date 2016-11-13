package sg.edu.smu.livelabs.mobicom.net.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by smu on 9/3/16.
 */
public class ChatRoomSyncResponse {
    public String status;
    public String message;

    @SerializedName("single_chat")
    public List<SingleChatRoomInfo> singleChats;

    @SerializedName("group_chat")
    public List<GroupChatRoomInfo> groupChats;
}
