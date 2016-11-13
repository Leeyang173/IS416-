package sg.edu.smu.livelabs.mobicom.net.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import sg.edu.smu.livelabs.mobicom.net.item.BeaconRatingItem;

/**
 * Created by smu on 15/1/16.
 */
public class BeaconRatingResponse {
    public String status;

    @SerializedName("details")
    public List<BeaconRatingItem> beaconRatingItems;
}
