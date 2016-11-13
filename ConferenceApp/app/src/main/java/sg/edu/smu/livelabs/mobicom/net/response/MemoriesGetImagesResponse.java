package sg.edu.smu.livelabs.mobicom.net.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import sg.edu.smu.livelabs.mobicom.net.item.MemoriesItem;


/**
 * Created by smu on 9/11/15.
 */
public class MemoriesGetImagesResponse {
    public String status;
    @SerializedName("can_upload")
    public boolean canUpload;
    @SerializedName("images")
    public List<MemoriesItem> images;
}
