package sg.edu.smu.livelabs.mobicom.models.dijkstra.model;

import java.util.ArrayList;

/**
 * Represents a node in a graph.
 */
public class Node {

    private int distanceFromSource = Integer.MAX_VALUE;
    int shortestPathToNode;

    public int getShortestPathToNode() {
        return shortestPathToNode;
    }

    public void addNode(int n) {
        shortestPathToNode = n;
    }

    public int getDistanceFromSource() {
        return distanceFromSource;
    }

    public void setDistanceFromSource(int distanceFromSource) {
        this.distanceFromSource = distanceFromSource;
    }

    private boolean visited = false;

    public boolean isVisited() {
        return visited;
    }

    public void setVisited(boolean visited) {
        this.visited = visited;
    }

    private ArrayList<Edge> edges = new ArrayList<Edge>();

    public ArrayList<Edge> getEdges() {
        return edges;
    }

    public void setEdges(ArrayList<Edge> edges) {
        this.edges = edges;
    }
}
