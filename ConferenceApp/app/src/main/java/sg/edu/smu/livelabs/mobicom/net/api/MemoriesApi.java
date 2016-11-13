package sg.edu.smu.livelabs.mobicom.net.api;

import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;
import rx.Observable;
import sg.edu.smu.livelabs.mobicom.net.response.MemoriesGetImagesResponse;
import sg.edu.smu.livelabs.mobicom.net.response.SimpleResponse;

/**
 * Created by smu on 22/1/16.
 */
public interface MemoriesApi {

    @FormUrlEncoded
    @POST("memories/get_images")
    Observable<MemoriesGetImagesResponse> getImages(@Field("date") String date, @Field("user_id") String userId,
                                                    @Field("image_pool") String imagePool);

    @FormUrlEncoded
    @POST("memories/upload_image")
    Observable<SimpleResponse> uploadImage(@Field("date") String date, @Field("user_id") String userId,
                                         @Field("image_id") String imageId, @Field("caption") String caption);


}
