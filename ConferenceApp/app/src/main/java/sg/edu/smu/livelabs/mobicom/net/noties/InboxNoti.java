package sg.edu.smu.livelabs.mobicom.net.noties;

import com.google.gson.annotations.SerializedName;

/**
 * Created by smu on 2/6/16.
 */
public class InboxNoti {
    @SerializedName("notification_id")
    public long notiID;
    public String content;
}
