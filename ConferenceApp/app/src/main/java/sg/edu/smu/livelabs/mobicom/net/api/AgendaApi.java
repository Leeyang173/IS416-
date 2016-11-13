package sg.edu.smu.livelabs.mobicom.net.api;

import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.POST;
import rx.Observable;
import sg.edu.smu.livelabs.mobicom.net.response.AllEventsResponse;
import sg.edu.smu.livelabs.mobicom.net.response.AllPaperResponse;
import sg.edu.smu.livelabs.mobicom.net.response.EventRatingResponse1;
import sg.edu.smu.livelabs.mobicom.net.response.EventRatingsResponse;
import sg.edu.smu.livelabs.mobicom.net.response.InboxNotificationResponse;
import sg.edu.smu.livelabs.mobicom.net.response.MyEventResponse;
import sg.edu.smu.livelabs.mobicom.net.response.SimpleResponse;
import sg.edu.smu.livelabs.mobicom.net.response.SimpleResponse2;
import sg.edu.smu.livelabs.mobicom.net.response.UserPoolGroupResponse;

/**
 * Created by smu on 28/2/16.
 */
public interface AgendaApi {
    @FormUrlEncoded
    @POST("get_all_events")
    Observable<AllEventsResponse> getAllEvents(@Field("user_id") long userID,
                                           @Field("last_modified_time") String lastModifiedTime);
    @FormUrlEncoded
    @POST("get_paper_list")
    Observable<AllPaperResponse> getEventPapers(@Field("user_id") long userID,
                                               @Field("last_modified_time") String lastModifiedTime);

    @FormUrlEncoded
    @POST("get_my_events")
    Observable<MyEventResponse> getMyEvents(@Field("user_id") long userID);

    @FormUrlEncoded
    @POST("mark_as_my_event/mark")
    Observable<MyEventResponse> AddToMyEvent(@Field("user_id") long userID,
                                           @Field("event_id") String eventIDs);

    @FormUrlEncoded
    @POST("mark_as_my_event/unmark")
    Observable<MyEventResponse> removeMyEvent(@Field("user_id") long userID,
                                           @Field("event_id") String eventIDs);
    @FormUrlEncoded
    @POST("event_rating/store_rating")
    Observable<EventRatingResponse1> rate(@Field("user_id") long userId,
                                         @Field("event_id") long eventId,
                                         @Field("rating") int rate);
    @FormUrlEncoded
    @POST("event_rating/get_events_rating")
    Observable<EventRatingsResponse> getRating(@Field("user_id") long userId,
                                          @Field("event_id") String eventIdList);

    @FormUrlEncoded
    @POST("comments/store_comment")
    Observable<SimpleResponse2> storeComment(@Field("user_id") long userId,
                                             @Field("topic_handle") String topicHandle ,
                                             @Field("comment_handle") String commentHandle,
                                             @Field("action") String action);
    @FormUrlEncoded
    @POST("comments/store_comment_like")
    Observable<SimpleResponse2> storeLikeComment(@Field("user_id") long userId,
                                             @Field("comment_handle") String commentHandle,
                                             @Field("action") String action);
    @FormUrlEncoded
    @POST("delete_forum_topic") //just for moderator
    Observable<SimpleResponse> deleteTopic(@Field("user_id") long userId,
                                                 @Field("topic_handle") String topicHandle);

    @GET("get_user_handle_for_main_topic")
    Observable<SimpleResponse> getUserHandleForMainTopic();

    @FormUrlEncoded
    @POST("notify_users/get_groups")
    Observable<UserPoolGroupResponse> getUserPoolGroup(@Field("user_id") long userId);

    @FormUrlEncoded
    @POST("notify_users")
    Observable<SimpleResponse2> notifyUsers(@Field("user_id") long userId,
                                           @Field("message") String msg ,
                                           @Field("groups") String groups
                                            );

    @FormUrlEncoded
    @POST("notify_users/get_general_notifications")
    Observable<InboxNotificationResponse> getInboxMsg(@Field("user_id") long userId,
                                                      @Field("last_modified_time") String lastModifiedTime);


}
