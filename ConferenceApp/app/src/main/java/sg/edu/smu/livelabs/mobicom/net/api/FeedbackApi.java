package sg.edu.smu.livelabs.mobicom.net.api;

import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;
import rx.Observable;
import sg.edu.smu.livelabs.mobicom.net.response.FeedbackResponse;

/**
 * Created by smu on 26/5/16.
 */
public interface FeedbackApi {
    @FormUrlEncoded
    @POST("feedback")
    Observable<FeedbackResponse> sendFeedback(@Field("user_id") long userId,
                                              @Field("platform") String platform,
                                              @Field("type") String type,
                                              @Field("feedback") String feedback);
}
