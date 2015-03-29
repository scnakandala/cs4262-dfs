package cs4262.dfs;

public class Main {

    public static int hopCount = 10;
    public static boolean debug = false;
    
    public static void main(String[] args) throws InterruptedException {

        final Node node = UDPNode.getInstance();
        final BootstrapClient bootstrapClient = new BootstrapClient();
        final RoutingTable routingTable = RoutingTable.getInstance();
        final ConsoleReader consoleReader = new ConsoleReader(node);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                bootstrapClient.unregister();
                node.informLeaveToPeers();
            }
        });

        consoleReader.start();
        String[] nodes = bootstrapClient.register();
        if (nodes != null) {
            for (int i = 0; i < nodes.length; i++) {
                routingTable.addNode(nodes[i]);
            }
        }
        node.informJoinToPeers();
        node.start();

        node.join();
        consoleReader.join();
    }
}
