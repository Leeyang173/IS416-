package sg.edu.smu.livelabs.mobicom.net.api;

import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;
import rx.Observable;
import sg.edu.smu.livelabs.mobicom.net.response.InterestResponse;
import sg.edu.smu.livelabs.mobicom.net.response.ProfileResponse;
import sg.edu.smu.livelabs.mobicom.net.response.SimpleResponse;

/**
 * Created by smu on 22/1/16.
 */
public interface ProfileApi {

    @FormUrlEncoded
    @POST("get_user_details")
    Observable<ProfileResponse> getUserDetails(@Field("email_id") String email);

    @FormUrlEncoded
    @POST("update_profile")
    Observable<ProfileResponse> updateProfile(@Field("user_id") String userId, @Field("name") String name, @Field("avatar_id") String avatarId,
                                             @Field("interests") String interests,
                                              @Field("user_handle") String userHandle,
                                              @Field("session_token") String session_token);

    @FormUrlEncoded
    @POST("update_profile")
    Observable<ProfileResponse> updateAvatar(@Field("user_id") String userId,  @Field("avatar_id") String avatarId,
                                             @Field("user_handle") String userHandle,
                                             @Field("session_token") String session_token);

    @FormUrlEncoded
    @POST("update_profile")
    Observable<InterestResponse> updateInterest(@Field("user_id") String userId,  @Field("interests") String interest,
                                                @Field("user_handle") String userHandle,
                                                @Field("session_token") String session_token);

    @FormUrlEncoded
    @POST("update_profile")
    Observable<SimpleResponse> updateInterest22(@Field("user_id") String userId,  @Field("interests") String interest);

    @FormUrlEncoded
    @POST("get_interests_list")
    Observable<InterestResponse> getInterests(@Field("last_modified_time") String lastModifiedDate, @Field("user_id") String user_id);
}
