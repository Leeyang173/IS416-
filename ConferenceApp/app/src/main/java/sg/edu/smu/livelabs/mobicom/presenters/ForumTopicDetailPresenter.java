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
import sg.edu.smu.livelabs.mobicom.busEvents.BadgeNotiEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.DeleteCommentEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.NewCommentEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.UpdateCommentEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.UpdateTopicEvent;
import sg.edu.smu.livelabs.mobicom.flow.Layout;
import sg.edu.smu.livelabs.mobicom.models.data.CommentEntity;
import sg.edu.smu.livelabs.mobicom.models.data.TopicEntity;
import sg.edu.smu.livelabs.mobicom.services.ForumService;
import sg.edu.smu.livelabs.mobicom.services.MasterPointService;
import sg.edu.smu.livelabs.mobicom.views.ForumTopicDetailHeaderView;
import sg.edu.smu.livelabs.mobicom.views.ForumTopicDetailView;

/**
 * Created by smu on 23/4/16.
 */
@AutoScreen(
        component = @AutoComponent(dependencies = MainActivity.class, superinterfaces = AppDependencies.class),
        screenAnnotations = Layout.class
)
@DaggerScope(ForumTopicDetailPresenter.class)
@Layout(R.layout.forum_topic_detail_view)
public class ForumTopicDetailPresenter extends ViewPresenter<ForumTopicDetailView> implements View.OnClickListener{

    private Context context;
    private ActionBarOwner actionBar;
    private AgendaCommentAdapter agendaCommentAdapter;
    private Bus bus;
    private MainActivity mainActivity;
    private TopicEntity topicEntity;
    private ForumTopicDetailHeaderView topicHeaderView;
    private Handler handler;
    private Runnable runnable;

    public ForumTopicDetailPresenter(MainActivity mainActivity, ActionBarOwner actionBar, Bus bus,
                                     @ScreenParam TopicEntity topicEntity){
        this.actionBar = actionBar;
        this.bus = bus;
        this.mainActivity = mainActivity;
        this.topicEntity = topicEntity;
    }

    @Override
    protected void onLoad(Bundle savedInstanceState) {
        super.onLoad(savedInstanceState);
        context = getView().getContext();
        if (!hasView()) return;
        actionBar.setConfig(new ActionBarOwner.Config(true, "Forum", null));
        agendaCommentAdapter = new AgendaCommentAdapter(context, false);
        final UltimateRecyclerView ultimateRecyclerView = getView().listView;

        final View headerView = LayoutInflater.from(context).inflate(R.layout.forum_topic_detail_header, null);
        ultimateRecyclerView.setNormalHeader(headerView);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        ultimateRecyclerView.setLayoutManager(linearLayoutManager);
        ultimateRecyclerView.setAdapter(agendaCommentAdapter);
        StickyRecyclerHeadersDecoration headersDecor = new StickyRecyclerHeadersDecoration(agendaCommentAdapter);
        ultimateRecyclerView.addItemDecoration(headersDecor);

        ultimateRecyclerView.disableLoadmore();

        ultimateRecyclerView.setHasFixedSize(false);
        topicHeaderView = new ForumTopicDetailHeaderView(headerView, context);
        topicHeaderView.loadData(topicEntity);
        headerView.setLayoutParams(new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT));
        ultimateRecyclerView.setOnParallaxScroll(new UltimateRecyclerView.OnParallaxScroll() {
            @Override
            public void onParallaxScroll(float percentage, float offset, View parallax) {

            }
        });
        ultimateRecyclerView.setDefaultOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                handler.removeCallbacks(runnable);
                handler.post(runnable);
            }
        });
        getView().enterComment.setOnClickListener(this);
        getView().mainActivity = mainActivity;
        getView().adapter = agendaCommentAdapter;
        refreshLocalData();
        UIHelper.getInstance().showProgressDialog(context, context.getString(R.string.progressing), true);
        refreshRemoteData();
//        topicHeaderView.likeBtn.setOnClickListener(this);
        getView().enterComment.setOnClickListener(this);
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                ForumService.getInstance().SyncATopic(topicEntity.getTopicHandle());
                ForumService.getInstance().getTopicComments(topicEntity.getTopicHandle());
            }
        };
    }

    @Override
    public void onClick(View v) {
        try{
            int id = v.getId();
            switch (id){
                case R.id.enter_comment:
                    String comment = getView().commentEdit.getText().toString().trim();
                    if (!comment.isEmpty()){
                        if (MainActivity.mode == MainActivity.OFFLINE_MODE){
                            UIHelper.getInstance().showAlert(context, context.getString(R.string.no_internet_connection1));
                            return;
                        }
                        UIHelper.getInstance().showProgressDialog(context, context.getString(R.string.progressing), true);
                        ForumService.getInstance().addCommentToSP(topicEntity.getTopicHandle(), comment);
                    }
                    break;
                case R.id.likeBtn:
                    //TODO
                    break;
            }
        }catch (Exception e){
            Log.d(App.appName, "Agenda topic detail " + e.toString());
        }
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

    @Subscribe
    public void addNewCommentEvent(NewCommentEvent newCommentEvent){
        UIHelper.getInstance().dismissProgressDialog();
        if (newCommentEvent.success){
            getView().commentEdit.setText("");
            refreshLocalData();
        } else {
            UIHelper.getInstance().showAlert(context, "Unable to add comment. Please try again!");
        }
    }

    @Subscribe
    public void deleteCommentEvent(DeleteCommentEvent deleteCommentEvent){
        UIHelper.getInstance().dismissProgressDialog();
        if (deleteCommentEvent.isSuccess){
            refreshLocalData();
        } else {
            UIHelper.getInstance().showAlert(context, "Unable to add comment. Please try again!");
        }
    }

    @Subscribe
    public void updateTopicComments(UpdateCommentEvent newCommentEvent){
        refreshLocalData();
        UIHelper.getInstance().dismissProgressDialog();
        getView().listView.setRefreshing(false);
//        handler.removeCallbacks(runnable);
//        handler.postDelayed(runnable, 5000);
    }

    @Subscribe
    public void updateATopic(UpdateTopicEvent updateTopicEvent){
        topicHeaderView.loadData(topicEntity);
        UIHelper.getInstance().dismissProgressDialog();
        getView().listView.setRefreshing(false);
    }

    private void refreshLocalData() {
        List<CommentEntity> commentEntities = ForumService.getInstance().getComments(topicEntity.getTopicHandle());
        agendaCommentAdapter.setData(commentEntities);
    }

    private void refreshRemoteData() {
        ForumService.getInstance().SyncATopic(topicEntity.getTopicHandle());
        ForumService.getInstance().getTopicComments(topicEntity.getTopicHandle());
    }

    @Subscribe
    public void badgeNotiEvent(BadgeNotiEvent event){
        MasterPointService.getInstance().getBadgesAPI();
        MasterPointService.getInstance().showToolTips(getView().listView, event.badgeName);
    }
}
