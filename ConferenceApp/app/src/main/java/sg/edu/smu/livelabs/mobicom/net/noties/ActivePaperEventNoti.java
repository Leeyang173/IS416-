package sg.edu.smu.livelabs.mobicom.net.noties;

import com.google.gson.annotations.SerializedName;

/**
 * Created by smu on 26/5/16.
 */
public class ActivePaperEventNoti {
    @SerializedName("event_id")
    public long eventID;
    @SerializedName("rating_quiz_status")
    public String ratingQuizStatus;
}
