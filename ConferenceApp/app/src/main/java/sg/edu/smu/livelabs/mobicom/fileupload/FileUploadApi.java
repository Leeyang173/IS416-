package sg.edu.smu.livelabs.mobicom.fileupload;

import com.squareup.okhttp.RequestBody;

import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.Part;
import retrofit.http.Query;
import rx.Observable;

/**
 * Created by Le Gia Hai on 13/5/2015.
 */
public interface FileUploadApi {
    @Multipart
    @POST("upload-photo")
    Observable<FileUploadResponse> uploadFile(@Part("file\"; filename=\"image.jpg\"") RequestBody resource, @Query("userId") String path);

    @FormUrlEncoded
    @POST("image_upload/store")
    Observable<FileUploadResponse> uploadFile1(@Field("data") String ImageData, @Field("user_id") long userId );

}
