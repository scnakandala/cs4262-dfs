package cs4262.dfs;

import cs4262.dfs.utils.DFSProperties;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Node extends Thread {

    private DFSProperties dfsProperties;
    private BootstrapClient bootstrapClient;

    public Node() {
        this.dfsProperties = DFSProperties.getInstance();
        this.bootstrapClient = new BootstrapClient();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                //When shutting down UNREG the peer node from bootstrap server
                bootstrapClient.unregister();

                //Todo
                //Informing neighbors for gracefully leaving the network                
            }
        });
    }

    @Override
    public void run() {
        try {
            String host = dfsProperties.getProperty("node.host", "");
            String port = dfsProperties.getProperty("node.port", "");
            Socket socket = new Socket(dfsProperties.getProperty("bs.host", ""),
                    Integer.parseInt(dfsProperties.getProperty("bs.port", "")));
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    socket.getInputStream()));
            while (true) {
                String query = in.readLine();
                //Todo
                //Process the query. Chek it is locally available. Else forward
                //to some neighbours
            }
        } catch (IOException ex) {
            Logger.getLogger(Node.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(-1);
        }
    }
}
