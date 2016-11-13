package sg.edu.smu.livelabs.mobicom.net.response;

import android.util.Log;

import com.google.gson.annotations.SerializedName;

import sg.edu.smu.livelabs.mobicom.App;


/**
 * Created by smu on 4/4/16.
 */
public class AttendeeResponse {
    @SerializedName("user_id")
    public long UID;
    public String name;
    @SerializedName("first_name")
    public String firstName;
    @SerializedName("email_id")
    public String email;
    public String role;
    public String desig;
    @SerializedName("organisation")
    public String description;
    public String status;
    @SerializedName("avatar_id")
    public String avatar;
    public String interests;
    @SerializedName("user_handle")
    public String userHandle;

    public AttendeeResponse(){
        Log.d(App.APP_TAG, "AttendeeResponse");
    }
}
