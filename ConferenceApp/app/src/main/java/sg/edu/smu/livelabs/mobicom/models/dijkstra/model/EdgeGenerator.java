package sg.edu.smu.livelabs.mobicom.models.dijkstra.model;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

public class EdgeGenerator {
    private Context context;
    private int height, width;
    private InputStreamReader floorplan;
    private HashMap<Integer, Integer> idMap;

    public HashMap<Integer, Integer> getIdMap() {
        return idMap;
    }

    public EdgeGenerator(InputStreamReader floorplan) {
        idMap = new HashMap<>();
        this.floorplan = floorplan;
    }

    public Edge[] generateEdges() {
        BufferedReader br = null;

        try {
            ArrayList<String[]> tempList = new ArrayList<>();
            String currentLine;

            br = new BufferedReader(floorplan);
            //br = new BufferedReader(new FileReader(floorplan));

            while ((currentLine = br.readLine()) != null) {
                String[] tempArr = currentLine.split(",");
                tempList.add(tempArr);
            }

            String[][] map = new String[tempList.size()][tempList.get(0).length];

            //creating the 2d map
            for (int i = 0; i < tempList.size(); i++) {
                for (int j = 0; j < tempList.get(0).length; j++) {
                    map[i][j] = tempList.get(i)[j];
                }
            }

            ArrayList<Edge> edgeList = new ArrayList<>();
            height = map.length;
            width = map[0].length;

            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    String currentCell = map[i][j];

                    if (!currentCell.equals("x")) {
                        idMap.put(Integer.parseInt(map[i][j]), (i * width + j));

                        //going up
                        if (inRange(i - 1, j) && !map[i - 1][j].equals("x")) {
                            edgeList.add(new Edge(i * width + j, (i - 1) * width + j, 1));
                        }

                        //going down
                        if (inRange(i + 1, j) && !map[i + 1][j].equals("x")) {
                            edgeList.add(new Edge(i * width + j, (i + 1) * width + j, 1));
                        }

                        //going left
                        if (inRange(i, j - 1) && !map[i][j - 1].equals("x")) {
                            edgeList.add(new Edge(i * width + j, i * width + (j - 1), 1));
                        }

                        //going right
                        if (inRange(i, j + 1) && !map[i][j + 1].equals("x")) {
                            edgeList.add(new Edge(i * width + j, i * width + (j + 1), 1));
                        }
                    }
                }
            }

            Edge[] edges = new Edge[edgeList.size()];
            edges = edgeList.toArray(edges);

            /*
             for (Map.Entry<String, String> entry : idMap.entrySet()) {
             System.out.println("landmark id:" + entry.getKey() + ", node value:" + entry.getValue());
             }
             */
            return edges;

//            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (Exception e) {

            }
        }
        return null;
    }

    public boolean inRange(int h, int w) {
        return inHeight(h) && inWidth(w);
    }

    public boolean inHeight(int index) {
        return index >= 0 && index < height;
    }

    public boolean inWidth(int index) {
        return index >= 0 && index < width;
    }
}
