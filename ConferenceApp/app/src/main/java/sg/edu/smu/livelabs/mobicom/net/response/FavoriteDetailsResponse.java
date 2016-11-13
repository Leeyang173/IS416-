package sg.edu.smu.livelabs.mobicom.net.response;

import com.google.gson.annotations.SerializedName;

import sg.edu.smu.livelabs.mobicom.net.item.FavoriteDetailsItem;

/**
 * Created by smu on 9/11/15.
 */
public class FavoriteDetailsResponse {
    public String status;
    @SerializedName("details")
    public FavoriteDetailsItem items;

}
