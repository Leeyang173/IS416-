package sg.edu.smu.livelabs.mobicom.net.api;

import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;
import rx.Observable;
import sg.edu.smu.livelabs.mobicom.net.response.EVAPromotionResponse;
import sg.edu.smu.livelabs.mobicom.net.response.SelfieLeaderboardResponse;
import sg.edu.smu.livelabs.mobicom.net.response.SelfieLikersResponse;
import sg.edu.smu.livelabs.mobicom.net.response.SelfiePhotosResponse;
import sg.edu.smu.livelabs.mobicom.net.response.SelfiePostPhotoResponse;
import sg.edu.smu.livelabs.mobicom.net.response.SelfieSearchResponse;
import sg.edu.smu.livelabs.mobicom.net.response.SelfieStatusResponse;
import sg.edu.smu.livelabs.mobicom.net.response.SimpleResponse;

/**
 * Created by smu on 27/10/15.
 */
public interface EVAPromotionAPI {


    @FormUrlEncoded
    @POST("coolfie_comp/get_comp_list")
    Observable<EVAPromotionResponse> getPromotions(@Field("user_id") String userId);

    @FormUrlEncoded
    @POST("coolfie_comp/upload_image")
    Observable<SelfiePostPhotoResponse> postPhoto(@Field("user_id") String userId,
                                                  @Field("image_id") String imageId,
                                                  @Field("comp_id") String promotionId,
                                                  @Field("description") String description);

    @FormUrlEncoded
    @POST("coolfie_comp/eva_image_status")
    Observable<SelfieStatusResponse> likeSelfie(@Field("user_id") String userId,
                                                @Field("id") String selfieId,
                                                @Field("comp_id") String promotionId,
                                                @Field("status") String status);

    @FormUrlEncoded
    @POST("coolfie_comp/get_image_like_details")
    Observable<SelfieLikersResponse> getLikesSelfieDetails(@Field("user_id") String userId,
                                                           @Field("id") String selfieId,
                                                           @Field("comp_id") String promotionId);


    @FormUrlEncoded
    @POST("coolfie_comp/edit_caption")
    Observable<SimpleResponse> editSelfie(@Field("user_id") String userId,
                                          @Field("id") String selfieId,
                                          @Field("comp_id") String promotionId,
                                          @Field("description") String description);

    @FormUrlEncoded
    @POST("coolfie_comp/delete_image")
    Observable<SimpleResponse> deleteSelfie(@Field("user_id") String userId,
                                            @Field("id") String selfieId,
                                            @Field("comp_id") String promotionId);

    @FormUrlEncoded
    @POST("coolfie_comp/get_user_images")
    Observable<SelfiePhotosResponse> getUserSelfie(@Field("user_id") String userId,
                                                   @Field("comp_id") String promotionId,
                                                   @Field("req_user_id") String requestUserId);


    @FormUrlEncoded
    @POST("coolfie_comp/get_new_images")
    Observable<SelfiePhotosResponse> getSelfies(@Field("user_id") String userId,
                                                @Field("comp_id") String promotionId,
                                                @Field("last_update_time") String time);

    @FormUrlEncoded
    @POST("coolfie_comp/leaderboard")
    Observable<SelfieLeaderboardResponse> getLeaderboard(@Field("user_id") String userId,
                                                         @Field("comp_id") String promotionId);

    @FormUrlEncoded
    @POST("coolfie_comp/comp_search")
    Observable<SelfieSearchResponse> search(@Field("user_id") String userId,
                                            @Field("comp_id") String promotionId,
                                            @Field("search_text") String searchText);

    @FormUrlEncoded
    @POST("coolfie_comp/tracking")
    Observable<SimpleResponse> tracking(@Field("user_id") String userId,
                                        @Field("function") String function,
                                        @Field("post1") String post1,
                                        @Field("post2") String post2,
                                        @Field("post3") String post3,
                                        @Field("post4") String post4);
}
