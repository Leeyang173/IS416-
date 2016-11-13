package sg.edu.smu.livelabs.mobicom.net.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import sg.edu.smu.livelabs.mobicom.net.item.EVAPromotionItem;

/**
 * Created by smu on 9/11/15.
 */
public class EVAPromotionResponse {
    public String status;
    @SerializedName("details")
    public List<EVAPromotionItem> promotions;

    public EVAPromotionResponse() {
    }

    public EVAPromotionResponse(String status, List<EVAPromotionItem> promotions) {
        this.status = status;
        this.promotions = promotions;
    }
}
