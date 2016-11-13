package sg.edu.smu.livelabs.mobicom.net.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by smu on 9/3/16.
 */
public class GroupChatRoomInfo {
    public class Member {
        public String name;
        @SerializedName("user_id")
        public long userId;
        @SerializedName("is_admin")
        public String adminStatus;
        public String status;
        @SerializedName("email_id")
        public String email;
        @SerializedName("avatar")
        public String avatarId;
    }

    @SerializedName("id")
    public long groupId;

    public String title;
    @SerializedName("photo_id")
    public String avatar;
    @SerializedName("last_updated")
    public String lastUpdated;
    public boolean isAdmin;

    public List<Member> members;
}
