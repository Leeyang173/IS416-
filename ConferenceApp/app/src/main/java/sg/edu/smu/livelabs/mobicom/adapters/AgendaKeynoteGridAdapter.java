package sg.edu.smu.livelabs.mobicom.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.fileupload.UploadFileService;
import sg.edu.smu.livelabs.mobicom.models.data.AttendeeEntity;
import sg.edu.smu.livelabs.mobicom.views.UserInfoPopup;

/**
 * Created by johnlee on 18/2/16.
 */
public class AgendaKeynoteGridAdapter extends BaseAdapter {

    private Context c;
    private List<AttendeeEntity> keynoteUsers;

    public AgendaKeynoteGridAdapter(Context c, List<AttendeeEntity> keynoteUsers){
        this.c = c;
        this.keynoteUsers = keynoteUsers;
    }

    @Override
    public int getCount() {
        return keynoteUsers.size();
    }

    @Override
    public AttendeeEntity getItem(int position) {
        return keynoteUsers.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {

        return getCount();
    }

    @Override
    public int getItemViewType(int position) {

        return position;
    }

    @Override
    public boolean areAllItemsEnabled()
    {
        return false;
    }

    @Override
    public boolean isEnabled(int position)
    {
        return false;
    }

    public List<AttendeeEntity> getLeaders() {
        return keynoteUsers;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        View view = convertView;

        final ViewHolder holder;
        if (view == null) {
            holder = new ViewHolder();
            LayoutInflater vi = (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = vi.inflate(R.layout.agenda_keynote_user, parent, false);

            holder.nameTV = (TextView)view.findViewById(R.id.name_tv);
            holder.descriptionTV = (TextView)view.findViewById(R.id.description_tv);
            holder.avatarIV = (ImageView)view.findViewById(R.id.avatar_iv);
            holder.empty_profile = (TextView)view.findViewById(R.id.empty_profile_tv);
            holder.userLayoutRL = (RelativeLayout)view.findViewById(R.id.user_layout);
            view.setTag(holder);

        }
        else{
            holder = (ViewHolder) view.getTag();
        }

        final AttendeeEntity keynoteUser = keynoteUsers.get(getItemViewType(position));
        if (keynoteUser != null){
            holder.nameTV.setText(keynoteUser.getName());
            holder.descriptionTV.setText(keynoteUser.getDescription());
            UploadFileService.getInstance().loadAvatar(holder.avatarIV, keynoteUser.getAvatar(),
                    holder.empty_profile, keynoteUser.getName());

            holder.userLayoutRL.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new UserInfoPopup(c, keynoteUser);
                }
            });
        } else {
            holder.nameTV.setText("");
            holder.descriptionTV.setText("");
            holder.avatarIV.setImageResource(R.drawable.empty_profile);
        }

        return view;
    }

    private class ViewHolder {
        ImageView avatarIV;
        TextView nameTV;
        TextView empty_profile;
        TextView descriptionTV;
        RelativeLayout userLayoutRL;

    }


    //update new interests
    public void updates(List<AttendeeEntity> keynoteUsers){
        this.keynoteUsers = keynoteUsers;
        this.notifyDataSetChanged();
        this.notifyDataSetInvalidated();
    }
}
