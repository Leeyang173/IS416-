package sg.edu.smu.livelabs.mobicom.presenters;

import android.os.StrictMode;
import android.content.Context;
import android.content.res.AssetManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;

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
    private String url = RestClient.LOC_MAC_URL;
    private boolean error = false;
    private String destID = "1010200075";

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
            int locID = getView().locDDL.getSelectedItemPosition();
            switch (locID){
                case 0:
                    destID = "1010200125";
                    break;
                case 1:
                    destID = "1010200100";
                    break;
                case 2:
                    destID = "1010200089";
                    break;
                case 3:
                    destID = "1010200068";
                    break;
                case 4:
                    destID = "1010200140";
                    break;
                case 5:
                    destID = "1010200138";
                    break;
                case 6:
                    destID = "1010200130";
                    break;
                case 7:
                    destID = "1010200126";
                    break;
                case 8:
                    destID = "1010200087";
                    break;
                case 9:
                    destID = "1010200085";
                    break;
                case 10:
                    destID = "1010200083";
                    break;
                case 11:
                    destID = "1010200010";
                    break;
                case 12:
                    destID = "1010300123";
                    break;
                case 13:
                    destID = "1010300114";
                    break;
                case 14:
                    destID = "1010300101";
                    break;
                case 15:
                    destID = "1010300074";
                    break;
                case 16:
                    destID = "1010300151";
                    break;
                case 17:
                    destID = "1010300138";
                    break;
                case 18:
                    destID = "1010300140";
                    break;
                case 19:
                    destID = "1010300146";
                    break;
                case 20:
                    destID = "1010300070";
                    break;
                case 21:
                    destID = "1010300072";
                    break;
                case 22:
                    destID = "1010300095";
                    break;
                case 23:
                    destID = "1010300091";
                    break;
                case 24:
                    destID = "1010300157";
                    break;
                default:
                    break;
            }
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

        Context context = mainActivity.getContext();

        EdgeGenerator eg = new EdgeGenerator(new InputStreamReader(context.getAssets().open("sislevel2.csv")));
        Edge[] level2 = eg.generateEdges();
        HashMap<Integer, Integer> level2Map = eg.getIdMap();

        EdgeGenerator eg2 = new EdgeGenerator(new InputStreamReader(context.getAssets().open("sislevel3.csv")));
        Edge[] level3 = eg2.generateEdges();
        HashMap<Integer, Integer> level3Map = eg2.getIdMap();

        String from = getLocation();
        String to = destID;
        int fromLevel = Integer.parseInt(from.substring(3, 5));
        int fromLandmark = Integer.parseInt(from.substring(6, 10));
        int toLevel = Integer.parseInt(to.substring(3, 5));
        int toLandmark = Integer.parseInt(to.substring(6, 10));


        //THIS CODES ARE FOR SMU LABS

        /*if (fromLevel == 1 && toLevel == 2) {
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
        */

        ///
        if (fromLevel == 3 && toLevel == 2) {
            Graph level3Graph = new Graph(level3, level3Map.get(fromLandmark), level3Map.get(44));
            level3Graph.calculateShortestDistances();
            System.out.println(level3Graph.toString());

            Graph level2Graph = new Graph(level2, level2Map.get(32), level2Map.get(toLandmark));
            level2Graph.calculateShortestDistances();
            System.out.println(level2Graph.toString());
        } else if (fromLevel == 2 && toLevel == 3) {
            Graph level2Graph = new Graph(level2, level2Map.get(fromLandmark), level2Map.get(32));
            level2Graph.calculateShortestDistances();
            System.out.println(level2Graph.toString());

            Graph level3Graph = new Graph(level3, level3Map.get(44), level3Map.get(toLandmark));
            level3Graph.calculateShortestDistances();
            System.out.println(level3Graph.toString());
        } else if (fromLevel == 3) {
            Graph level3Graph = new Graph(level3, level3Map.get(fromLandmark), level3Map.get(toLandmark));
            level3Graph.calculateShortestDistances();
            System.out.println(level3Graph.toString());
        } else {
            Graph level2Graph = new Graph(level2, level2Map.get(fromLandmark), level2Map.get(toLandmark));
            level2Graph.calculateShortestDistances();
            System.out.println(level2Graph.toString());
        }
    }

    public String getMac() throws IOException {
        String toRet = "";
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost("https://athena.smu.edu.sg/hestia/analytics_sandbox/smulabs/index.php/get_mac_from_ip");
        List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>(1);
        nameValuePair.add(new BasicNameValuePair("ip", getLocalIpAddress()));
        InputStream is = null;
        try {
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePair));
            HttpResponse response = httpClient.execute(httpPost);
            // write response to log
            Log.d("Http Post Response:", response.toString());
            HttpEntity entity = response.getEntity();
            is = entity.getContent();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();

            String line = null;
                while ((line = reader.readLine()) != null) {
                    sb.append((line + "\n"));
                }
            line = sb.toString();

            JSONObject result = new JSONObject(line);
            toRet = result.getString("mac");
        }catch(Exception e){
            // Log exception
            e.printStackTrace();
        } finally {
            if (is != null){
                is.close();
            }
        }
        return  toRet;
    }

    public String getLocation() throws IOException {
        String toRet = "";
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost("https://athena.smu.edu.sg/hestia/analytics_sandbox/smulabs/index.php/Point_location/getUserLocationByMAC");
        List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>(1);
        nameValuePair.add(new BasicNameValuePair("encoded_mac", getMac()));
        InputStream is = null;
        try {
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePair));
            HttpResponse response = httpClient.execute(httpPost);
            // write response to log
            Log.d("Http Post Response:", response.toString());
            HttpEntity entity = response.getEntity();
            is = entity.getContent();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();

            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append((line + "\n"));
            }
            line = sb.toString();

            JSONObject result = new JSONObject(line);
            JSONArray arr = result.getJSONArray(result.names().getString(result.names().length() - 1));
            JSONObject obj = arr.getJSONObject(0);
            toRet = obj.getString("location");
        }catch(Exception e){
            // Log exception
            e.printStackTrace();
        } finally {
            if (is != null){
                is.close();
            }
        }
        return toRet;
    }

    public String getLocalIpAddress(){
        Context context = mainActivity.getContext();
        WifiManager wifiManager = (WifiManager) context.getSystemService(context.WIFI_SERVICE);
        String ipAddress = Formatter.formatIpAddress(wifiManager.getConnectionInfo().getIpAddress());
        return ipAddress;
    }
}
