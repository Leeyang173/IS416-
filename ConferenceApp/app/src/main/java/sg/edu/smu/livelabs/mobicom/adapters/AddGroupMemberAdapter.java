package sg.edu.smu.livelabs.mobicom.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.searchView.SearchAdapter;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.UIHelper;
import sg.edu.smu.livelabs.mobicom.fileupload.UploadFileService;
import sg.edu.smu.livelabs.mobicom.models.AttendeeTag;

/**
 * Created by smu on 21/7/15.
 */
public class AddGroupMemberAdapter extends SearchAdapter<AttendeeTag> {

    private Context context;
    private AddGroupMemberListener listener;

    public AddGroupMemberAdapter(Context context, List<AttendeeTag> items, AddGroupMemberListener listener) {
        super(items);
        this.context = context;
        this.listener = listener;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (position >= items.size()) return null;
        if (convertView == null){
            convertView = LayoutInflater.from(context).inflate(R.layout.add_group_member_item, parent, false);
            ViewHolder VH = new ViewHolder(convertView);
            convertView.setTag(VH);
        }
        ViewHolder VH = (ViewHolder) convertView.getTag();
        AttendeeTag tag = items.get(position);
        VH.nameText.setText(tag.text);
        if (tag.isAdded){
            VH.inviteTV.setText("Remove");
            VH.inviteTV.setTextColor(0xff6d6d6c);
        } else {
            VH.inviteTV.setText("Invite");
            VH.inviteTV.setTextColor(0xffcb2d27);
        }
        String description = tag.attendee.getDescription();
        if (description != null && !description.isEmpty() && !"-1".equals(description)){
            VH.descriptionText.setText(tag.attendee.getDescription());
        }
        UploadFileService.getInstance().loadAvatar(
                VH.avatarImage, tag.attendee.getAvatar(),
                VH.emptyProfileTV, tag.text
        );
        VH.tag = tag;
        //TODO
        return convertView;
    }

    public void filter(CharSequence searchKey){
        if (searchKey == null || searchKey.toString().equals("")){
            items.clear();
            items.addAll(shallowCopy);
            notifyDataSetChanged();
        } else {
            getFilter().filter(searchKey);
        }
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        @Bind(R.id.name_text)
        public TextView nameText;
        @Bind(R.id.description_text)
        public TextView descriptionText;
        @Bind(R.id.empty_profile_tv)
        public TextView emptyProfileTV;
        @Bind(R.id.avatar_image)
        public ImageView avatarImage;
        @Bind(R.id.invite_tv)
        public TextView inviteTV;
        public AttendeeTag tag;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            UIHelper uiHelper = UIHelper.getInstance();
            uiHelper.setBoldTypeface(nameText);
            uiHelper.setBoldTypeface(emptyProfileTV);
            uiHelper.setTypeface(descriptionText);
            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            if (tag.isAdded){
                listener.removeTag(tag);
            } else {
                listener.addTag(tag);
            }

        }
    }

    public interface AddGroupMemberListener{
        void addTag(AttendeeTag tag);
        void removeTag(AttendeeTag tag);
    }

}
