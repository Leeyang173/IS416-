package sg.edu.smu.livelabs.mobicom.net.api;

import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.POST;
import rx.Observable;
import sg.edu.smu.livelabs.mobicom.net.response.BadgeResponse;
import sg.edu.smu.livelabs.mobicom.net.response.BadgeRuleResponse;
import sg.edu.smu.livelabs.mobicom.net.response.FormResponse;
import sg.edu.smu.livelabs.mobicom.net.response.GameResponse;
import sg.edu.smu.livelabs.mobicom.net.response.UserBadgeResponse;
import sg.edu.smu.livelabs.mobicom.net.response.UserStatsResponse;

/**
 * Created by smu on 22/1/16.
 */
public interface BadgeApi {

    @FormUrlEncoded
    @POST("form_reward_track/check_count")
    Observable<FormResponse> checkFormCount(@Field("user_id") String userId, @Field("form_type") String formType, @Field("form_id") String formId);

    @FormUrlEncoded
    @POST("badge/get_parent_badges")
    Observable<BadgeResponse> getBadges(@Field("user_id") String userId, @Field("type") String type);

    @GET("badge/get_child_badges_rules") //not used
    Observable<BadgeRuleResponse> getBadgesRules();

    @GET("badge/get_games_names")
    Observable<GameResponse> getGames(@Field("user_id") String userId);

    @FormUrlEncoded
    @POST("badge/store_user_badge") //not used
    Observable<UserBadgeResponse> updateUserBadge(@Field("user_id") String userId, @Field("badge_id") String badgeId, @Field("badge_child_id") String badgeChildId);

    @FormUrlEncoded
    @POST("badge/get_user_badges") //not used
    Observable<UserBadgeResponse> getUserBadge(@Field("user_id") String userId);

    @FormUrlEncoded
    @POST("badge/remove_user_badge") //not used
    Observable<UserBadgeResponse> removeUserBadge(@Field("user_id") String userId, @Field("badge_id") String badgeId, @Field("badge_child_id") String badgeChildId);


    @FormUrlEncoded
    @POST("user_stats") //not used
    Observable<UserStatsResponse> getUserStats(@Field("user_id") String userId);

}
