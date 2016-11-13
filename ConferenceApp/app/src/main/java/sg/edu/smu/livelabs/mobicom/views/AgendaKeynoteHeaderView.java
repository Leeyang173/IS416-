package sg.edu.smu.livelabs.mobicom.views;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.UIHelper;
import sg.edu.smu.livelabs.mobicom.adapters.AgendaKeynoteGridAdapter;
import sg.edu.smu.livelabs.mobicom.models.AgendaEvent;
import sg.edu.smu.livelabs.mobicom.models.data.AttendeeEntity;

/**
 * Created by smu on 5/5/16.
 */
public class AgendaKeynoteHeaderView {
    @Bind(R.id.time_tv)
    public TextView timeTV;
    @Bind(R.id.room_no_tv)
    public TextView roomNoTV;
    @Bind(R.id.title_tv)
    public TextView titleTV;
    @Bind(R.id.sub_title_tv)
    public TextView subTitleTV;
//    @Bind(R.id.user_layout)
//    public RelativeLayout userLayout;
//    @Bind(R.id.empty_profile_tv)
//    public TextView empty_profile;
//    @Bind(R.id.avatar_iv)
//    public ImageView avatarIV;
//    @Bind(R.id.name_tv)
//    public TextView nameTV;
//    @Bind(R.id.description_tv)
//    public TextView descriptionTV;
    @Bind(R.id.about_speaker_tv)
    public TextView aboutSpeakerTV;
    @Bind(R.id.content_tv)
    public TextView contentTV;
    @Bind(R.id.comment_header_tv)
    public TextView commentHeader;

    @Bind(R.id.key_note_users)
    public CustomGridView keyNoteUserGrid;

    @Bind(R.id.be_first_tv)
    public TextView beFirstTV;

    private Context context;
    private AgendaKeynoteGridAdapter adapter;

    public AgendaKeynoteHeaderView(Context context, View view){
        this.context = context;
        ButterKnife.bind(this, view);
        UIHelper uiHelper = UIHelper.getInstance();
        uiHelper.setBoldTypeface(timeTV);
        uiHelper.setBoldTypeface(roomNoTV);
        uiHelper.setBoldTypeface(titleTV);
        uiHelper.setBoldTypeface(subTitleTV);
        uiHelper.setBoldTypeface(aboutSpeakerTV);
//        uiHelper.setBoldTypeface(nameTV);
        uiHelper.setBoldTypeface(beFirstTV);
//        uiHelper.setBoldTypeface(empty_profile);
        uiHelper.setTypeface(commentHeader, contentTV);
    }

    public void setData(AgendaEvent event){
        timeTV.setText(event.getStartTimeStr());
        roomNoTV.setText(event.getRoom());
        titleTV.setText(event.getTitle());
        subTitleTV.setText(event.getDescription());
        List<AttendeeEntity> keynoteAttendees = event.getKeynoteUsers();

        adapter = new AgendaKeynoteGridAdapter(context, keynoteAttendees);
        if(keynoteAttendees != null && keynoteAttendees.size() > 0)
            keyNoteUserGrid.setAdapter(adapter);

        if(event.getEventEntity().getKeynoteUserDetail() == null || event.getEventEntity().getKeynoteUserDetail().isEmpty()){
            contentTV.setVisibility(View.GONE);
            aboutSpeakerTV.setVisibility(View.GONE);
        }
        else{
            contentTV.setText(event.getEventEntity().getKeynoteUserDetail());
        }

//        AttendeeEntity attendeeEntity = event.getKeynoteUser();
//        if (attendeeEntity != null){
//            nameTV.setText(attendeeEntity.getName());
//            descriptionTV.setText(attendeeEntity.getDescription());
//            UploadFileService.getInstance().loadAvatar(avatarIV, attendeeEntity.getAvatar(),
//                    empty_profile, attendeeEntity.getName());
//        } else {
//            nameTV.setText("");
//            descriptionTV.setText("");
//            avatarIV.setImageResource(R.drawable.empty_profile);
//        }

//       TODO contentTV.setText(event.get); about speaker?????
    }

}
