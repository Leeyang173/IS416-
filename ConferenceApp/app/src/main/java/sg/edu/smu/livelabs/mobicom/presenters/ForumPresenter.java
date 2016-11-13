package sg.edu.smu.livelabs.mobicom.presenters;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import autodagger.AutoComponent;
import automortar.AutoScreen;
import mortar.MortarScope;
import mortar.ViewPresenter;
import sg.edu.smu.livelabs.mobicom.ActionBarOwner;
import sg.edu.smu.livelabs.mobicom.App;
import sg.edu.smu.livelabs.mobicom.AppDependencies;
import sg.edu.smu.livelabs.mobicom.DaggerScope;
import sg.edu.smu.livelabs.mobicom.MainActivity;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.UIHelper;
import sg.edu.smu.livelabs.mobicom.adapters.ForumAdapter;
import sg.edu.smu.livelabs.mobicom.busEvents.DeleteTopicEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.NewTopicEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.RefreshTopicEvent;
import sg.edu.smu.livelabs.mobicom.flow.Layout;
import sg.edu.smu.livelabs.mobicom.net.response.SimpleResponse;
import sg.edu.smu.livelabs.mobicom.services.AgendaService;
import sg.edu.smu.livelabs.mobicom.services.ForumService;
import sg.edu.smu.livelabs.mobicom.views.ForumView;

/**
 * Created by smu on 23/4/16.
 */
@AutoScreen(
        component = @AutoComponent(dependencies = MainActivity.class, superinterfaces = AppDependencies.class),
        screenAnnotations = Layout.class
)
@DaggerScope(ForumPresenter.class)
@Layout(R.layout.forum_view)
public class ForumPresenter extends ViewPresenter<ForumView> implements View.OnClickListener{
    private ActionBarOwner actionBarOwner;
    private MainActivity mainActivity;
    private Bus bus;
    private ForumAdapter adapter;
    private Context context;
    private Handler handler;
    private Runnable runnable;
    public ForumPresenter(MainActivity mainActivity, ActionBarOwner actionBarOwner, Bus bus){
        this.actionBarOwner = actionBarOwner;
        this.mainActivity = mainActivity;
        this.bus = bus;
    }

    @Override
    protected void onLoad(Bundle savedInstanceState) {
        super.onLoad(savedInstanceState);
        if (!hasView()) return;
        mainActivity.currentTab = MainActivity.OTHER_TAB;
        context = getView().getContext();
        actionBarOwner.setConfig(new ActionBarOwner.Config(true, "Forum", null));
        adapter = new ForumAdapter(context);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        getView().listView.setLayoutManager(linearLayoutManager);
        getView().listView.setAdapter(adapter);
        getView().listView.setHasFixedSize(false);
        getView().listView.disableLoadmore();
        getView().listView.setDefaultOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                AgendaService.getInstance().getMainTopic();
                if (ForumService.getInstance().topicCursor == null){
                    ForumService.getInstance().getTopics();
                }
                getView().searchText.setText("");
            }
        });
        getView().searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                handler.removeCallbacks(runnable);
                handler.postDelayed(runnable, 1000);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    String key = getView().searchText.getText().toString();
                    if (key.isEmpty()){
                        adapter.setData(ForumService.getInstance().getAllTopic());
                    } else {
                        adapter.setData(ForumService.getInstance().searchTopic(key));
                    }
                }catch (Exception e){
                    Log.d(App.APP_TAG, "Forum present search exception " + e.toString());
                }

            }
        };
        adapter.setData(ForumService.getInstance().getAllTopic());
        getView().newBtn.setOnClickListener(this);
        getView().submitTopic.setOnClickListener(this);
        getView().overlayView.setOnClickListener(this);
        getView().newTopicLayout.setVisibility(View.GONE);
    }

    @Override
    protected void onEnterScope(MortarScope scope) {
        super.onEnterScope(scope);
        bus.register(this);
    }

    @Override
    protected void onExitScope() {
        adapter.closeData();
        bus.unregister(this);
        super.onExitScope();

    }

    @Subscribe
    public void newTopicEvent(NewTopicEvent newTopicEvent){
        UIHelper.getInstance().dismissProgressDialog();
        if (newTopicEvent.success){
            adapter.setData(ForumService.getInstance().getAllTopic());
        } else {
            UIHelper.getInstance().showAlert(context, "Unable to add a topic. Please try again.");
        }
    }

    @Subscribe
    public void deleteTopicEvent(DeleteTopicEvent event){
        UIHelper.getInstance().dismissProgressDialog();
        if (event.isSuccess){
            adapter.setData(ForumService.getInstance().getAllTopic());
        } else {
            UIHelper.getInstance().showAlert(context, "Unable to delete a topic. Please try again.");
        }
    }

    @Subscribe
    public void updateTopicEvent(RefreshTopicEvent newTopicEvent){
        adapter.setData(ForumService.getInstance().getAllTopic());
        UIHelper.getInstance().dismissProgressDialog();
        getView().listView.setRefreshing(false);
    }

    //moderator delete a topic
    @Subscribe
    public void updateTopic(SimpleResponse simpleResponse){
        if ("success".equals(simpleResponse.status)){
            adapter.setData(ForumService.getInstance().getAllTopic());
        } else {
            if (simpleResponse.details == null){
                simpleResponse.details = "Unable to delete topic. Please try again.";
            }
            UIHelper.getInstance().showAlert(context, simpleResponse.details);
        }
        UIHelper.getInstance().dismissProgressDialog();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id){
            case R.id.new_layout:
                getView().newTopicLayout.setVisibility(View.VISIBLE);
                mainActivity.setVisibleBottombar(View.GONE);
                getView().topicEdit.requestFocus();
                InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(getView().topicEdit, InputMethodManager.SHOW_IMPLICIT);
                break;
            case R.id.enter_topic:

                try {
                    String topicStr = getView().topicEdit.getText().toString();
                    if (!topicStr.isEmpty()){
                        if (MainActivity.mode == MainActivity.OFFLINE_MODE){
                            UIHelper.getInstance().showAlert(context, context.getString(R.string.no_internet_connection1));
                            return;
                        }
                        UIHelper.getInstance().showProgressDialog(context,
                                context.getString(R.string.progressing), true);
                        ForumService.getInstance().addTopicSP(topicStr);
                    }
                    getView().topicEdit.clearFocus();
                    InputMethodManager imm1 = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm1.showSoftInput(getView().topicEdit, InputMethodManager.HIDE_NOT_ALWAYS);
                    getView().newTopicLayout.setVisibility(View.GONE);
                    mainActivity.setVisibleBottombar(View.VISIBLE);
                } catch (Exception e){
                    Log.d(App.APP_TAG, e.toString());
                }

                break;
            case R.id.overlay_view:
                getView().topicEdit.clearFocus();
                InputMethodManager imm2 = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm2.showSoftInput(getView().topicEdit, InputMethodManager.HIDE_NOT_ALWAYS);
                getView().newTopicLayout.setVisibility(View.GONE);
                mainActivity.setVisibleBottombar(View.VISIBLE);
                break;

        }
    }
}
