package sg.edu.smu.livelabs.mobicom.views;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.UIHelper;
import sg.edu.smu.livelabs.mobicom.fileupload.UploadFileService;
import sg.edu.smu.livelabs.mobicom.models.data.TopicEntity;

/**
 * Created by smu on 23/4/16.
 */
public class ForumTopicDetailHeaderView {
    @Bind(R.id.empty_profile_tv)
    public TextView emptyProfileTV;
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
    @Bind(R.id.likes_no_tv)
    public TextView likeTV;
    @Bind(R.id.likeBtn)
    public ImageView likeBtn;
    private Context context;

    public ForumTopicDetailHeaderView(View view, Context context){
        ButterKnife.bind(this, view);
        this.context = context;
        UIHelper uiHelper = UIHelper.getInstance();
        uiHelper.setBoldTypeface(titleTV);
        uiHelper.setBoldTypeface(emptyProfileTV);
        uiHelper.setTypeface(timeTV, nameTV, commentsTV);//likeTV,
    }

    public void loadData(TopicEntity topic){
        nameTV.setText(topic.getUserFullName());
        titleTV.setText(topic.getText());
        UploadFileService.getInstance().loadAvatarURL(
                avatar, topic.getUserPhotoUrl(),
                emptyProfileTV, topic.getUserFullName()
        );
        long time = (System.currentTimeMillis() - topic.getCreatedTime().getTime()) / 1000;
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
        if (topic.getTotalLikes() > 1){
            likes = topic.getTotalLikes() + " LIKES";
        } else{
            likes = topic.getTotalLikes() + " LIKE";
        }
        likeTV.setText(likes);

        if (topic.getTotalComments() > 1){
            comments = topic.getTotalComments() + " COMMENTS";
        } else{
            comments = topic.getTotalComments() + " COMMENT";
        }
        commentsTV.setText(comments);
    }
}
