package sg.edu.smu.livelabs.mobicom.net.api;

import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;
import rx.Observable;
import sg.edu.smu.livelabs.mobicom.net.response.ChatMessageSent;
import sg.edu.smu.livelabs.mobicom.net.response.ChatMessages;
import sg.edu.smu.livelabs.mobicom.net.response.ChatRoomSyncResponse;
import sg.edu.smu.livelabs.mobicom.net.response.GroupDetailsResponse;
import sg.edu.smu.livelabs.mobicom.net.response.LoginResponse;
import sg.edu.smu.livelabs.mobicom.net.response.SimpleResponse;
import sg.edu.smu.livelabs.mobicom.net.response.SimpleResponse2;

/**
 * Created by smu on 22/1/16.
 */
public interface ChatApi {

    @FormUrlEncoded
    @POST("login")
    Observable<LoginResponse> login(@Field("email_id") String username, @Field("password") String password);

    @FormUrlEncoded
    @POST("qr_login")
    Observable<LoginResponse> login2(@Field("qr_code") String qrCode, @Field("key") String key);

    @FormUrlEncoded
    @POST("update_profile")
    Observable<LoginResponse> updateProfile(@Field("user_id") String userID,
                                            @Field("user_handle") String userHandle,
                                            @Field("session_token") String session_token,
                                            @Field("name") String name,
                                            @Field("organisation") String school,
                                            @Field("avatar_id") String avatarId,
                                            @Field("interests") String interest);
    @FormUrlEncoded
    @POST("update_user_mac")
    Observable<SimpleResponse2> updateUserMac(@Field("user_id") long userID,
                                              @Field("mac") String mac,
                                              @Field("phone_type") String phoneType);

    @FormUrlEncoded
    @POST("notification/register")
    Observable<SimpleResponse> registerGCM(@Field("reg_id") String regId,   @Field("user_id") String userID, @Field("ostype") String osType);

    @FormUrlEncoded
    @POST("chat/get_chat_set")
    Observable<ChatRoomSyncResponse> syncChatRooms(@Field("user_id") long UID, @Field("last_updated") String lastUpdated);

    @FormUrlEncoded
    @POST("chat/get_group_details")
    Observable<GroupDetailsResponse> getChatRoomDetails(@Field("user_id") long UID, @Field("group_id") long groupId);

    @FormUrlEncoded
    @POST("chat/create_group")
    Observable<SimpleResponse> createChatGroup(@Field("user_id") long UID, @Field("title") String title, @Field("photo_id") String avatar, @Field("members") String members);

    @FormUrlEncoded
    @POST("chat/update_group_details")
    Observable<SimpleResponse2> updateChatGroup(@Field("user_id") long UID, @Field("group_id") String groupId, @Field("title") String title, @Field("photo_id") String avatar);

    @FormUrlEncoded
    @POST("chat/update_chat_users")
    Observable<SimpleResponse2> updateChatGroup(@Field("user_id") long UID, @Field("group_id") String groupId, @Field("user_ids") String members);

    @FormUrlEncoded
    @POST("chat/delete_group_chat")
    Observable<SimpleResponse> deleteGroupChat(@Field("user_id") long UID, @Field("group_id") String groupId);

    @FormUrlEncoded
    @POST("chat/exit_group")
    Observable<SimpleResponse> exitGroupChat(@Field("user_id") long UID, @Field("group_id") String groupId);

    @FormUrlEncoded
    @POST("chat/delete_single_chat")
    Observable<SimpleResponse> deleteSingleChat(@Field("user_id") long UID, @Field("friend_id") String friendId);

    @FormUrlEncoded
    @POST("chat/send")
    Observable<ChatMessageSent> sendChat(@Field("user_id") long UID, @Field("client_id") long clientId,  @Field("message") String message, @Field("to") long to, @Field("group_id") long groupId);

    @FormUrlEncoded
    @POST("chat/get_group_chat_messages")
    Observable<ChatMessages> getGroupChatMessages(@Field("user_id") long UID, @Field("group_id") long groupId, @Field("insert_time") String  time);

    @FormUrlEncoded
    @POST("chat/get_single_chat_messages")
    Observable<ChatMessages> getSingleChatMessages(@Field("user_id") long UID, @Field("to") long friendId, @Field("last_update") String time);

}
