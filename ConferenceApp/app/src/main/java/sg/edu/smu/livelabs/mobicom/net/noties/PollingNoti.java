package sg.edu.smu.livelabs.mobicom.net.noties;

import com.google.gson.annotations.SerializedName;

/**
 * Created by smu on 8/4/16.
 */
public class PollingNoti {
    @SerializedName("poll_id")
    public long pollId;
    @SerializedName("poll_title")
    public String title;
    public String content;
}
