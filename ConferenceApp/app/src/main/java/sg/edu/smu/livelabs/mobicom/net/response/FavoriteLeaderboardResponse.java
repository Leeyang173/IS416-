package sg.edu.smu.livelabs.mobicom.net.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import sg.edu.smu.livelabs.mobicom.net.item.FavoriteItem;

/**
 * Created by smu on 9/11/15.
 */
public class FavoriteLeaderboardResponse {
    public String status;
    @SerializedName("details")
    public List<FavoriteItem> items;

}
