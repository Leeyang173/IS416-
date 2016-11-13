package sg.edu.smu.livelabs.mobicom.adapters;

import android.content.Context;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.marshalchen.ultimaterecyclerview.UltimateRecyclerviewViewHolder;
import com.marshalchen.ultimaterecyclerview.UltimateViewAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import flow.Flow;
import sg.edu.smu.livelabs.mobicom.App;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.UIHelper;
import sg.edu.smu.livelabs.mobicom.busEvents.AgendaReloadingEvent;
import sg.edu.smu.livelabs.mobicom.fileupload.UploadFileService;
import sg.edu.smu.livelabs.mobicom.models.AgendaEvent;
import sg.edu.smu.livelabs.mobicom.models.data.AttendeeEntity;
import sg.edu.smu.livelabs.mobicom.models.data.EventEntity;
import sg.edu.smu.livelabs.mobicom.models.data.GameEventEntity;
import sg.edu.smu.livelabs.mobicom.presenters.AgendaPresenter;
import sg.edu.smu.livelabs.mobicom.presenters.screen.AgendaTopicDetailScreen;
import sg.edu.smu.livelabs.mobicom.services.AgendaService;
import sg.edu.smu.livelabs.mobicom.services.GameService;
import sg.edu.smu.livelabs.mobicom.services.TrackingService;
import sg.edu.smu.livelabs.mobicom.util.UtilityUI;

/**
 * Created by smu on 26/2/16.
 */
public class AgendaAdapter extends UltimateViewAdapter<AgendaAdapter.ViewHolder> {

    private Context context;
    private AgendaListener listener;
    private List<AgendaEvent> events;

    public AgendaAdapter(Context context, AgendaListener listener){
        this.context = context;
        this.listener = listener;
        events = new ArrayList<>();
    }

    public void setData (List<AgendaEvent> events){
        this.events.clear();
        this.events.addAll(events);
        this.notifyDataSetChanged();
    }

    @Override
    public ViewHolder getViewHolder(View view) {
        return new ViewHolder(view);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.agenda_event_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public int getAdapterItemCount() {
        return events.size();
    }

    @Override
    public long generateHeaderId(int position) {
//        if (customHeaderView != null)
//            position--;
//        if (position < events.size()){
//            return events.get(position).getStartTime().getTime();
//        }
        return -1;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (position < getItemCount() &&
                (customHeaderView != null ? position <= events.size() : position < events.size()) &&
                (customHeaderView != null ? position > 0 : true)) {
            AgendaEvent event = events.get(customHeaderView != null ? position - 1 : position);
            holder.setData(event);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateHeaderViewHolder(ViewGroup parent) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.agenda_time_header, parent, false);
        return new HeaderViewHolder(v);
    }

    @Override
    public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (customHeaderView != null)
            position--;
        if (position >= events.size()) return;
        HeaderViewHolder headerViewHolder = (HeaderViewHolder) holder;
        headerViewHolder.time.setText(events.get(position).getStartTimeStr());
    }

    public class ViewHolder extends UltimateRecyclerviewViewHolder implements View.OnClickListener{
        @Bind(R.id.background100_view)
        public ImageView background100;
        @Bind(R.id.background60_view)
        public ImageView background60;
        @Bind(R.id.check_layout)
        public LinearLayout checkLayout;
        @Bind(R.id.check_in_icon)
        public ImageView checkInBtn;
        @Bind(R.id.likes_text)
        public TextView likesTxt;
        @Bind(R.id.time_text)
        public TextView timeTxt;

        @Bind(R.id.room_no_text)
        public TextView roomNoTxt;

        @Bind(R.id.session_layout)
        public LinearLayout sessionLayout;
        @Bind(R.id.title_text)
        public TextView titleTxt;
        @Bind(R.id.sub_title_text)
        public TextView subTitleTxt;
        @Bind(R.id.rating_layout)
        public LinearLayout ratingLayout;
        @Bind(R.id.session_rating_text)
        public TextView sessionRatingTxt;
        @Bind(R.id.session_answers_text)
        public TextView sessionAnswerTxt;
        @Bind(R.id.session_comment_text)
        public TextView sessionCommentTxt;
        @Bind(R.id.session_rating_no_text)
        public TextView sessionRatingNoTxt;
        @Bind(R.id.session_answers_no_text)
        public TextView sessionAnswerNoTxt;
        @Bind(R.id.session_comment_no_text)
        public TextView sessionCommentNoTxt;
        @Bind(R.id.topic_layout)
        public RelativeLayout topicLayout;
        @Bind(R.id.topics)
        public ListView topics;//paperEvent
        private AgendaEvent event;

        @Bind(R.id.tea_break_layout)
        public LinearLayout teabreakLayout;
        @Bind(R.id.title_text_teabreak)
        public TextView teaBreakTitleTxt;
        @Bind(R.id.game_layout)
        public RelativeLayout gameLayout;
        @Bind(R.id.game0_image)
        public CircleImageView game0;
        @Bind(R.id.game1_image)
        public CircleImageView game1;
        @Bind(R.id.game2_image)
        public CircleImageView game2;
        @Bind(R.id.try_these_game)
        public TextView tryGameTxt;

        @Bind(R.id.key_note_layout)
        public RelativeLayout keynoteLayout;
        @Bind(R.id.empty_profile_tv)
        public TextView emptyProfileTV;
        @Bind(R.id.avatar_image)
        public CircleImageView avatar;
        @Bind(R.id.title_text_keynote)
        public TextView keynoteTitleTxt;
        @Bind(R.id.sub_title_text_keynote)
        public TextView keynoteSubTitleTxt;
        @Bind(R.id.full_profile_layout)
        public RelativeLayout fullProfileLayout;
        @Bind(R.id.name_text)
        public TextView nameTxt;
        @Bind(R.id.description_text)
        public TextView descriptionTxt;

        @Bind(R.id.other_layout)
        public LinearLayout otherLayout;
        @Bind(R.id.title_text_other)
        public TextView otherTitleTxt;
        @Bind(R.id.description_text_other)
        public TextView otherDescriptionTxt;


        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            UIHelper uiHelper = UIHelper.getInstance();
            uiHelper.setBoldTypeface(titleTxt);
            uiHelper.setBoldTypeface(teaBreakTitleTxt);
            uiHelper.setBoldTypeface(keynoteTitleTxt);
            uiHelper.setBoldTypeface(otherTitleTxt);
            uiHelper.setBoldTypeface(timeTxt);
            uiHelper.setTypeface(roomNoTxt, subTitleTxt, keynoteSubTitleTxt,
                    sessionRatingNoTxt, sessionRatingTxt, sessionAnswerNoTxt
                    , sessionAnswerTxt, sessionCommentNoTxt, sessionCommentTxt,
                    tryGameTxt, nameTxt, descriptionTxt, otherDescriptionTxt);
            checkLayout.setOnClickListener(this);
            keynoteLayout.setOnClickListener(this);
            game0.setOnClickListener(this);
            game1.setOnClickListener(this);
            game2.setOnClickListener(this);
        }

        private void setData(AgendaEvent event){
            this.event = event;
            timeTxt.setText(event.getStartTimeStr());
            checkInBtn.setSelected(event.isChecked());
//            String likeString = " like";
//            if(event.getLikeCount() > 1){
//                likeString += "s";
//            }
//            likesTxt.setText(event.getLikeCount() + likeString);
            roomNoTxt.setVisibility(View.VISIBLE);
            roomNoTxt.setText(event.getRoom());
            String eventType = event.getEventType();
            if (event.isCurrentEvent()){
                background100.setVisibility(View.VISIBLE);
                background60.setVisibility(View.GONE);
            } else {
                background100.setVisibility(View.GONE);
                background60.setVisibility(View.VISIBLE);
            }
            if (AgendaService.KEYNOTE.equals(eventType)){
                keynoteTitleTxt.setText(event.getTitle());
                keynoteSubTitleTxt.setText(event.getDescription());
                AttendeeEntity attendeeEntity = event.getKeynoteUser();
                if (attendeeEntity != null){
                    nameTxt.setText(attendeeEntity.getName());
                    descriptionTxt.setText(attendeeEntity.getDescription());
                    UploadFileService.getInstance().loadAvatar(avatar, attendeeEntity.getAvatar(),
                            emptyProfileTV, attendeeEntity.getName());
                    fullProfileLayout.setVisibility(View.VISIBLE);
                } else {
                    fullProfileLayout.setVisibility(View.GONE);
                }
                keynoteLayout.setVisibility(View.VISIBLE);
                teabreakLayout.setVisibility(View.GONE);
                sessionLayout.setVisibility(View.GONE);
                topicLayout.setVisibility(View.GONE);
                otherLayout.setVisibility(View.GONE);
            } else if (AgendaService.BREAK.equals(eventType)){
                teaBreakTitleTxt.setText(event.getTitle());
                List<GameEventEntity> games = event.getGames();
                if (games == null || games.isEmpty()){
                    gameLayout.setVisibility(View.GONE);
                } else{
                    gameLayout.setVisibility(View.VISIBLE);
                    UploadFileService.getInstance()
                            .loadImageURL(game0, R.drawable.default_game, games.get(0).getImage(), 96, 96);
                    if (games.size() == 1){
                        game1.setVisibility(View.GONE);
                        game2.setVisibility(View.GONE);
                    } else {
                        UploadFileService.getInstance()
                                .loadImageURL(game1, R.drawable.default_game, games.get(1).getImage(), 96, 96);
                        if (games.size() == 2){
                            game2.setVisibility(View.GONE);
                        } else {
                            UploadFileService.getInstance()
                                    .loadImageURL(game2, R.drawable.default_game, games.get(2).getImage(), 96, 96);
                        }
                    }
                }
                keynoteLayout.setVisibility(View.GONE);
                teabreakLayout.setVisibility(View.VISIBLE);
                sessionLayout.setVisibility(View.GONE);
                topicLayout.setVisibility(View.GONE);
                otherLayout.setVisibility(View.GONE);
            } else if (AgendaService.MULTIPLE.equals(eventType)){
                titleTxt.setText(event.getTitle());
                subTitleTxt.setText(event.getDescription());
                keynoteLayout.setVisibility(View.GONE);
                teabreakLayout.setVisibility(View.GONE);
                sessionLayout.setVisibility(View.VISIBLE);
                EventEntity eventEntity = event.getEventEntity();
                if (eventEntity.getCorrectAnswers() <= 0 && eventEntity.getCommentCount() <= 0
                        && eventEntity.getRating() <= 0){
                    ratingLayout.setVisibility(View.GONE);
                } else {
                    ratingLayout.setVisibility(View.VISIBLE);
                    sessionRatingNoTxt.setText(event.getRate());
                    sessionAnswerNoTxt.setText(event.getCorrectAnswerNo());
                    sessionCommentNoTxt.setText(event.getCommentCount());
                }

                if (event.getSubEvents() != null && event.getSubEvents().size() > 0){
                    roomNoTxt.setVisibility(View.VISIBLE);
                    topicLayout.setVisibility(View.VISIBLE);
                    topics.setAdapter(new AgendaTopicAdapter(context, event.getSubEvents(), listener, true));

                    final Handler mainHandler = new Handler(context.getMainLooper());
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    UtilityUI.setListViewHeightBasedOnChildren(topics);
                                }
                            }, 500);
                        }
                    });


                } else {
                    topicLayout.setVisibility(View.GONE);
                }
                otherLayout.setVisibility(View.GONE);
            } else if(AgendaService.WORKSHOP_MULTIPLE.equals(eventType)){
                keynoteTitleTxt.setText(event.getTitle());
                keynoteSubTitleTxt.setText(event.getDescription());
                keynoteLayout.setVisibility(View.VISIBLE);
                teabreakLayout.setVisibility(View.GONE);
                sessionLayout.setVisibility(View.GONE);
                keynoteLayout.setOnClickListener(this);
                fullProfileLayout.setVisibility(View.GONE);
//                if (event.getSubEvents() != null && event.getSubEvents().size() > 0){
//                    topicLayout.setVisibility(View.VISIBLE);
//                    topics.setAdapter(new AgendaTopicAdapter(context, event.getSubEvents(), listener, false));
//                    UtilityUI.setListViewHeightBasedOnChildren(topics);
//                } else {
                    topicLayout.setVisibility(View.GONE);
//                }
                otherLayout.setVisibility(View.GONE);
            }
            else {
                otherTitleTxt.setText(event.getTitle());
                otherDescriptionTxt.setText(event.getDescription());
                keynoteLayout.setVisibility(View.GONE);
                teabreakLayout.setVisibility(View.GONE);
                sessionLayout.setVisibility(View.GONE);
                topicLayout.setVisibility(View.GONE);
                otherLayout.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onClick(View v) {
            int id = v.getId();
            switch (id){
                case R.id.check_layout:
                    String likeString = " like";
//                    int likeCount = event.getLikeCount();
                    if (checkInBtn.isSelected()){
                        AgendaService.getInstance().updateMyAgenda(event, false);
                        checkInBtn.setSelected(false);
//                        likeCount--;
//                        if(likeCount> 1){
//                            likeString += "s";
//                        }
//                        likesTxt.setText(likeCount + likeString);
                        TrackingService.getInstance().sendTracking("113", "agenda", "bookmark",
                                TrackingService.getInstance().simpleDateFormatTracking.format(event.getStartTime()), Long.toString(event.getServerId()), "");
                    } else {
                        AgendaService.getInstance().updateMyAgenda(event, true);
                        checkInBtn.setSelected(true);
//                        likeCount++;
//                        if(likeCount> 1){
//                            likeString += "s";
//                        }
//                        likesTxt.setText(likeCount + likeString);
                        TrackingService.getInstance().sendTracking("105", "agenda", "bookmark", Long.toString(event.getServerId()), "", "");
                    }
                    if (AgendaPresenter.MY_AGENDA == AgendaPresenter.typeAgenda){
                        App.getInstance().getBus().post(new AgendaReloadingEvent());
                    }
                    break;
                case R.id.key_note_layout:
                    Flow.get(context).set(new AgendaTopicDetailScreen(event, null));
                    TrackingService.getInstance().sendTracking("104", "agenda", Long.toString(event.getServerId()), "", "", "");
                    break;
                case R.id.session_layout:
                    if (AgendaService.WORKSHOP_MULTIPLE.equals(event.getEventType())
                            && event.getSubEvents() != null && event.getSubEvents().size() > 0){
                        Flow.get(context).set(new AgendaTopicDetailScreen(event, null));
                    }
                    break;
                case R.id.game0_image:
                    GameEventEntity gameEventEntity0 = event.getGames().get(0);
                    GameService.getInstance().openGamePage(gameEventEntity0.getKeyWork(), gameEventEntity0.getGameId(), App.getInstance().getMainActivity());
                    TrackingService.getInstance().sendTracking("122", "agenda", Long.toString(event.getServerId()),
                            Long.toString(gameEventEntity0.getGameId()), "", "");
                    break;
                case R.id.game1_image:
                    GameEventEntity gameEventEntity1 = event.getGames().get(1);
                    GameService.getInstance().openGamePage(gameEventEntity1.getKeyWork(), gameEventEntity1.getGameId(), App.getInstance().getMainActivity());
                    TrackingService.getInstance().sendTracking("122", "agenda", Long.toString(event.getServerId()),
                            Long.toString(gameEventEntity1.getGameId()), "", "");
                    break;
                case R.id.game2_image:
                    GameEventEntity gameEventEntity2 = event.getGames().get(2);
                    GameService.getInstance().openGamePage(gameEventEntity2.getKeyWork(), gameEventEntity2.getGameId(), App.getInstance().getMainActivity());
                    TrackingService.getInstance().sendTracking("122", "agenda", Long.toString(event.getServerId()),
                            Long.toString(gameEventEntity2.getGameId()), "", "");
                    break;
            }
        }
    }

    public class HeaderViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.time_header)
        public TextView time;
        public HeaderViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            UIHelper.getInstance().setTypeface(time);
        }
    }

    public interface AgendaListener{
        void goToDetailPage(EventEntity subEvent);
    }
}
