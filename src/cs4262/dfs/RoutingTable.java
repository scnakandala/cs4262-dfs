package cs4262.dfs;

import cs4262.dfs.utils.DFSProperties;
import java.util.ArrayList;
import java.util.Random;

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
    
    public ArrayList<String> getRandomThreeNeighBours(){
        ArrayList<String> selected= new ArrayList<String>();
        ArrayList<String> temp= new ArrayList<String>(this.nodes);
        Random rand = new Random();
        for(int i=0;i<3;i++){
            selected.add(temp.remove(rand.nextInt(temp.size())));
        }
        return selected;
    }
    
    public String getMyIP(){
        return DFSProperties.getInstance().getProperty("node.host", "localhost");
    }
    
    public int getPort(){
        return Integer.parseInt(DFSProperties.getInstance()
                .getProperty("node.port", "localhost"));
    }
}
