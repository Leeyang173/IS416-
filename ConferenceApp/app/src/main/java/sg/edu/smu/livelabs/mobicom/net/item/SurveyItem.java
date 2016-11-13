package sg.edu.smu.livelabs.mobicom.net.item;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

/**
 * Created by smu on 15/1/16.
 */
public class SurveyItem {
    public String id;
    public String title;
    @SerializedName("start_time")
    public Date startTime;
    @SerializedName("end_time")
    public Date endTime;
    @SerializedName("form_url")
    public String url;
    @SerializedName("form_id")
    public String formId;
    @SerializedName("type")
    public String type;
    @SerializedName("image_url")
    public String imageURL;
    public String status;
    @SerializedName("insert_time")
    public Date insertTime;
    @SerializedName("last_modified_time")
    public Date lastModifiedTime;

    public SurveyItem() {
    }
}
