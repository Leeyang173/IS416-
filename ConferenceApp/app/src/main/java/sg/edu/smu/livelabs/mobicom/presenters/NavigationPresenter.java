package sg.edu.smu.livelabs.mobicom.presenters;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;

import autodagger.AutoComponent;
import automortar.AutoScreen;
import flow.Flow;
import mortar.ViewPresenter;
import sg.edu.smu.livelabs.mobicom.ActionBarOwner;
import sg.edu.smu.livelabs.mobicom.AppDependencies;
import sg.edu.smu.livelabs.mobicom.DaggerScope;
import sg.edu.smu.livelabs.mobicom.MainActivity;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.flow.Layout;
import sg.edu.smu.livelabs.mobicom.models.dijkstra.model.Edge;
import sg.edu.smu.livelabs.mobicom.models.dijkstra.model.EdgeGenerator;
import sg.edu.smu.livelabs.mobicom.models.dijkstra.model.Graph;
import sg.edu.smu.livelabs.mobicom.net.RestClient;
import sg.edu.smu.livelabs.mobicom.presenters.screen.ARNavigationScreen;
import sg.edu.smu.livelabs.mobicom.views.NavigationView;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
/**
 * Created by Jerms on 14/11/16.
 */
@AutoScreen(
        component = @AutoComponent(dependencies = MainActivity.class, superinterfaces = AppDependencies.class),
        screenAnnotations = Layout.class
)
@DaggerScope(NavigationPresenter.class)
@Layout(R.layout.navi_view)
public class NavigationPresenter extends ViewPresenter<NavigationView> {
    private MainActivity mainActivity;
    private ActionBarOwner actionBarOwner;
    private String url = RestClient.WEBVIEW_BASE_URL + "sponsor";
    private boolean error = false;

    public NavigationPresenter(MainActivity mainActivity, ActionBarOwner actionBarOwner){
        this.mainActivity = mainActivity;
        this.actionBarOwner = actionBarOwner;
    }
    @Override
    protected void onLoad(Bundle savedInstanceState) {
        super.onLoad(savedInstanceState);
        mainActivity.currentTab = MainActivity.OTHER_TAB;
        mainActivity.setVisibleBottombar(View.VISIBLE);
        actionBarOwner.setConfig(new ActionBarOwner.Config(true, "Navigation", null));
        getView().messageTV.setText("Where are you headed?");
        getView().arButton.setText("Toggle AR");
        getView().locDDL.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view,
                                   int position, long id) {

            String location = getView().locDDL.getSelectedItem().toString();
            System.out.println(location);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            // TODO Auto-generated method stub

        }
        });

        getView().arButton.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                //Flow.get(getView().getContext()).set(new ARNavigationScreen());
                try {
                    test();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void test() throws IOException {

       // EdgeGenerator eg = new EdgeGenerator("src/dijkstra/labs1.csv");
        Context context = mainActivity.getContext();

        EdgeGenerator eg = new EdgeGenerator(new InputStreamReader(context.getAssets().open("labs1.csv")));
        Edge[] level1 = eg.generateEdges();
        HashMap<Integer, Integer> level1Map = eg.getIdMap();

        EdgeGenerator eg2 = new EdgeGenerator(new InputStreamReader(context.getAssets().open("labs2.csv")));
        Edge[] level2 = eg2.generateEdges();
        HashMap<Integer, Integer> level2Map = eg2.getIdMap();

        String from = "1060110046";
        String to = "1060210037";
        int fromLevel = Integer.parseInt(from.substring(3, 5));
        int fromLandmark = Integer.parseInt(from.substring(6, 10));
        int toLevel = Integer.parseInt(to.substring(3, 5));
        int toLandmark = Integer.parseInt(to.substring(6, 10));

        if (fromLevel == 1 && toLevel == 2) {
            Graph level1Graph = new Graph(level1, level1Map.get(fromLandmark), level1Map.get(51));
            level1Graph.calculateShortestDistances();
            System.out.println(level1Graph.toString());

            Graph level2Graph = new Graph(level2, level2Map.get(35) + 1, level2Map.get(toLandmark));
            level2Graph.calculateShortestDistances();
            System.out.println(level2Graph.toString());
        } else if (fromLevel == 2 && toLevel == 1) {
            Graph level2Graph = new Graph(level2, level2Map.get(fromLandmark), level2Map.get(35) + 1);
            level2Graph.calculateShortestDistances();
            System.out.println(level2Graph.toString());

            Graph level1Graph = new Graph(level1, level1Map.get(51), level1Map.get(toLandmark));
            level1Graph.calculateShortestDistances();
            System.out.println(level1Graph.toString());
        } else if (fromLevel == 1) {
            Graph level1Graph = new Graph(level1, level1Map.get(fromLandmark), level1Map.get(toLandmark));
            level1Graph.calculateShortestDistances();
            System.out.println(level1Graph.toString());
        } else {
            Graph level2Graph = new Graph(level2, level2Map.get(fromLandmark), level2Map.get(toLandmark));
            level2Graph.calculateShortestDistances();
            System.out.println(level2Graph.toString());
        }
    }
}
