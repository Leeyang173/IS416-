package sg.edu.smu.livelabs.mobicom.net;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by Aftershock PC on 28/7/2015.
 */
public class ChatMessageNoti implements Serializable {
    public long dbId;
    public long fromUser;
    @SerializedName("sender_name")
    public String name;
    @SerializedName("group_chat_id")
    public long groupId;
    public String content;
    public String timestamp;
    @SerializedName("msg_id")
    public  long serverId;
}
