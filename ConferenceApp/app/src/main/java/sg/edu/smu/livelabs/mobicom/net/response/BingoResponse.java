package sg.edu.smu.livelabs.mobicom.net.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import sg.edu.smu.livelabs.mobicom.net.item.BeaconItem;
import sg.edu.smu.livelabs.mobicom.net.item.BingoItem;

/**
 * Created by smu on 15/1/16.
 */
public class BingoResponse {
    public String status;

    @SerializedName("details")
    public List<BingoItem> bingoItems;
}
