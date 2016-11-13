package sg.edu.smu.livelabs.mobicom.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.marshalchen.ultimaterecyclerview.UltimateRecyclerviewViewHolder;
import com.marshalchen.ultimaterecyclerview.UltimateViewAdapter;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.dao.query.LazyList;
import de.hdodenhof.circleimageview.CircleImageView;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.UIHelper;
import sg.edu.smu.livelabs.mobicom.fileupload.UploadFileService;
import sg.edu.smu.livelabs.mobicom.models.data.AttendeeEntity;
import sg.edu.smu.livelabs.mobicom.services.TrackingService;
import sg.edu.smu.livelabs.mobicom.views.UserInfoPopup;

/**
 * Created by smu on 10/3/16.
 */
public class ContactsAdapter extends UltimateViewAdapter<UltimateRecyclerviewViewHolder> {
    private Context context;
    private LazyList<AttendeeEntity> attendeeEntities;

    public ContactsAdapter(Context context){
        this.context = context;
    }

    public void setData(LazyList<AttendeeEntity> attendees){
        LazyList<AttendeeEntity> temp = this.attendeeEntities;
        attendeeEntities = attendees;
        notifyDataSetChanged();
        if (temp != null){
            temp.close();
        }
    }

    @Override
    public UltimateRecyclerviewViewHolder getViewHolder(View view) {
        return new ViewHolder(view, true);
    }

    @Override
    public UltimateRecyclerviewViewHolder onCreateViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.contact_item, parent, false);
        return  new ViewHolder(view, true);
    }

    @Override
    public void onBindViewHolder(UltimateRecyclerviewViewHolder ultimateRecyclerviewViewHolder, int position) {
        if (position < getItemCount() &&
                (customHeaderView != null ? position <= attendeeEntities.size() : position < attendeeEntities.size()) &&
                (customHeaderView != null ? position > 0 : true)) {
            int realPosition = customHeaderView != null ? position - 1 : position;
            AttendeeEntity attendee = attendeeEntities.get(realPosition);
            ViewHolder VH = (ViewHolder) ultimateRecyclerviewViewHolder;
            VH.setData(attendee);
        }
    }

    @Override
    public int getAdapterItemCount() {
        return attendeeEntities == null ? 0 : attendeeEntities.size();
    }

    @Override
    public long generateHeaderId(int position) {
        return -1;
    }

    @Override
    public RecyclerView.ViewHolder onCreateHeaderViewHolder(ViewGroup parent) {
        return null;
    }

    @Override
    public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder, int position) {

    }

    public class ViewHolder extends UltimateRecyclerviewViewHolder implements View.OnClickListener{

        @Bind(R.id.contact_layout)
        public RelativeLayout contact;
        @Bind(R.id.empty_profile_tv)
        public TextView emptyProfileTV;
        @Bind(R.id.avatar_iv)
        public CircleImageView avatarView;
        @Bind(R.id.name_text)
        public TextView nameText;
        @Bind(R.id.description_text)
        public TextView descriptionText;
        @Bind(R.id.interest_text)
        public TextView interestText;
        private AttendeeEntity attendee;

        public ViewHolder(View itemView, boolean isItem) {
            super(itemView);
            if (isItem){
                ButterKnife.bind(this, itemView);
                UIHelper helper = UIHelper.getInstance();
                helper.setBoldTypeface(nameText);
                helper.setBoldTypeface(emptyProfileTV);
                helper.setItalicTypeface(descriptionText);
                contact.setOnClickListener(this);
            }
        }

        public void setData(AttendeeEntity attendee) {
            this.attendee = attendee;
            nameText.setText(attendee.getName());
            descriptionText.setText(attendee.getDescription());
//            interestText.setText(attendee.getInterests());
            UploadFileService.getInstance().loadAvatar(
                    avatarView, attendee.getAvatar(),
                    emptyProfileTV, attendee.getName());
        }
        @Override
        public void onClick(View v) {
            new UserInfoPopup(context, attendee);
            TrackingService.getInstance().sendTracking("203", "messages", "attendees", attendee.getUID().toString(), "", "");
        }
    }

}
