package sg.edu.smu.livelabs.mobicom.presenters;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;

import com.marshalchen.ultimaterecyclerview.stickyheadersrecyclerview.StickyRecyclerHeadersDecoration;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import autodagger.AutoComponent;
import automortar.AutoScreen;
import flow.Flow;
import jp.wasabeef.recyclerview.animators.FlipInTopXAnimator;
import mortar.MortarScope;
import mortar.ViewPresenter;
import rx.functions.Action0;
import sg.edu.smu.livelabs.mobicom.ActionBarOwner;
import sg.edu.smu.livelabs.mobicom.App;
import sg.edu.smu.livelabs.mobicom.AppDependencies;
import sg.edu.smu.livelabs.mobicom.DaggerScope;
import sg.edu.smu.livelabs.mobicom.MainActivity;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.UIHelper;
import sg.edu.smu.livelabs.mobicom.adapters.AgendaPaperAdapter;
import sg.edu.smu.livelabs.mobicom.busEvents.AgendaPaperReloadEvent;
import sg.edu.smu.livelabs.mobicom.flow.Layout;
import sg.edu.smu.livelabs.mobicom.presenters.screen.AgendaScreen;
import sg.edu.smu.livelabs.mobicom.presenters.screen.AgendaPaperScreen;
import sg.edu.smu.livelabs.mobicom.services.AgendaService;
import sg.edu.smu.livelabs.mobicom.services.TrackingService;
import sg.edu.smu.livelabs.mobicom.views.AgendaPaperView;

/**
 * Created by smu on 14/4/16.
 */
@AutoScreen(
        component = @AutoComponent(dependencies = MainActivity.class, superinterfaces = AppDependencies.class),
        screenAnnotations = Layout.class
)
@DaggerScope(AgendaPaperPresenter.class)
@Layout(R.layout.agenda_paper_view)
public class AgendaPaperPresenter extends ViewPresenter<AgendaPaperView> implements AgendaPaperAdapter.AgendaPaperListener {

    public Context context;
    private AgendaPaperAdapter adapter;
    private MainActivity mainActivity;
    private ActionBarOwner actionBarOwner;
    private Bus bus;

    public AgendaPaperPresenter(Bus bus, MainActivity mainActivity, ActionBarOwner actionBarOwner){
        this.mainActivity = mainActivity;
        this.actionBarOwner = actionBarOwner;
        this.bus = bus;
    }

    @Override
    protected void onEnterScope(MortarScope scope) {
        super.onEnterScope(scope);
        bus.register(this);
        if(App.getInstance().currentPresenter.equals(MorePresenter.NAME)){
            App.getInstance().setPrevious();
            App.getInstance().currentPresenter = "PaperPresenter";
        }
    }

    @Override
    protected void onExitScope() {
        super.onExitScope();
        bus.unregister(this);
        App.getInstance().previousPresenter = "";
    }

    @Override
    protected void onLoad(Bundle savedInstanceState) {
        super.onLoad(savedInstanceState);
        if (!hasView()) return;
        context = getView().getContext();
        //enable bottom bar
        mainActivity.messageBtn.setSelected(false);
        mainActivity.gamesBtn.setSelected(false);
        mainActivity.agendaBtn.setSelected(true);
        mainActivity.homeBtn.setSelected(false);
        mainActivity.moreBtn.setSelected(false);
        mainActivity.currentTab = MainActivity.AGENDA_TAB;
        //
        actionBarOwner.setConfig(new ActionBarOwner.Config(true,
                new ActionBarOwner.MenuAction("Bookmark", new Action0() {
                    @Override
                    public void call() {
                        Flow.get(context).set(new AgendaScreen(AgendaPresenter.MY_AGENDA));
                        TrackingService.getInstance().sendTracking("111", "agenda", "bookmark", "", "", "");
                    }
                }),
                new ActionBarOwner.MenuAction("Agenda", new Action0() {
                    @Override
                    public void call() {
                        Flow.get(context).set(new AgendaScreen(AgendaPresenter.FULL_AGENDA));
                        TrackingService.getInstance().sendTracking("102", "agenda", "agenda", "", "", "");
                    }
                }),
                new ActionBarOwner.MenuAction("Papers", new Action0() {
                    @Override
                    public void call() {
//                        Flow.get(context).set(new AgendaPaperScreen());
                    }
                }), ActionBarOwner.Config.RIGHT_FOCUS));
        adapter = new AgendaPaperAdapter(context, this);
        getView().papers.setHasFixedSize(false);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        getView().papers.setLayoutManager(linearLayoutManager);
        getView().papers.setAdapter(adapter);
        StickyRecyclerHeadersDecoration headersDecor = new StickyRecyclerHeadersDecoration(adapter);
        getView().papers.addItemDecoration(headersDecor);
        getView().papers.disableLoadmore();
        getView().papers.setItemAnimator(new FlipInTopXAnimator());
        getView().papers.setDefaultOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                AgendaService.getInstance().syncPaper(false);
            }
        });
        adapter.setData(AgendaService.getInstance().getAllPaper());
        AgendaService.getInstance().syncPaper(false);
    }

    @Subscribe
    public void reloadPaperEvent(AgendaPaperReloadEvent event){
        adapter.setData(AgendaService.getInstance().getAllPaper());
        getView().papers.setRefreshing(false);
    }

    @Override
    public void openPDF(String url) {
        UIHelper.getInstance().openPDF(url, mainActivity);
    }

    @Override
    public void openEpub(String url) {
        UIHelper.getInstance().openEpub(url, mainActivity);
    }
}
