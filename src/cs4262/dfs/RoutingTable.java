package cs4262.dfs;

import java.util.ArrayList;

public class RoutingTable {

    private static RoutingTable instance;

    private ArrayList<String> nodes;

    private RoutingTable() {
        this.nodes = new ArrayList<String>();
    }

    public static RoutingTable getInstance() {
        if (RoutingTable.instance == null) {
            RoutingTable.instance = new RoutingTable();
        }
        return RoutingTable.instance;
    }

    public void addNode(String node) {
        nodes.add(node);
    }

    public void removeNode(String node) {
        nodes.remove(node);
    }
    
    public ArrayList<String> getNodes(){
        return this.nodes;
    }
}
