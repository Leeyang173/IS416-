package sg.edu.smu.livelabs.mobicom.net.response;

import com.google.gson.annotations.SerializedName;

/**
 * Created by smu on 7/5/16.
 */
public class EventGameResponse {
    public String keyword; //type of game
    @SerializedName("target_id")
    public long gameID;
    public String image;
}
