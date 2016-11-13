package sg.edu.smu.livelabs.mobicom.net.noties;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

/**
 * Created by Aftershock PC on 4/8/2015.
 */
public class GroupInvitedNoti {
    @SerializedName("group_id")
    public long groupId;
    public String content;
    public Date timestamp;
}
