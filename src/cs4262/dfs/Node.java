package cs4262.dfs;

import cs4262.dfs.utils.DFSProperties;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Node extends Thread {

    private DFSProperties dfsProperties;
    private BootstrapClient bootstrapClient;

    public Node() {
        this.dfsProperties = DFSProperties.getInstance();
        this.bootstrapClient = new BootstrapClient();
    }

    @Override
    public void run() {       
        try {
            final DatagramSocket s = new DatagramSocket (7000);
            Runtime.getRuntime().addShutdownHook(new Thread() {
                    public void run() {
                        //When shutting down close the UDP Socket
                        s.close();
                    }
            });
            
            String host = dfsProperties.getProperty("node.host", "");
            String port = dfsProperties.getProperty("node.port", "");
            Socket socket = new Socket(dfsProperties.getProperty("bs.host", ""),
                    Integer.parseInt(dfsProperties.getProperty("bs.port", "")));
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    socket.getInputStream()));
            
            byte [] data = new byte [100];
            DatagramPacket dgp = new DatagramPacket (data, data.length);
            
            while (true) {
                s.receive (dgp);
                Communicator.processQuery(new String (data).trim());
                
            }
        } catch (IOException ex) {
            Logger.getLogger(Node.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(-1);
        }
    }
}
