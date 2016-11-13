package sg.edu.smu.livelabs.mobicom.net.response;

import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.List;

/**
 * Created by smu on 14/4/16.
 */
public class SessionPaperResponse {
    @SerializedName("event_id")
    public long eventId;
    public String title;
    public String description;
    @SerializedName("start_time")
    public Date startTime;
    @SerializedName("end_time")
    public Date endTime;
    public List<PaperResponse> papers;
}
