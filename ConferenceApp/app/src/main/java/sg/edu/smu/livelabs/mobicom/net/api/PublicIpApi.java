package sg.edu.smu.livelabs.mobicom.net.api;

import com.squareup.okhttp.ResponseBody;

import retrofit.Call;
import retrofit.http.GET;

/**
 * Created by smu on 27/5/16.
 */
public interface PublicIpApi {


    @GET("./")
    Call<ResponseBody> getPublicIP();
}
