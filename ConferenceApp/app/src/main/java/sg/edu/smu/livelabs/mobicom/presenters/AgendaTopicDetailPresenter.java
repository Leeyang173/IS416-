package sg.edu.smu.livelabs.mobicom.presenters;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import com.marshalchen.ultimaterecyclerview.UltimateRecyclerView;
import com.marshalchen.ultimaterecyclerview.stickyheadersrecyclerview.StickyRecyclerHeadersDecoration;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.List;

import autodagger.AutoComponent;
import automortar.AutoScreen;
import automortar.ScreenParam;
import flow.Flow;
import mortar.MortarScope;
import mortar.ViewPresenter;
import sg.edu.smu.livelabs.mobicom.ActionBarOwner;
import sg.edu.smu.livelabs.mobicom.App;
import sg.edu.smu.livelabs.mobicom.AppDependencies;
import sg.edu.smu.livelabs.mobicom.DaggerScope;
import sg.edu.smu.livelabs.mobicom.MainActivity;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.UIHelper;
import sg.edu.smu.livelabs.mobicom.adapters.AgendaCommentAdapter;
import sg.edu.smu.livelabs.mobicom.busEvents.AgendaReloadingEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.AgendaUpdatedTopicEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.BadgeNotiEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.DeleteCommentEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.NewCommentEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.UpdateCommentEvent;
import sg.edu.smu.livelabs.mobicom.flow.Layout;
import sg.edu.smu.livelabs.mobicom.models.AgendaEvent;
import sg.edu.smu.livelabs.mobicom.models.data.AttendeeEntity;
import sg.edu.smu.livelabs.mobicom.models.data.CommentEntity;
import sg.edu.smu.livelabs.mobicom.models.data.EventEntity;
import sg.edu.smu.livelabs.mobicom.models.data.PaperEventEntity;
//import sg.edu.smu.livelabs.mobicom.presenters.screen.QuizScreen;
import sg.edu.smu.livelabs.mobicom.presenters.screen.QuizScreen;
import sg.edu.smu.livelabs.mobicom.services.AgendaService;
import sg.edu.smu.livelabs.mobicom.services.ForumService;
import sg.edu.smu.livelabs.mobicom.services.MasterPointService;
import sg.edu.smu.livelabs.mobicom.services.TrackingService;
import sg.edu.smu.livelabs.mobicom.views.AgendaKeynoteHeaderView;
import sg.edu.smu.livelabs.mobicom.views.AgendaTopicDetailHeaderView;
import sg.edu.smu.livelabs.mobicom.views.AgendaTopicDetailView;
import sg.edu.smu.livelabs.mobicom.views.AgendaWorkshopHeaderView;
import sg.edu.smu.livelabs.mobicom.views.UserInfoPopup;

/**
 * Created by smu on 1/3/16.
 */
@AutoScreen(
        component = @AutoComponent(dependencies = MainActivity.class, superinterfaces = AppDependencies.class),
        screenAnnotations = Layout.class
)
@DaggerScope(AgendaTopicDetailPresenter.class)
@Layout(R.layout.agenda_topic_detail_view)
public class AgendaTopicDetailPresenter extends ViewPresenter<AgendaTopicDetailView> implements View.OnClickListener{

    private Bus bus;
    private AgendaCommentAdapter agendaCommentAdapter;
    private Context context;
    private MainActivity mainActivity;
    private AgendaEvent agendaEvent;
    private EventEntity eventEntity;
    private ActionBarOwner actionBar;
    private PaperEventEntity paperEventEntity;
    private AgendaTopicDetailHeaderView paperView;
    private AgendaKeynoteHeaderView keynoteView;
    private AgendaWorkshopHeaderView workshopHeaderView;
    private View headerView;
    private String topicHandler;
    private Handler handler;
    private Runnable runnable;
    private long serverEventID;

    public AgendaTopicDetailPresenter(Bus bus, MainActivity mainActivity, ActionBarOwner actionBarOwner,
                                      @ScreenParam AgendaEvent agendaEvent, @ScreenParam EventEntity eventEntity){
        this.bus = bus;
        this.agendaEvent = agendaEvent;
        this.eventEntity = eventEntity;
        this.mainActivity = mainActivity;
        this.actionBar = actionBarOwner;
    }

    @Override
    protected void onLoad(Bundle savedInstanceState) {
        super.onLoad(savedInstanceState);
        if (!hasView()) return;
        context = getView().getContext();
        handler = new Handler();
        actionBar.setConfig(new ActionBarOwner.Config(true, context.getString(R.string.agenda), null));
        agendaCommentAdapter = new AgendaCommentAdapter(context, true);
        UltimateRecyclerView ultimateRecyclerView = getView().listView;
        if (AgendaService.KEYNOTE.equals(agendaEvent.getEventType())){
            topicHandler = agendaEvent.getEventEntity().getTopicHandle();
            headerView = LayoutInflater.from(context).inflate(R.layout.agenda_keynote_header_view, null);
            keynoteView = new AgendaKeynoteHeaderView(context, headerView);
            keynoteView.setData(agendaEvent);
//            keynoteView.userLayout.setOnClickListener(this);
            serverEventID = agendaEvent.getServerId();
            ForumService.getInstance().getTopicComments(topicHandler);
            runnable = new Runnable() {
                @Override
                public void run() {
                    ForumService.getInstance().getTopicComments(topicHandler);
                }
            };
        } else if (AgendaService.WORKSHOP_MULTIPLE.equals(agendaEvent.getEventType())){
            topicHandler = agendaEvent.getEventEntity().getTopicHandle();
            headerView = LayoutInflater.from(context).inflate(R.layout.agenda_workshop_header, null);
            workshopHeaderView = new AgendaWorkshopHeaderView(context, headerView);
            workshopHeaderView.setData(agendaEvent);
            ForumService.getInstance().getTopicComments(topicHandler);
            runnable = new Runnable() {
                @Override
                public void run() {
                    ForumService.getInstance().getTopicComments(topicHandler);
                }
            };
            serverEventID = agendaEvent.getServerId();
        } else {
            headerView = LayoutInflater.from(context).inflate(R.layout.agenda_topic_detail_header, null);
            paperView = new AgendaTopicDetailHeaderView(headerView, context);
            paperEventEntity = AgendaService.getInstance()
                    .getPaperEventByServerId(eventEntity.getPaperId());
            paperView.loadData(agendaEvent, eventEntity, paperEventEntity);
            paperView.pdfImg.setOnClickListener(this);
            paperView.epubImg.setOnClickListener(this);
            paperView.quizImg.setOnClickListener(this);
            paperView.quizTxt.setOnClickListener(this);
            setSelectedStar(eventEntity.getMyRate());
            paperView.star1.setOnClickListener(this);
            paperView.star2.setOnClickListener(this);
            paperView.star3.setOnClickListener(this);
            paperView.star4.setOnClickListener(this);
            paperView.star5.setOnClickListener(this);
            serverEventID = eventEntity.getServerId();
            topicHandler = eventEntity.getTopicHandle();
            refreshLocalData();
            AgendaService.getInstance().getRating(eventEntity);
            ForumService.getInstance().getTopicComments(topicHandler);
            runnable = new Runnable() {
                @Override
                public void run() {
                    AgendaService.getInstance().getRating(eventEntity);
                    ForumService.getInstance().getTopicComments(topicHandler);
                }
            };
        }
        ultimateRecyclerView.setNormalHeader(headerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        ultimateRecyclerView.setLayoutManager(linearLayoutManager);
        ultimateRecyclerView.setAdapter(agendaCommentAdapter);
        StickyRecyclerHeadersDecoration headersDecor = new StickyRecyclerHeadersDecoration(agendaCommentAdapter);
        ultimateRecyclerView.addItemDecoration(headersDecor);

        ultimateRecyclerView.disableLoadmore();

        ultimateRecyclerView.setDefaultOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()  {
            @Override
            public void onRefresh() {
                handler.removeCallbacks(runnable);
                handler.post(runnable);
            }
        });

        ultimateRecyclerView.setHasFixedSize(false);

        headerView.setLayoutParams(new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT));
        ultimateRecyclerView.setOnParallaxScroll(new UltimateRecyclerView.OnParallaxScroll() {
            @Override
            public void onParallaxScroll(float percentage, float offset, View parallax) {

            }
        });
        getView().enterComment.setOnClickListener(this);
        getView().mainActivity = mainActivity;
        getView().adapter = agendaCommentAdapter;

    }

    @Override
    protected void onEnterScope(MortarScope scope) {
        super.onEnterScope(scope);
        bus.register(this);
        mainActivity.setVisibleBottombar(View.GONE);
    }

    @Override
    protected void onExitScope() {
        bus.unregister(this);
        mainActivity.setVisibleBottombar(View.VISIBLE);
        super.onExitScope();
    }

    @Override
    public void onClick(View v) {
        try{
            int id = v.getId();
            switch (id){
                case R.id.star1:
                    AgendaService.getInstance().rateSubEvent(eventEntity, 1);
                    break;
                case R.id.star2:
                    AgendaService.getInstance().rateSubEvent(eventEntity, 2);
                    break;
                case R.id.star3:
                    AgendaService.getInstance().rateSubEvent(eventEntity, 3);
                    break;
                case R.id.star4:
                    AgendaService.getInstance().rateSubEvent(eventEntity, 4);
                    break;
                case R.id.star5:
                    AgendaService.getInstance().rateSubEvent(eventEntity, 5);
                    break;
                case R.id.pdf_image:
                    UIHelper.getInstance().openPDF(paperEventEntity.getPdfLink(), mainActivity);
                    TrackingService.getInstance().sendTracking("106", "agenda", Long.toString(eventEntity.getServerId()), "pdf", Long.toString(eventEntity.getPaperId()), "");
                    break;
                case R.id.epub_image:
                    UIHelper.getInstance().openEpub(paperEventEntity.getEpubLink(), mainActivity);
                    TrackingService.getInstance().sendTracking("107", "agenda",
                            Long.toString(eventEntity.getServerId()), "epub", Long.toString(eventEntity.getPaperId()), "");
                    break;
                case R.id.quiz_txt:
                case R.id.quiz_image:
                    Flow.get(context).set(new QuizScreen(eventEntity.getServerId()));
                    break;
                case R.id.enter_comment:
                    String comment = getView().commentEdit.getText().toString().trim();
                    if (!comment.isEmpty()){
                        if (MainActivity.mode == MainActivity.OFFLINE_MODE){
                            UIHelper.getInstance().showAlert(context, context.getString(R.string.no_internet_connection1));
                            return;
                        }
                        UIHelper.getInstance().showProgressDialog(context, context.getString(R.string.progressing), true);
                        ForumService.getInstance().addCommentToSP(topicHandler, comment);
                    }
                    break;
                case R.id.user_layout:
                    AttendeeEntity attendeeEntity = agendaEvent.getKeynoteUser();
                    if (attendeeEntity != null){
                        new UserInfoPopup(context, attendeeEntity);
                    }
                    break;
                default:
//                Object tag = v.getTag();
//                if (tag != null && tag instanceof Number){
//                    int index = ((Number) tag).intValue() - AgendaTopicDetailView.START_AUTHOR_INDEX;
//                    if (index > 0 && index < authors.size()){
//                        showUserPopup(authors.get(index));
//                    }
//                }
            }
        }catch (Exception e){
            Log.d(App.appName, "Agenda topic detail " + e.toString());
        }

    }

    @Subscribe
    public void UpdateTopic(AgendaUpdatedTopicEvent event){
        switch (event.type){
            case AgendaUpdatedTopicEvent.RATE_TOPIC:
                setSelectedStar(eventEntity.getMyRate());
                String str = String.format("%.1f", eventEntity.getRating());
                paperView.averageRateTxt.setText(str);
                break;
            default:
                //TODO if change something else, reload Data
        }
        UIHelper.getInstance().dismissProgressDialog();
    }

    @Subscribe
    public void reloadHeader(AgendaReloadingEvent agendaReloadingEvent){
        if (AgendaService.MULTIPLE.equals(agendaEvent.getEventType())){
            eventEntity = AgendaService.getInstance().getEventByServerId(eventEntity.getServerId());
            paperView.loadData(agendaEvent, eventEntity, paperEventEntity);
        }

    }

    @Subscribe
    public void deleteCommentEvent(DeleteCommentEvent deleteCommentEvent){
        UIHelper.getInstance().dismissProgressDialog();
        if (deleteCommentEvent.isSuccess){
            refreshLocalData();
        } else {
            UIHelper.getInstance().showAlert(context, "Unable to delete comment. Please try again.");
        }
    }

    @Subscribe
    public void addNewCommentEvent(NewCommentEvent newCommentEvent){
        UIHelper.getInstance().dismissProgressDialog();
        if (newCommentEvent.success){
            getView().commentEdit.setText("");
            refreshLocalData();
            TrackingService.getInstance().sendTracking("109", "agenda", String.valueOf(serverEventID),"comment", "comment", "");
        } else {
            UIHelper.getInstance().showAlert(context, "Unable to add comment. Please try again.");
        }
    }

    @Subscribe
    public void updateTopicComments(UpdateCommentEvent updateCommentEvent){
        UIHelper.getInstance().dismissProgressDialog();
        getView().listView.setRefreshing(false);
        if (updateCommentEvent.isSuccess){
            refreshLocalData();
        }
//        if (runnable != null){
//            handler.removeCallbacks(runnable);
//            handler.postDelayed(runnable, 5000);
//        }
    }

    private void refreshLocalData() {
        List<CommentEntity> commentEntities;
        if (AgendaService.MULTIPLE.equals(agendaEvent.getEventType())){
            commentEntities = ForumService.getInstance().getComments(eventEntity.getTopicHandle());
            if (commentEntities == null || commentEntities.isEmpty()){
                paperView.beFirstTV.setVisibility(View.VISIBLE);
                agendaCommentAdapter.setData(commentEntities);
            } else {
                paperView.beFirstTV.setVisibility(View.GONE);
                agendaCommentAdapter.setData(commentEntities);
            }
        } else if(AgendaService.KEYNOTE.equals(agendaEvent.getEventType())){
            commentEntities = ForumService.getInstance().getComments(agendaEvent.getEventEntity().getTopicHandle());
            if (commentEntities == null || commentEntities.isEmpty()){
                keynoteView.beFirstTV.setVisibility(View.VISIBLE);
                agendaCommentAdapter.setData(commentEntities);
            } else {
                keynoteView.beFirstTV.setVisibility(View.GONE);
                agendaCommentAdapter.setData(commentEntities);
            }
        } else {
            commentEntities = ForumService.getInstance().getComments(agendaEvent.getEventEntity().getTopicHandle());
            if (commentEntities == null || commentEntities.isEmpty()){
                workshopHeaderView.beFirstTV.setVisibility(View.VISIBLE);
                agendaCommentAdapter.setData(commentEntities);
            } else {
                workshopHeaderView.beFirstTV.setVisibility(View.GONE);
                agendaCommentAdapter.setData(commentEntities);
            }
        }

    }

    private void setSelectedStar(int rate){
        if (rate > 5){
            rate = 5;
        }
        for (int i = 0; i < rate; i++){
            paperView.stars[i].setSelected(true);
        }
        for (int i = rate; i < 5; i++){
            paperView.stars[i].setSelected(false);
        }
    }


    @Subscribe
    public void badgeNotiEvent(BadgeNotiEvent event){
        MasterPointService.getInstance().getBadgesAPI();
        MasterPointService.getInstance().showToolTips(getView().listView, event.badgeName);
    }
}
