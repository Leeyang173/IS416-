package sg.edu.smu.livelabs.mobicom.net.item;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by johnlee on 15/1/16.
 */
public class RecentWinnersItem {
    @SerializedName("user")
    public List<UserProfileItem> users;

}
