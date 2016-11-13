package sg.edu.smu.livelabs.mobicom.net.api;

import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;
import rx.Observable;
import sg.edu.smu.livelabs.mobicom.net.response.MacIpResponse;
import sg.edu.smu.livelabs.mobicom.net.response.SimpleResponse;

/**
 * Created by smu on 22/1/16.
 */
public interface BEPApi {

    @FormUrlEncoded
    @POST("notif_receipt_click_detection/record_receipt_detection")
    Observable<SimpleResponse> recordReceiptDetection(@Field("user_id") String userId, @Field("notif_id") String notifId,
                                                      @Field("receipt_timestamp") String timeStamp,
                                                      @Field("intervention_type") String type);

    @FormUrlEncoded
    @POST("notif_receipt_click_detection/record_click_detection")
    Observable<SimpleResponse> recorcClickDetection(@Field("user_id") String userId, @Field("notif_id") String notifId,
                                                    @Field("click_timestamp") String timeStamp,
                                                    @Field("intervention_type") String type);


    @FormUrlEncoded
    @POST("get_mac_from_ip")
    Observable<MacIpResponse> getMacFromIP(@Field("ip") String ip, @Field("public_ip") String publicIp);
}
