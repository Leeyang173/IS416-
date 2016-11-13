package sg.edu.smu.livelabs.mobicom.net.response;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

/**
 * Created by smu on 12/4/16.
 */
public class PaperResponse {
    @SerializedName("paper_id")
    public long serverID;
    @SerializedName("paper_title")
    public String title;
    public String authors;
    public String pdf;
    public String epub;
    @SerializedName("event_time")
    public Date eventTime;
}
