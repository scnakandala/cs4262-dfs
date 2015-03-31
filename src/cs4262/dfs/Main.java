package cs4262.dfs;

import cs4262.dfs.thrift.ThriftNode;

public class Main {

    public static int hopCount = 10;
    public static boolean debug = true;
    public static long queryStartedTime;
    
    public static long[][] queryExecutionSummary = new long[50][2];
    public static long[] forwardedMessageCounts = new long[250];
    
    public static void main(String[] args) throws InterruptedException {

        final BootstrapClient bootstrapClient = new BootstrapClient();
        final RoutingTable routingTable = RoutingTable.getInstance();        
        String[] nodes = bootstrapClient.register();
        if (nodes != null) {
            for (int i = 0; i < nodes.length; i++) {
                routingTable.addNode(nodes[i]);
            }
        }
        
        final Node node = ThriftNode.getInstance();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                bootstrapClient.unregister();
                node.informLeaveToPeers();
            }
        });
        node.startNode();
        
        final ConsoleReader consoleReader = new ConsoleReader(node);
        consoleReader.start();
        
        node.join();
        consoleReader.join();
    }
}
