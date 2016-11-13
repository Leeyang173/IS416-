package sg.edu.smu.livelabs.mobicom.models;


import com.searchView.SearchItem;

import me.kaede.tagview.Tag;
import sg.edu.smu.livelabs.mobicom.models.data.AttendeeEntity;

/**
 * Created by smu on 23/7/15.
 */
public class AttendeeTag extends Tag implements SearchItem{
    private String searchKey;
    public AttendeeEntity attendee;
    public boolean isAdded;

    public AttendeeTag(String text) {
        super(text);
        if (text != null){
            searchKey = text.trim().toLowerCase();
        } else {
            searchKey = "";
        }
        isAdded = false;
        this.isDeletable = true;
        this.radius = 5f;
        this.layoutBorderSize = 0f;
        this.layoutColor = 0xfff26429;
        this.tagTextColor = 0xFF000000;
        this.deleteIndicatorColor = 0xFF000000;
    }

    public boolean match(CharSequence constraint){
        if (searchKey.contains(constraint)){
            return true;
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof AttendeeTag){
            return id == ((AttendeeTag)o).id;
        }
        return false;
    }

    // in add group chat
    public AttendeeTag(Long userId, String name, String email, String avatarId, String description) {
        this(name);
        AttendeeEntity attendee = new AttendeeEntity();
        attendee.setName(name);
        attendee.setUID(userId);
        attendee.setEmail(email);
        if (avatarId == null || avatarId.isEmpty()){
            attendee.setAvatar("-1");
        } else {
            attendee.setAvatar(avatarId);
        }
        if (description == null || description.isEmpty()){
            attendee.setDescription("-1");
        } else {
            attendee.setDescription(description);
        }

        this.attendee = attendee;
        this.id = userId.hashCode();
//        searchKey = searchKey.concat(name);
    }

    public void setDescription(String description){
        attendee.setDescription(description);
    }


}
