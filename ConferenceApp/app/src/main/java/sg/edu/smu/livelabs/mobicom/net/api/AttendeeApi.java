package sg.edu.smu.livelabs.mobicom.net.api;

import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;
import rx.Observable;
import sg.edu.smu.livelabs.mobicom.net.response.AllAttendeesResponse;
import sg.edu.smu.livelabs.mobicom.net.response.SimpleResponse;

/**
 * Created by smu on 4/4/16.
 */
public interface AttendeeApi {
    @FormUrlEncoded
    @POST("get_attendees_list")
    Observable<AllAttendeesResponse> getAllAttendees(@Field("last_modified_time") String lastModifiedTime,
                                                     @Field("user_id") String userId);

    @FormUrlEncoded
    @POST("user_recommend")
    Observable<SimpleResponse> getRecommendUsers(@Field("user_id") long userId,
                                                 @Field("user_list") String exceptedUser);

}
