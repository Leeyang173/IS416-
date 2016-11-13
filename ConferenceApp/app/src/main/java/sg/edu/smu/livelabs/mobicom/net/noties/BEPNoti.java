package sg.edu.smu.livelabs.mobicom.net.noties;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

/**
 * Created by smu on 8/4/16.
 */
public class BEPNoti {
    public String from;
    public String content;
    public String status;
    @SerializedName("notification_id")
    public int id;
    public Date timestamp;
}
