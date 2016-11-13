package sg.edu.smu.livelabs.mobicom.net.api;

import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;
import rx.Observable;
import sg.edu.smu.livelabs.mobicom.net.response.ScavengerGroupResponse;
import sg.edu.smu.livelabs.mobicom.net.response.ScavengerHuntListResponse;
import sg.edu.smu.livelabs.mobicom.net.response.SimpleResponse;

/**
 * Created by smu on 22/1/16.
 */
public interface ScavengerApi {

    @FormUrlEncoded
    @POST("scav_hunt/get_scav_hunt_list")
    Observable<ScavengerHuntListResponse> getScavengerHuntList(@Field("last_modified_time") String lastModifiedDate, @Field("user_id") String userId);

    @FormUrlEncoded
    @POST("scav_hunt/add_group_member")
    Observable<ScavengerGroupResponse> addMemberToGroup(@Field("user_id") String userId,
                                                        @Field("hunt_id") String huntId, @Field("member_id") String memberId);

    @FormUrlEncoded
    @POST("scav_hunt/add_group_member")
    Observable<SimpleResponse> addMemberToGroup2(@Field("user_id") String userId,
                                                        @Field("hunt_id") String huntId, @Field("member_id") String memberId);

    @FormUrlEncoded
    @POST("scav_hunt/remove_user_from_group")
    Observable<ScavengerGroupResponse> removeMemberFromGroup(@Field("user_id") String userId, @Field("hunt_id") String huntId
            , @Field("member_id") String memberId, @Field("group_id") String groupId);

    @FormUrlEncoded
    @POST("scav_hunt/remove_user_from_group")
    Observable<SimpleResponse> removeMemberFromGroup2(@Field("user_id") String userId, @Field("hunt_id") String huntId
            , @Field("member_id") String memberId, @Field("group_id") String groupId);

    @FormUrlEncoded
    @POST("scav_hunt/delete_group")
    Observable<SimpleResponse> deleteGroup(@Field("user_id") String userId, @Field("hunt_id") String huntId
            ,  @Field("group_id") String groupId);

    @FormUrlEncoded
    @POST("scav_hunt/get_scav_hunt_members")
    Observable<ScavengerGroupResponse> getHuntMembers(@Field("user_id") String userId, @Field("hunt_id") String huntId);

    @FormUrlEncoded
    @POST("scav_hunt/submit_scav_result")
    Observable<ScavengerGroupResponse> submitHunt(@Field("user_id") String userId, @Field("hunt_id") String huntId
            ,  @Field("group_id") String groupId,  @Field("type") String type);

    @FormUrlEncoded
    @POST("scav_hunt/store_start_hunt_details")
    Observable<SimpleResponse> storeStartHunt(@Field("user_id") String userId, @Field("hunt_id") String huntId
            ,  @Field("group_id") String groupId);
}
