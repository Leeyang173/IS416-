package sg.edu.smu.livelabs.mobicom.net.response;

import com.google.gson.annotations.SerializedName;

import sg.edu.smu.livelabs.mobicom.net.item.UserStatsItem;

/**
 * Created by smu on 15/1/16.
 */
public class UserStatsResponse {
    public String status;

    @SerializedName("details")
    public UserStatsItem userStatsItem;
}
