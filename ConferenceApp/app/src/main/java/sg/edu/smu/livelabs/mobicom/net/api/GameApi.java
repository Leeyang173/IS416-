package sg.edu.smu.livelabs.mobicom.net.api;

import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;
import rx.Observable;
import sg.edu.smu.livelabs.mobicom.net.response.BannerResponse;
import sg.edu.smu.livelabs.mobicom.net.response.GameResponse;
import sg.edu.smu.livelabs.mobicom.net.response.RankingResponse;

/**
 * Created by smu on 22/1/16.
 */
public interface GameApi {

    @FormUrlEncoded
    @POST("all_games_details/get_details")
    Observable<GameResponse> getGames(@Field("user_id") String userId, @Field("last_modified_time") String lastModifiedDate);

    @FormUrlEncoded
    @POST("web_api/get_ranking")
    Observable<RankingResponse> getRanking2(@Field("user_id") String userId);

    @FormUrlEncoded
    @POST("ranking/get_ranking")
    Observable<RankingResponse> getRanking(@Field("user_id") String userId);

    @FormUrlEncoded
    @POST("ranking/get_main_leaderboard") //for home page
    Observable<RankingResponse> getRankingHome(@Field("user_id") String userId);


    @FormUrlEncoded
    @POST("banners/get_banners_details")
    Observable<BannerResponse> getBanners(@Field("user_id") String userId, @Field("last_modified_time") String lastUpdate );
}
