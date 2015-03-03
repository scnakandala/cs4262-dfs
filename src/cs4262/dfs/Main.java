package cs4262.dfs;

public class Main {

    public static void main(String[] args) throws InterruptedException {
        
        //Starting peer node
        Node node = new Node();
        node.start();
        
        //Starting console reader
        ConsoleReader consoleReader = new ConsoleReader();
        consoleReader.start();
        
        //Registering with the bootstrap server and initialising routing tables
        BootstrapClient bootstrapClient = new BootstrapClient();
        RoutingTable routingTable = RoutingTable.getInstance();
        String[] nodes = bootstrapClient.register();
        if (nodes != null) {
            for (int i = 0; i < nodes.length; i++) {
                routingTable.addNode(nodes[i]);
            }
        }
        
        //Waiting for the peer node to stop
        node.join();
        //Waiting for the console reader to stop
        consoleReader.join();
    }
}
