package sg.edu.smu.livelabs.mobicom.net.noties;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

/**
 * Created by smu on 8/4/16.
 */
public class BEPSurveyNoti {
    public String status;
    public String from;
    public String title;
    public String content;
    @SerializedName("notification_id")
    public long id;
    public Date timestamp;
}
