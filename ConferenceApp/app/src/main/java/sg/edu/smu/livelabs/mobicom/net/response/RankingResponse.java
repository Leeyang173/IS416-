package sg.edu.smu.livelabs.mobicom.net.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import sg.edu.smu.livelabs.mobicom.net.item.RankingItem;

/**
 * Created by smu on 15/1/16.
 */
public class RankingResponse {
    public String status;
    @SerializedName("user_details")
    public UserRanking userRanking;
    @SerializedName("details")
    public List<RankingItem> rankingItems;
    @SerializedName("asset_details")
    public List<RankingItem> assetRankingItems;

    public class UserRanking{
        @SerializedName("user_id")
        public String userId;
        public String count;
        public int rank;
    }
}
