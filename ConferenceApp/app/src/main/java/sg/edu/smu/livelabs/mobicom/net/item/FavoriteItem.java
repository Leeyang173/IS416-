package sg.edu.smu.livelabs.mobicom.net.item;

import com.google.gson.annotations.SerializedName;

/**
 * Created by smu on 27/10/15.
 */
public class FavoriteItem {
    @SerializedName("candidate_id")
    public String id;
    public String name;
    @SerializedName("avatar_id")
    public String avatar;
    @SerializedName("vote_count")
    public int count;
    @SerializedName("email_id")
    public String email;
    public boolean isLiked;
}
