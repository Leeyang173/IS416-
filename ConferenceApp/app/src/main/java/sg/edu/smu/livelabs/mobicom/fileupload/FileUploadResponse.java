package sg.edu.smu.livelabs.mobicom.fileupload;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Le Gia Hai on 13/5/2015.
 */
public class FileUploadResponse {
    @SerializedName("details")
    public String id;
    public String status;
}
