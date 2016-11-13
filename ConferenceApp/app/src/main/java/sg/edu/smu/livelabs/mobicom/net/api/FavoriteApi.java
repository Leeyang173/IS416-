package sg.edu.smu.livelabs.mobicom.net.api;

import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;
import rx.Observable;
import sg.edu.smu.livelabs.mobicom.net.response.FavoriteDetailsResponse;
import sg.edu.smu.livelabs.mobicom.net.response.FavoriteLeaderboardResponse;
import sg.edu.smu.livelabs.mobicom.net.response.FavoriteResponse;
import sg.edu.smu.livelabs.mobicom.net.response.FavoriteVoteResponse;

/**
 * Created by smu on 22/1/16.
 */
public interface FavoriteApi {

    @FormUrlEncoded
    @POST("fav_comp/get_all_comps")
    Observable<FavoriteResponse> getFavorites(@Field("user_id") String userId);

    @FormUrlEncoded
    @POST("fav_comp/get_comp_details")
    Observable<FavoriteDetailsResponse> getFavoriteDetails(@Field("user_id") String userId, @Field("comp_id") String compId);
    @FormUrlEncoded
    @POST("fav_comp/vote_for_candidate")
    Observable<FavoriteVoteResponse> vote(@Field("user_id") String userId, @Field("comp_id") String compId,
                                          @Field("candidate_id") String candidateId);
    @FormUrlEncoded
    @POST("fav_comp/remove_vote_for_candidate")
    Observable<FavoriteVoteResponse> unvote(@Field("user_id") String userId, @Field("comp_id") String compId,
                                            @Field("candidate_id") String candidateId);

    @FormUrlEncoded
    @POST("fav_comp/get_leaderboard")
    Observable<FavoriteLeaderboardResponse> getLeaderboard(@Field("user_id") String userId, @Field("comp_id") String compId);


}
