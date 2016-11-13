package sg.edu.smu.livelabs.mobicom.net.item;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by smu on 27/10/15.
 */
public class FavoriteDetailsItem {
    public String my_voted_candidates;
    public String user_pool_type;
    public String comp_title;
    @SerializedName("user_pool")
    public List<FavoriteItem> itemList;

    public String[] getMyVotedCandidates(){
        return my_voted_candidates.split(",");
    }
}
