package sg.edu.smu.livelabs.mobicom.models.dijkstra.model;

import java.util.ArrayList;

/**
 * Represents a graph.
 */
public class Graph {

    private Node[] nodes;

    public Node[] getNodes() {
        return nodes;
    }

    private int noOfNodes;

    public int getNoOfNodes() {
        return noOfNodes;
    }

    private Edge[] edges;

    public Edge[] getEdges() {
        return edges;
    }

    private int noOfEdges;

    public int getNoOfEdges() {
        return noOfEdges;
    }

    private int origin, endPoint;

    /**
     * Constructor that builds the whole graph from an Array of Edges
     */
    public Graph(Edge[] edges, int origin, int endPoint) {

        // The edges are passed in, so store them
        this.edges = edges;
        this.origin = origin;
        this.endPoint = endPoint;
        // Create all the nodes, ready to be updated with the edges
        this.noOfNodes = calculateNoOfNodes(edges);
        this.nodes = new Node[this.noOfNodes];
        for (int n = 0; n < this.noOfNodes; n++) {
            this.nodes[n] = new Node();
        }

        // Add all the edges to the nodes. Each edge is added to 2 nodes (the "to" and the "from")
        this.noOfEdges = edges.length;
        for (int edgeToAdd = 0; edgeToAdd < this.noOfEdges; edgeToAdd++) {
            this.nodes[edges[edgeToAdd].getFromNodeIndex()].getEdges().add(edges[edgeToAdd]);
            this.nodes[edges[edgeToAdd].getToNodeIndex()].getEdges().add(edges[edgeToAdd]);
        }

    }

    /**
     * Calculate the number of nodes in an array of edges
     *
     * @param edges An array of edges that represents the graph.
     * @return The number of nodes in the graph.
     *
     */
    private int calculateNoOfNodes(Edge[] edges) {
        int noOfNodes = 0;
        for (Edge e : edges) {
            if (e.getToNodeIndex() > noOfNodes) {
                noOfNodes = e.getToNodeIndex();
            }
            if (e.getFromNodeIndex() > noOfNodes) {
                noOfNodes = e.getFromNodeIndex();
            }
        }
        noOfNodes++;
        return noOfNodes;
    }

    /**
     * Uses Dijkstra's algorithm to calculate the shortest distance from the
     * source to all nodes
     *
     */
    public void calculateShortestDistances() {

        // Set node 0 as the source //origin
        this.nodes[origin].setDistanceFromSource(0);
        int nextNode = origin;

        // Visit every node, in order of stored distance
        for (int i = 0; i < this.nodes.length; i++) {

            // Loop round the edges that are joined to the current node
            ArrayList<Edge> currentNodeEdges = this.nodes[nextNode].getEdges();
            for (int joinedEdge = 0; joinedEdge < currentNodeEdges.size(); joinedEdge++) {
                // Determine the joined edge neighbour of the current node
                int neighbourIndex = currentNodeEdges.get(joinedEdge).getNeighbourIndex(nextNode);

                // Only interested in an unvisited neighbour
                if (!this.nodes[neighbourIndex].isVisited()) {

                    // Calculate the tentative distance for the neighbour
                    int tentative = this.nodes[nextNode].getDistanceFromSource() + currentNodeEdges.get(joinedEdge).getLength();

                    // Overwrite if the tentative distance is less than what's currently stored
                    if (tentative < nodes[neighbourIndex].getDistanceFromSource()) {
//                                                System.out.println("step number = " + i);
//                                                System.out.println("current node = " + nextNode);
//                                                System.out.println("neighbour: " + neighbourIndex);
                        nodes[neighbourIndex].addNode(nextNode);
                        nodes[neighbourIndex].setDistanceFromSource(tentative);
                    }

                }

            }

            // All neighbours are checked so this node is now visited
            nodes[nextNode].setVisited(true);

            // The next node to visit must be the one with the shortest distance.
            nextNode = getNodeShortestDistanced();
            //System.out.println();
        }

    }

    /**
     * Scans the unvisited nodes and calculates which one has the shortest
     * distance from the source.
     *
     * @return The index of the node with the smallest distance
     */
    private int getNodeShortestDistanced() {

        int storedNodeIndex = 0;
        int storedDist = Integer.MAX_VALUE;

        for (int i = 0; i < this.nodes.length; i++) {
            int currentDist = this.nodes[i].getDistanceFromSource();
            if (!this.nodes[i].isVisited() && currentDist < storedDist) {
                storedDist = currentDist;
                storedNodeIndex = i;
            }

        }

        return storedNodeIndex;
    }

    /**
     * Overrides Object.toString() to show the contents of the graph
     *
     */
    @Override
    public String toString() {

        String output = "Number of nodes = " + this.noOfNodes;
        output += "\nNumber of edges = " + this.noOfEdges;

        for (int i = 0; i < this.nodes.length; i++) {
            output += ("\nThe shortest distance from node " + origin + " to node " + i + " is " + nodes[i].getDistanceFromSource());
        }
        //System.out.println("trying to find route to node 367");
        int prevNode = nodes[endPoint].getShortestPathToNode();
        String s = prevNode + "";

        while (prevNode != origin) {
            prevNode = nodes[prevNode].getShortestPathToNode();
            s = prevNode + "," + s;
        }
        s = s + "," + endPoint;
        return s;

    }

}
