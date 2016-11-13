package sg.edu.smu.livelabs.mobicom.net.api;

import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;
import rx.Observable;
import sg.edu.smu.livelabs.mobicom.net.response.BeaconRatingResponse;
import sg.edu.smu.livelabs.mobicom.net.response.BeaconResponse;
import sg.edu.smu.livelabs.mobicom.net.response.BingoResponse;
import sg.edu.smu.livelabs.mobicom.net.response.SimpleResponse;

/**
 * Created by smu on 22/1/16.
 */
public interface BingoApi {

    @FormUrlEncoded
    @POST("bingo_game/get_upload_details")
    Observable<BingoResponse> getBingo(@Field("user_id") String userId);

    @FormUrlEncoded
    @POST("bingo_game/store_bingo_image_details")
    Observable<BingoResponse> updateBingo(@Field("user_id") String userId, @Field("image_id") String imageId, @Field("location_id") String locationId);

}
