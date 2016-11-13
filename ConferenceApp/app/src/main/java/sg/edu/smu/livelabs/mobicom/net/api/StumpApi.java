package sg.edu.smu.livelabs.mobicom.net.api;

import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;
import rx.Observable;
import sg.edu.smu.livelabs.mobicom.net.response.StumpQuestionResponse;
import sg.edu.smu.livelabs.mobicom.net.response.StumpResponse;
import sg.edu.smu.livelabs.mobicom.net.response.StumpSponsorResponse;
import sg.edu.smu.livelabs.mobicom.net.response.StumpStoreScoreResponse;

/**
 * Created by smu on 22/1/16.
 */
public interface StumpApi {

    @FormUrlEncoded
    @POST("stump/get_list")
    Observable<StumpResponse> getStumps(@Field("user_id") String userId);

    @FormUrlEncoded
    @POST("stump/get_ques")
    Observable<StumpQuestionResponse> getStumpQuestion(@Field("user_id") String userId, @Field("stump_id") String stumpId);

    @FormUrlEncoded
    @POST("stump/store_score")
    Observable<StumpStoreScoreResponse> storeScore(@Field("user_id") String userId, @Field("stump_id") String stumpId,
                                                 @Field("score") String score);

    @FormUrlEncoded
    @POST("stump/get_sponsors_list")
    Observable<StumpSponsorResponse> getSponsors(@Field("user_id") String userId);

}
