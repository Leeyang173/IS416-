package sg.edu.smu.livelabs.mobicom.net.item;

import com.google.gson.annotations.SerializedName;

/**
 * Created by smu on 15/1/16.
 */
public class InterestItem {
    public Long interests_id;
    @SerializedName("interest")
    public String interest;
    @SerializedName("last_modified_time")
    public String lastModifiedTime;

    public InterestItem() {
    }
}
