package sg.edu.smu.livelabs.mobicom.net.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import sg.edu.smu.livelabs.mobicom.net.item.FavoriteListItem;

/**
 * Created by smu on 9/11/15.
 */
public class FavoriteResponse {
    public String status;
    @SerializedName("details")
    public List<FavoriteListItem> items;

    public FavoriteResponse() {
    }

    public FavoriteResponse(String status, List<FavoriteListItem> items) {
        this.status = status;
        this.items = items;
    }
}
