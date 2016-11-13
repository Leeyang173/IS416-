package sg.edu.smu.livelabs.mobicom.net.response;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

/**
 * Created by smu on 6/6/16.
 */
public class MessageResponse {
    public long id;
    public String message;

    @SerializedName("insert_time")
    public Date time;
}
