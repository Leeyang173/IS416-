package sg.edu.smu.livelabs.mobicom.net.response;

import com.google.gson.annotations.SerializedName;

/**
 * Created by smu on 25/5/16.
 */
public class EventMyRatingResponse {
    @SerializedName("my_rating")
    public int myRate;
    @SerializedName("event_id")
    public long eventId;
    @SerializedName("average_rating")
    public double rating;
    @SerializedName("percent_correct_answers")
    public double correctAnswers;
    @SerializedName("comments_count")
    public double commentsCount;
}
