package sg.edu.smu.livelabs.mobicom.adapters;

import android.app.AlertDialog;
import android.app.Dialog;
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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.marshalchen.ultimaterecyclerview.SwipeableUltimateViewAdapter;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerviewViewHolder;
import com.marshalchen.ultimaterecyclerview.swipe.SwipeLayout;
import com.microsoft.socialplus.autorest.models.Reason;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.dao.query.LazyList;
import de.hdodenhof.circleimageview.CircleImageView;
import flow.Flow;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.UIHelper;
import sg.edu.smu.livelabs.mobicom.fileupload.UploadFileService;
import sg.edu.smu.livelabs.mobicom.models.data.AttendeeEntity;
import sg.edu.smu.livelabs.mobicom.models.data.TopicEntity;
import sg.edu.smu.livelabs.mobicom.presenters.screen.ForumTopicDetailScreen;
import sg.edu.smu.livelabs.mobicom.services.AgendaService;
import sg.edu.smu.livelabs.mobicom.services.AttendeesService;
import sg.edu.smu.livelabs.mobicom.services.DatabaseService;
import sg.edu.smu.livelabs.mobicom.services.ForumService;
import sg.edu.smu.livelabs.mobicom.services.TrackingService;
import sg.edu.smu.livelabs.mobicom.views.UserInfoPopup;

/**
 * Created by smu on 23/4/16.
 */
public class ForumAdapter extends SwipeableUltimateViewAdapter {
    private Context context;
    private LazyList<TopicEntity> topicEntities;
    private Dialog dialog;
    private int reasonPosition;

    public ForumAdapter(Context context) {
        this.context = context;
    }

    public void closeData(){
        if (topicEntities != null){
            topicEntities.close();
        }
    }

    public void setData(LazyList<TopicEntity> topics) {
        if (topics == null)
            return;
        LazyList<TopicEntity> temp = this.topicEntities;
        topicEntities = topics;
        notifyDataSetChanged();
        if (temp != null){
            temp.close();
        }
    }


    @Override
    public UltimateRecyclerviewViewHolder getViewHolder(View view) {
        return new ViewHolder(view, false);
    }

    @Override
    public UltimateRecyclerviewViewHolder onCreateViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.forum_item, parent, false);
        final ViewHolder viewHolder = new ViewHolder(view, true);
        SwipeLayout swipeLayout = viewHolder.swipeLayout;
        swipeLayout.setDragEdge(SwipeLayout.DragEdge.Right);
        swipeLayout.setShowMode(SwipeLayout.ShowMode.PullOut);
        return viewHolder;
    }

    @Override
    public int getAdapterItemCount() {
        return topicEntities == null ? 0 : topicEntities.size();
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

    @Override
    public void onBindViewHolder(UltimateRecyclerviewViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        if (position < getItemCount() &&
                (customHeaderView != null ? position <= topicEntities.size()
                        : position < topicEntities.size()) &&
                (customHeaderView != null ? position > 0 : true)) {
            TopicEntity topic = topicEntities.get(customHeaderView != null ? position - 1 : position);
            ((ViewHolder)holder).setContent(topic, position);
        }
    }

    class ViewHolder extends UltimateRecyclerviewViewHolder implements View.OnClickListener{
        @Bind(R.id.disable_view)
        public View disableView;
        @Bind(R.id.delete)
        public Button deleteBtn;
        @Bind(R.id.avatar_layout)
        public RelativeLayout avatarLayout;
        @Bind(R.id.empty_profile_tv)
        public TextView emptyProfileTv;
        @Bind(R.id.avatar_iv)
        public CircleImageView avatar;
        @Bind(R.id.name_tv)
        public TextView nameTV;
        @Bind(R.id.time_tv)
        public TextView timeTV;
        @Bind(R.id.title_tv)
        public TextView titleTV;
        @Bind(R.id.comments_no_tv)
        public TextView commentsTV;

        @Bind(R.id.report_btn)
        public ImageView reportBtn;
        @Bind(R.id.likes_no_tv)
        public TextView likeTV;
        @Bind(R.id.likeBtn)
        public ImageView likeBtn;
        @Bind(R.id.content_layout)
        public RelativeLayout mainLayout;
        private TopicEntity entity;
        private int position;
        public ViewHolder(View view, boolean isItem){
            super(view);
            if (!isItem) return;
            ButterKnife.bind(this, view);
            UIHelper uiHelper = UIHelper.getInstance();
            uiHelper.setTypeface(nameTV, commentsTV);
            uiHelper.setBoldTypeface(titleTV);
            uiHelper.setBoldTypeface(deleteBtn);
            uiHelper.setBoldTypeface(emptyProfileTv);
            deleteBtn.setOnClickListener(this);
            mainLayout.setOnClickListener(this);
            avatarLayout.setOnClickListener(this);
            nameTV.setOnClickListener(this);
            likeBtn.setVisibility(View.GONE);
//            likeBtn.setOnClickListener(this);
            reportBtn.setOnClickListener(this);
            view.setOnClickListener(this);
        }

        public void setContent(TopicEntity topic, int position){
            this.position = position;
            this.entity = topic;
            titleTV.setText(topic.getText());
            nameTV.setText(topic.getUserFullName());
            UploadFileService.getInstance().loadAvatarURL(
                    avatar, topic.getUserPhotoUrl(),
                    emptyProfileTv, topic.getUserFullName()
            );
            long time = (System.currentTimeMillis() - topic.getCreatedTime().getTime()) / 1000;
            if (time < 0){
                time = 0;
            }
            if (time < 60){
                timeTV.setText(time+"s");
            } else {
                time = time/60;
                if (time < 60){
                    timeTV.setText(time+"m");
                } else{
                    time = time/60;
                    if (time > 24){
                        long day = time / 24;
                        if(day < 7){
                            timeTV.setText(day+"d");
                        } else {
                            if (day > 7){
                                long week = day / 7;
                                timeTV.setText(week+"w");
                            }
                        }
                    } else {
                        timeTV.setText(time+"h");
                    }
                }
            }
            String likes = "", comments = "";
//            if (topic.getTotalLikes() > 1){
//                likes = topic.getTotalLikes() + " LIKES";
//            } else{
//                likes = topic.getTotalLikes() + " LIKE";
//            }
//            likeTV.setText(likes);
            if (topic.getTotalComments() > 1){
                comments = topic.getTotalComments() + " COMMENTS";
            } else{
                comments = topic.getTotalComments() + " COMMENT";
            }
            commentsTV.setText(comments);
            if ("".equals(topic.getTopicHandle())){
                disableView.setVisibility(View.VISIBLE);
            } else {
                //topic offline, haven't sync
                disableView.setVisibility(View.GONE);
            }
            if(topic.getUserHandle().equals(ForumService.getInstance().getUserHandler())
                ||DatabaseService.getInstance().getMe().isModerator()){
                deleteBtn.setEnabled(true);
            } else {
                deleteBtn.setEnabled(false);
            }

        }

        @Override
        public void onClick(View v) {
            int id = v.getId();
            switch (id){
                case R.id.delete:
                    UIHelper.getInstance()
                            .showProgressDialog(context,
                                    context.getString(R.string.progressing), true);
                    if (DatabaseService.getInstance().getMe().isModerator()){
                        UIHelper.getInstance().showProgressDialog(context, context.getString(R.string.progressing), true);
                        AgendaService.getInstance().deleteATopic(entity);
                    } else {
                        ForumService.getInstance().deleteTopic(entity);
                        setData(ForumService.getInstance().getAllTopic());
                    }
                    swipeLayout.close();
                    break;
                case R.id.avatar_layout:
                    AttendeeEntity attendeeEntity = AttendeesService.getInstance().getAttendeesByUserHandle(entity.getUserHandle());
                    if(attendeeEntity != null){
                        new UserInfoPopup(context, attendeeEntity);
                    }
                    break;
                case R.id.content_layout:
                    if (disableView.getVisibility() == View.GONE){
                        Flow.get(context).set(new ForumTopicDetailScreen(entity));
                        TrackingService.getInstance().sendTracking("517", "more", "forum", entity.getTopicHandle(), "", "");
                    }
                    break;
                case R.id.report_btn:
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
                            if(reasonPosition < 0){
                                Toast.makeText(context, "Please select a reason", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            ForumService.getInstance().reportATopic(entity.getTopicHandle(), reasons[reasonPosition]);
                            dialog.dismiss();
                        }
                    });
                    dialog.show();
                    break;
            }
        }
    }
}
