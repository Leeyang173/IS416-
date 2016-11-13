package sg.edu.smu.livelabs.mobicom.net.item;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by smu on 15/1/16.
 */
public class PollingItem {
    @SerializedName("poll_id")
    public String pollId;
    @SerializedName("total_poll_count")
    public String totalPollCount;
    @SerializedName("poll_results")
    public List<PollingResultItem> pollingResultItemList;
    public String title;
    public String type;
    @SerializedName("has_submitted")
    public String hasSubmitted;
    @SerializedName("user_answer")
    public String userAnswer;

    public PollingItem() {
    }
}
