package sg.edu.smu.livelabs.mobicom.net.api;

import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;
import rx.Observable;
import sg.edu.smu.livelabs.mobicom.net.response.SimpleResponse;

/**
 * Created by smu on 22/1/16.
 */
public interface QuizApi {

    @FormUrlEncoded
    @POST("get_event_quiz")
    Observable<SimpleResponse> getQuiz(@Field("event_id") String eventId, @Field("user_id") String userId);

}
