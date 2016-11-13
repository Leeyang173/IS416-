package sg.edu.smu.livelabs.mobicom.net.api;

import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;
import rx.Observable;
import sg.edu.smu.livelabs.mobicom.net.response.IceBreakerAddFriendResponse;
import sg.edu.smu.livelabs.mobicom.net.response.IceBreakerFriendDetailResponse;
import sg.edu.smu.livelabs.mobicom.net.response.IceBreakerGetFriendListResponse;
import sg.edu.smu.livelabs.mobicom.net.response.IceBreakerLeaderBoardResponse;

/**
 * Created by smu on 22/1/16.
 */
public interface IceBreakerApi {

    @FormUrlEncoded
    @POST("ice_breaker/get_user_details_from_qr_code")
    Observable<IceBreakerFriendDetailResponse> getUserFromQR(@Field("qr_code") String qrCode, @Field("user_id") String userId);

    @FormUrlEncoded
    @POST("ice_breaker/get_friend_list")
    Observable<IceBreakerGetFriendListResponse> getFriendList(@Field("user_id") String userId);

    @FormUrlEncoded
    @POST("ice_breaker/add_friend")
    Observable<IceBreakerAddFriendResponse> addFriend(@Field("user_id") String userId, @Field("friend_id") String friendId);

    @FormUrlEncoded
    @POST("ice_breaker/get_leaderboard")
    Observable<IceBreakerLeaderBoardResponse> getLeaderBoard(@Field("user_id") String userId, @Field("count") String count);
}
