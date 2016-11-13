package sg.edu.smu.livelabs.mobicom.net.response;

import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.List;

/**
 * Created by smu on 21/3/16.
 */
public class EventResponse {
    @SerializedName("event_id")
    public long eventId;
    @SerializedName("start_time")
    public Date startTime;
    @SerializedName("end_time")
    public Date endTime;
    public String title;
    public String description;
    @SerializedName("keynote_user")
    public String keynoteUser;
    public String location;
    @SerializedName("parent_id")
    public long parentID;
    public String status;
    @SerializedName("paper_id")
    public long paperId;
    @SerializedName("event_type")
    public String eventType;
    @SerializedName("topic_handle")
    public String topicHandle;
    @SerializedName("keynote_user_details")
    public String keynoteUserDetail;
    @SerializedName("rating_quiz_status")
    public String ratingQuizStatus;
    @SerializedName("rating_details")
    public EventRatingResponse rating;
    @SerializedName("game_details")
    public List<EventGameResponse> games;
    @SerializedName("my_like_status")
    public boolean myLikeStatus;
    @SerializedName("total_like_count")
    public int totalLikeCount;
}
