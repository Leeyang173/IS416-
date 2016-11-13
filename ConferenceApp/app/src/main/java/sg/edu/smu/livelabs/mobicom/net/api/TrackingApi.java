package sg.edu.smu.livelabs.mobicom.net.api;

import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;
import rx.Observable;
import sg.edu.smu.livelabs.mobicom.net.response.SimpleResponse;

/**
 * Created by smu on 22/1/16.
 */
public interface TrackingApi {

    @FormUrlEncoded
    @POST("app_usage_tracking")
    Observable<SimpleResponse> sendTracking(@Field("user_id") String userId,
                                            @Field("function_id") String functionId,
                                            @Field("function") String function,
                                            @Field("post1") String post1,
                                            @Field("post2") String post2,
                                            @Field("post3") String post3,
                                            @Field("post4") String post4,
                                            @Field("device_type") String deviceType);
}
