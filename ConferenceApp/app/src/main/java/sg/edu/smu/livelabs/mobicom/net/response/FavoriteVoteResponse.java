package sg.edu.smu.livelabs.mobicom.net.response;

import com.google.gson.annotations.SerializedName;

/**
 * Created by smu on 9/11/15.
 */
public class FavoriteVoteResponse {
    public String status;
    @SerializedName("details")
    public Result items;


    public class Result{
        @SerializedName("candidate_id")
        public String id;
        @SerializedName("vote_count")
        public int voteCount;
    }
}
