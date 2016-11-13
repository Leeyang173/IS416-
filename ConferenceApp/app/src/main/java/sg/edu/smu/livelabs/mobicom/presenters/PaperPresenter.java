package sg.edu.smu.livelabs.mobicom.presenters;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;

import autodagger.AutoComponent;
import automortar.AutoScreen;
import mortar.ViewPresenter;
import sg.edu.smu.livelabs.mobicom.ActionBarOwner;
import sg.edu.smu.livelabs.mobicom.AppDependencies;
import sg.edu.smu.livelabs.mobicom.DaggerScope;
import sg.edu.smu.livelabs.mobicom.MainActivity;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.UIHelper;
import sg.edu.smu.livelabs.mobicom.adapters.PapersAdapter;
import sg.edu.smu.livelabs.mobicom.flow.Layout;
import sg.edu.smu.livelabs.mobicom.services.PaperService;
import sg.edu.smu.livelabs.mobicom.views.PaperView;

/**
 * Created by smu on 28/2/16.
 */
@AutoScreen(
        component = @AutoComponent(dependencies = MainActivity.class, superinterfaces = AppDependencies.class),
        screenAnnotations = Layout.class
)
@DaggerScope(PaperPresenter.class)
@Layout(R.layout.paper_view)
public class PaperPresenter extends ViewPresenter<PaperView> implements PapersAdapter.PapersListener {
    private ActionBarOwner actionBarOwner;
    private PapersAdapter adapter;
    private Context context;
    private MainActivity mainActivity;


    public PaperPresenter(ActionBarOwner actionBarOwner, MainActivity mainActivity){
        this.actionBarOwner = actionBarOwner;
        this.mainActivity = mainActivity;
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
        actionBarOwner.setConfig(new ActionBarOwner.Config(true, "Papers", null));
        adapter = new PapersAdapter(context, this);
        getView().papers.setHasFixedSize(false);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        getView().papers.setLayoutManager(linearLayoutManager);
        getView().papers.setAdapter(adapter);
        getView().papers.disableLoadmore();
        getView().papers.setDefaultOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //TODO
            }
        });
        adapter.setData(PaperService.getInstance().getPaper());
    }

    @Override
    public void openPaper(String url) {
        if (url.endsWith(".pdf")) {
            UIHelper.getInstance().openPDF(url, mainActivity);
//            Intent i = new Intent(Intent.ACTION_VIEW);
//            i.setData(Uri.parse(url));
//            i.setClassName("com.google.android.apps.docs", "com.google.android.apps.viewer.PdfViewerActivity");
//            mainActivity.startActivity(i);
        }
    }
}