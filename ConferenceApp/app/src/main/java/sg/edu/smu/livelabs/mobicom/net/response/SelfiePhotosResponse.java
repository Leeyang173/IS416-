package sg.edu.smu.livelabs.mobicom.net.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import sg.edu.smu.livelabs.mobicom.net.item.DeletedSelfie;
import sg.edu.smu.livelabs.mobicom.net.item.Selfie;


/**
 * Created by smu on 9/11/15.
 */
public class SelfiePhotosResponse {
    public String status;
    public String title;
    @SerializedName("icon_status")
    public String iconStatus;
    @SerializedName("images")
    public List<Selfie> selfies;
    @SerializedName("deleted_images")
    public List<DeletedSelfie> deletedSelfies;
}
