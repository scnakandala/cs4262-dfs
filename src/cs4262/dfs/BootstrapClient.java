package cs4262.dfs;

import cs4262.dfs.communicators.UDPClient;
import cs4262.dfs.utils.DFSProperties;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BootstrapClient {

    private final DFSProperties dfsProperties;

    public BootstrapClient() {
        this.dfsProperties = DFSProperties.getInstance();
    }

    public String[] register() {
        String host = dfsProperties.getProperty("node.host", "");
        String port = dfsProperties.getProperty("node.port", "");
        String username = dfsProperties.getProperty("node.username", "");
        String command = " REG " + host + " " + port + " " + username;
        int length = command.toCharArray().length;
        length += 5;
        command = String.format("%04d", length) + " " + command;

        try {
            UDPClient udpClient = new UDPClient();
            String bsHost = dfsProperties.getProperty("bs.host", "");
            int bsPort = Integer.parseInt(dfsProperties.getProperty("bs.port", ""));
            String response = udpClient
                    .sendAndReceiveQuery(command, bsHost, bsPort);
            if(Main.debug){
                System.out.println("Send Command :"+ command);            
                System.out.println("Received Response :" + response);
            }
            return parseResponse(response);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public boolean unregister() {
        String host = dfsProperties.getProperty("node.host", "");
        String port = dfsProperties.getProperty("node.port", "");
        String username = dfsProperties.getProperty("node.username", "");
        String command = " UNREG " + host + " " + port + " " + username;
        int length = command.toCharArray().length;
        length += 5;
        command = String.format("%04d", length) + " " + command;

        try {
            UDPClient udpClient = new UDPClient();
            String bsHost = dfsProperties.getProperty("bs.host", "");
            int bsPort = Integer.parseInt(dfsProperties.getProperty("bs.port", ""));
            String response = udpClient
                    .sendAndReceiveQuery(command, bsHost, bsPort);
            if(Main.debug){
                System.out.println("Send Command :"+ command);            
                System.out.println("Received Response :" + response);
            }
            return true;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    private String[] parseResponse(String response) {
        int length = Integer.parseInt(response.split(" ")[2]);
        if (length == 0) {
            return null;
        } else if (length >= 9996) {
            if(Main.debug)
                System.out.println("Communication failed with BS. Error Code:" + length);
            return null;
        }
        String[] nodes = new String[length];
        response = response.substring(12).trim();
        String[] parameters = response.split(" ");
        for (int i = 0; i < length; i++) {
            nodes[i] = parameters[i * 2] + " " + parameters[i * 2 + 1];
        }
        return nodes;
    }
}
