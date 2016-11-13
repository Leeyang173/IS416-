package sg.edu.smu.livelabs.mobicom.net.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Aftershock PC on 27/7/2015.
 */
public class ChatMessages {
    public class Message {
        public long id;
        public String message;
        public long from;
        @SerializedName("in_time")
        public String messageTime;
    }

    public String status;
    @SerializedName("details")
    public List<Message> messages;
}
