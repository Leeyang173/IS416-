package sg.edu.smu.livelabs.mobicom.views;


import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.provider.ContactsContract;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;
import flow.Flow;
import me.kaede.tagview.Tag;
import me.kaede.tagview.TagView;
import sg.edu.smu.livelabs.mobicom.App;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.UIHelper;
import sg.edu.smu.livelabs.mobicom.fileupload.UploadFileService;
import sg.edu.smu.livelabs.mobicom.models.data.AttendeeEntity;
import sg.edu.smu.livelabs.mobicom.presenters.ChatPresenter;
import sg.edu.smu.livelabs.mobicom.presenters.screen.ChatScreen;
import sg.edu.smu.livelabs.mobicom.services.ChatService;
import sg.edu.smu.livelabs.mobicom.services.DatabaseService;
import sg.edu.smu.livelabs.mobicom.services.TrackingService;


/**
 * Created by smu on 8/3/16.
 */
public class UserInfoPopup implements View.OnClickListener{

    private static MaterialDialog dialog;
    private Context context;
    private AttendeeEntity attendeeEntity;

    private CircleImageView closeBtn;
    private CircleImageView avatarImg;
    private TextView emptyAvatarTV;
    private TextView nameText;
    private TextView descriptionText;
    private TextView addToPhoneBtn;
    private TextView messageBtn;
    private TagView tagView;
    private ScrollView scrollView;

    public UserInfoPopup(final Context context, AttendeeEntity attendeeEntity) {
        dialog = new MaterialDialog.Builder(context)
                        .customView(R.layout.user_info_item, true)
                        .build();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        this.context = context;
        View view = dialog.getView();
        closeBtn = (CircleImageView) view.findViewById(R.id.close_btn);
        avatarImg = (CircleImageView) view.findViewById(R.id.avatar_image);
        emptyAvatarTV = (TextView) view.findViewById(R.id.empty_profile_tv);
        nameText = (TextView) view.findViewById(R.id.name_text);
        descriptionText = (TextView) view.findViewById(R.id.description_text);
        addToPhoneBtn = (TextView) view.findViewById(R.id.add_to_phone_btn);
        messageBtn = (TextView) view.findViewById(R.id.message_btn);
        tagView = (TagView) view.findViewById(R.id.tagview);
        scrollView = (ScrollView) view.findViewById(R.id.scrollView);
        UIHelper uiHelper = UIHelper.getInstance();
        uiHelper.setBoldTypeface(nameText);
        uiHelper.setBoldTypeface(emptyAvatarTV);
        uiHelper.setTypeface(descriptionText);
        uiHelper.setExo2TypeFace(addToPhoneBtn, false);
        uiHelper.setExo2TypeFace(messageBtn, false);
        if (attendeeEntity.getUID() == DatabaseService.getInstance().getMe().getUID()){
            addToPhoneBtn.setVisibility(View.GONE);
            messageBtn.setVisibility(View.GONE);
        } else{
            addToPhoneBtn.setOnClickListener(this);
            messageBtn.setOnClickListener(this);
        }

        closeBtn.setOnClickListener(this);
        showData(attendeeEntity);
    }

    private void showData(AttendeeEntity attendeeEntity){
        this.attendeeEntity = attendeeEntity;
        nameText.setText(attendeeEntity.getName());
        descriptionText.setText(attendeeEntity.getDescription());
        Picasso.with(context).cancelRequest(avatarImg);
        String avatar = attendeeEntity.getAvatar();
        UploadFileService.getInstance().loadAvatar(avatarImg, avatar,
                emptyAvatarTV, attendeeEntity.getName());
        String interestStr = attendeeEntity.getInterests();
        if (interestStr == null || interestStr.isEmpty()){
            tagView.setVisibility(View.GONE);
            scrollView.setVisibility(View.GONE);
        } else {
            String[] interests = interestStr.split(",");
            for (String interest : interests){
                Tag tag = new Tag(interest, ContextCompat.getColor(context, R.color.white));
                tag.tagTextColor = ContextCompat.getColor(context, R.color.colorPrimary);
                tag.radius = 5f;
                tag.tagTextSize = 14f;
                tag.layoutBorderSize = 0f;
                tagView.addTag(tag);
            }
        }
        dialog.show();
    }

    public static void dismiss(){
        if (dialog != null && dialog.isShowing()){
            dialog.dismiss();
        }
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id){
            case R.id.add_to_phone_btn:
                Intent intent = new Intent(ContactsContract.Intents.Insert.ACTION);
                intent.setType(ContactsContract.RawContacts.CONTENT_TYPE);
                intent.putExtra(ContactsContract.Intents.Insert.NAME, attendeeEntity.getName());
                intent.putExtra(ContactsContract.Intents.Insert.EMAIL, attendeeEntity.getEmail())
                        .putExtra(ContactsContract.Intents.Insert.EMAIL_TYPE, ContactsContract.CommonDataKinds.Email.TYPE_WORK);
                context.startActivity(intent);
                TrackingService.getInstance().sendTracking("204", "messages", "attendees", attendeeEntity.getUID().toString(), "add_to_phone", "");
                dialog.dismiss();
                break;
            case R.id.message_btn:
                if( App.getInstance().currentChatType != ChatPresenter.SINGLE_TYPE){
                    Flow.get(context).set(new ChatScreen(ChatService.
                            getInstance().findSingleChatRoom(attendeeEntity, true)));
                }
                TrackingService.getInstance().sendTracking("205", "messages", "attendees", attendeeEntity.getUID().toString(), "message", "");
                dialog.dismiss();
                break;
            case R.id.close_btn:
                dialog.dismiss();
                break;
        }
    }
}
