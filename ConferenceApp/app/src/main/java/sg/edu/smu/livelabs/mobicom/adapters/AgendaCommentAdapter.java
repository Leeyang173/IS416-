package sg.edu.smu.livelabs.mobicom.adapters;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.marshalchen.ultimaterecyclerview.SwipeableUltimateViewAdapter;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerviewViewHolder;
import com.marshalchen.ultimaterecyclerview.swipe.SwipeLayout;
import com.microsoft.socialplus.autorest.models.Reason;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.UIHelper;
import sg.edu.smu.livelabs.mobicom.fileupload.UploadFileService;
import sg.edu.smu.livelabs.mobicom.models.data.AttendeeEntity;
import sg.edu.smu.livelabs.mobicom.models.data.CommentEntity;
import sg.edu.smu.livelabs.mobicom.services.AttendeesService;
import sg.edu.smu.livelabs.mobicom.services.DatabaseService;
import sg.edu.smu.livelabs.mobicom.services.ForumService;
import sg.edu.smu.livelabs.mobicom.views.UserInfoPopup;

/**
 * Created by smu on 2/3/16.
 */
public class AgendaCommentAdapter extends SwipeableUltimateViewAdapter {
    private Context context;
    private List<CommentEntity> comments;
    private boolean isEvent;
    private Dialog dialog;
    private int reasonPosition;

    public AgendaCommentAdapter(Context context, boolean isEvent){
        this.context = context;
        this.comments = new ArrayList<>();
        this.isEvent = isEvent;
    }

    @Override
    public ViewHolder getViewHolder(View view) {
        return new ViewHolder(view, false);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.agenda_topic_comment_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view, true);
        SwipeLayout swipeLayout = viewHolder.swipeLayout;
        swipeLayout.setDragEdge(SwipeLayout.DragEdge.Right);
        swipeLayout.setShowMode(SwipeLayout.ShowMode.PullOut);
        return viewHolder;
    }

    @Override
    public int getAdapterItemCount() {
        return comments == null ? 0 : comments.size();
    }

    @Override
    public long generateHeaderId(int position) {
        return -1;
    }

    @Override
    public void onBindViewHolder(UltimateRecyclerviewViewHolder holder, int position) {
        if (position < getItemCount() &&
                (customHeaderView != null ? position <= comments.size() : position < comments.size()) &&
                (customHeaderView != null ? position > 0 : true)) {
            CommentEntity comment = comments.get(customHeaderView != null ? position - 1 : position);
            ((ViewHolder)holder).setData(comment);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateHeaderViewHolder(ViewGroup parent) {
        return null;
    }

    @Override
    public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder, int position) {

    }

    public void setData(List<CommentEntity> agendaComments){
        if (agendaComments != null){
            comments.clear();
            comments.addAll(agendaComments);
            notifyDataSetChanged();
        } else {
            comments.clear();
            notifyDataSetChanged();
        }

    }

    public void insertFirst(CommentEntity comment){
        insertFirstInternal(comments, comment);
    }

    public class ViewHolder extends UltimateRecyclerviewViewHolder implements View.OnClickListener{
        @Bind(R.id.avatar_layout)
        public RelativeLayout avatarLayout;
        @Bind(R.id.empty_profile_tv)
        public TextView emptyProfileTV;
        @Bind(R.id.profile_iv)
        public ImageView profileIV;
        @Bind(R.id.time_txt)
        public TextView timeTxt;
        @Bind(R.id.comment_txt)
        public TextView commentTV;
        @Bind(R.id.name_txt)
        public TextView nameTV;
        @Bind(R.id.delete_btn)
        public Button deleteBtn;
        @Bind(R.id.like_layout)
        public RelativeLayout likeLayout;
        @Bind(R.id.like_btn)
        public ImageView likeBtn;
        @Bind(R.id.like_count)
        public TextView likesTV;
        @Bind(R.id.report_layout)
        public RelativeLayout reportLayout;
        @Bind(R.id.report_btn)
        public ImageView reportBtn;
        @Bind(R.id.content_layout)
        public LinearLayout linearLayout;
        private CommentEntity comment;
        private long likeCounts;
        public ViewHolder(View itemView, boolean isItem) {
            super(itemView);
            if (!isItem) return;
            ButterKnife.bind(this, itemView);
            UIHelper uiHelper = UIHelper.getInstance();
            uiHelper.setBoldTypeface(commentTV);
            uiHelper.setBoldTypeface(emptyProfileTV);
            uiHelper.setTypeface(nameTV, timeTxt, likesTV);
            deleteBtn.setOnClickListener(this);
            avatarLayout.setOnClickListener(this);
            nameTV.setOnClickListener(this);
            likeLayout.setOnClickListener(this);
            reportLayout.setOnClickListener(this);
            linearLayout.setLongClickable(true);
            linearLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    ClipboardManager cManager = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData cData = ClipData.newPlainText(ChatMessagesAdapter.clipboardName, comment.getText());
                    cManager.setPrimaryClip(cData);
                    Toast.makeText(getContext(), "A message is Copied.", Toast.LENGTH_LONG).show();
                    return true;
                }
            });
        }

        public void setData(CommentEntity comment){
            swipeLayout.close(false, false);
            this.comment = comment;
            Picasso.with(context).cancelRequest(profileIV);
            UploadFileService.getInstance().loadAvatarURL(
                    profileIV, comment.getUserPhotoUrl(),
                    emptyProfileTV, comment.getUserFullName());
            long time = (System.currentTimeMillis() - comment.getCreatedTime().getTime()) / 1000;
            if (time < 0) time = 0;
            if (time < 60){
                timeTxt.setText(time+"s");
            } else {
                time = time/60;
                if (time < 60){
                    timeTxt.setText(time+"m");
                } else{
                    time = time/60;
                    if (time > 24){
                        long day = time / 24;
                        if(day < 7){
                            timeTxt.setText(day+"d");
                        } else {
                            if (day > 7){
                                long week = day / 7;
                                timeTxt.setText(week+"w");
                            }
                        }
                    } else {
                        timeTxt.setText(time+"h");
                    }
                }
            }
            nameTV.setText(comment.getUserFullName());
            commentTV.setText(comment.getText());
            String likes = " like";
            if(comment.getTotalLikeCount() == null)
                likeCounts = 0l;
            else
                likeCounts = comment.getTotalLikeCount();
            if(likeCounts > 1){
                likes+= "s";
            }
            likesTV.setText(likeCounts+ likes);
            if (comment.getUserHandle().equals(ForumService.getInstance().getUserHandler())){
                deleteBtn.setEnabled(true);
            } else {
                deleteBtn.setEnabled(false);
            }
//            if (isEvent){
                likeLayout.setVisibility(View.VISIBLE);
                likeBtn.setSelected(comment.getLiked());
//            } else{
//                likeLayout.setVisibility(View.GONE);
//            }

            if(comment.getUserFullName().toLowerCase().trim().equals(DatabaseService.getInstance().getMe().getName().toLowerCase().trim())){
                reportLayout.setVisibility(View.GONE);
                RelativeLayout.LayoutParams layout = (RelativeLayout.LayoutParams)likeLayout.getLayoutParams();
                layout.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                likeLayout.setLayoutParams(layout);
            }
            else{
                reportLayout.setVisibility(View.VISIBLE);
            }

        }

        @Override
        public void onClick(View v) {
            int id = v.getId();
            switch (id){
                case R.id.delete_btn:
                    UIHelper.getInstance().showProgressDialog(context,
                            context.getString(R.string.progressing), true);
                    ForumService.getInstance().deleteCommentSP(comment);
                    break;
                case R.id.name_txt:
                case R.id.avatar_layout:
                    AttendeeEntity attendeeEntity = AttendeesService.getInstance().getAttendeesByUserHandle(comment.getUserHandle());
                    if(attendeeEntity != null){
                        new UserInfoPopup(context, attendeeEntity);
                    }
                    break;
                case R.id.like_layout:
                    String likes = " like";
                    if (likeBtn.isSelected()){
                        likeBtn.setSelected(false);
                        likeCounts--;
                        if(likeCounts > 1){
                            likes+= "s";
                        }

                        likesTV.setText(likeCounts+ likes);
                        ForumService.getInstance().deleteLikeAComment(comment);
                    } else {
                        likeBtn.setSelected(true);
                        likeCounts++;
                        if(likeCounts > 1){
                            likes+= "s";
                        }
                        likesTV.setText(likeCounts + likes);
                        ForumService.getInstance().likeAComment(comment);
                    }
                    break;
                case R.id.report_layout:
                    if(dialog!= null && dialog.isShowing()){
                        dialog.dismiss();
                    }
                    dialog = new Dialog(context);
                    dialog.setContentView(R.layout.dialog_box_report_comment);
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

                    reasonPosition = -1;
                    final Reason[] reasons = Reason.values();
                    final EditText reason = (EditText) dialog.findViewById(R.id.report);
                    Button cancelBtn = (Button) dialog.findViewById(R.id.closeBtn);
                    Button reportBtn = (Button) dialog.findViewById(R.id.reportBtn);

                    reason.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            builder.setTitle("Select reason");

                            builder.setItems(
                                    new CharSequence[]{"Threats Cyberbullying Harassment", "Child Endangerment Exploitation", "Offensive Content",
                                            "Virus Spyware Malware", "Content Infringement","Others" ,"None"},
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog,
                                                            int which) {
                                            switch(which){
                                                case 0:reason.setText("Threats Cyberbullying Harassment");
                                                    break;
                                                case 1: reason.setText("Child Endangerment Exploitation");
                                                    break;
                                                case 2: reason.setText("Offensive Content");
                                                    break;
                                                case 3: reason.setText("Virus Spyware Malware");
                                                    break;
                                                case 4: reason.setText("Content Infringement");
                                                    break;
                                                case 5: reason.setText("Others");
                                                    break;
                                                case 6: reason.setText("None");
                                                    break;
                                            }
                                            reasonPosition = which;
                                        }
                                    });
                            builder.show();
                        }
                    });

                    cancelBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(dialog != null && dialog.isShowing())
                                dialog.dismiss();
                        }
                    });

                    reportBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(reasonPosition > -1) {
                                ForumService.getInstance().reportComment(comment.getCommentHandle(), reasons[reasonPosition]);
                                dialog.dismiss();
                                UIHelper.getInstance().showProgressDialog(context, "Reporting...", false);
                            }
                            else{
                                Toast.makeText(context, "Please select a reason", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    dialog.show();
                    break;
            }
        }
    }
}
