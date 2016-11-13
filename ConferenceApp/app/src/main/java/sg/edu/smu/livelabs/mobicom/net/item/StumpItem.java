package sg.edu.smu.livelabs.mobicom.net.item;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

/**
 * Created by smu on 15/1/16.
 */
public class StumpItem {
    @SerializedName("stump_id")
    public String id;
    public String title;
    public String description;
    @SerializedName("show_answers")
    public String showAnswers;
    @SerializedName("show_leaderboard")
    public String showLeaderboard;
    @SerializedName("image_url")
    public String imageURL;
    @SerializedName("repeat_ques")
    public String repeatQues;
    public String status;
    @SerializedName("insert_time")
    public Date insertTime;

    public StumpItem() {
    }
}
