package sg.edu.smu.livelabs.mobicom.net.api;

import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;
import rx.Observable;
import sg.edu.smu.livelabs.mobicom.net.response.BeaconRatingResponse;
import sg.edu.smu.livelabs.mobicom.net.response.BeaconResponse;
import sg.edu.smu.livelabs.mobicom.net.response.SimpleResponse;

/**
 * Created by smu on 22/1/16.
 */
public interface BeaconApi {

    @FormUrlEncoded
    @POST("beacons/get_details")
    Observable<BeaconResponse> getBeacons(@Field("last_modified_time") String lastModifiedTime, @Field("user_id") String userId);

    @FormUrlEncoded
    @POST("beacons/get_user_beacon_rating")
    Observable<BeaconRatingResponse> getUserBeaconRating(@Field("user_id") String userId, @Field("beacon_id") String beaconId);

    @FormUrlEncoded
    @POST("beacons/store_rating")
    Observable<SimpleResponse> updateUserBeaconRating(@Field("user_id") String userId, @Field("beacon_id") String beaconId, @Field("rating") String rating);

}
