package sg.edu.smu.livelabs.mobicom.net.api;

import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;
import rx.Observable;
import sg.edu.smu.livelabs.mobicom.net.response.PollingGetDetailsResponse;
import sg.edu.smu.livelabs.mobicom.net.response.PollingResponse;
import sg.edu.smu.livelabs.mobicom.net.response.PollingSimpleResponse;
import sg.edu.smu.livelabs.mobicom.net.response.SimpleResponse;

/**
 * Created by smu on 22/1/16.
 */
public interface PollingApi {

    @FormUrlEncoded
    @POST("user_poll/start_poll")
    Observable<PollingResponse> startPolling(@Field("user_id") String userId, @Field("title") String title, @Field("type") String type );

    @FormUrlEncoded
    @POST("user_poll/stop_poll")
    Observable<PollingResponse> stopPolling(@Field("poll_id") String pollId, @Field("user_id") String userId);

    @FormUrlEncoded
    @POST("user_poll/submit_poll_answer")
    Observable<PollingSimpleResponse> submitPoll(@Field("user_id") String userId, @Field("poll_id") String pollId, @Field("answer") String answer);

    @FormUrlEncoded
    @POST("user_poll/get_poll_details")
    Observable<PollingGetDetailsResponse> getPollDetails(@Field("poll_id") String pollId, @Field("user_id") String userId);

    @FormUrlEncoded
    @POST("user_poll/get_poll_details")
    Observable<SimpleResponse> getPollDetails2(@Field("poll_id") String pollId, @Field("user_id") String userId);

    @FormUrlEncoded
    @POST("user_poll/get_all_polls")
    Observable<PollingGetDetailsResponse> getPollingList(@Field("user_id") String userId);
}
