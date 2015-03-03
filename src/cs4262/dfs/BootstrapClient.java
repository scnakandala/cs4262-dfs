package cs4262.dfs;

import cs4262.dfs.utils.DFSProperties;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BootstrapClient {

    private DFSProperties dfsProperties;

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
        if (length > 999) {
            command = length + " " + command;
        } else if (length > 99) {
            command = "0" + length + " " + command;
        } else if (length > 9) {
            command = "00" + length + " " + command;
        } else {
            command = "000" + length + " " + command;
        }

        try {
            Socket socket = new Socket(dfsProperties.getProperty("bs.host", ""),
                    Integer.parseInt(dfsProperties.getProperty("bs.port", "")));
            PrintWriter out
                    = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in
                    = new BufferedReader(
                            new InputStreamReader(socket.getInputStream()));
            out.print(command);
            out.flush();
            String response = in.readLine();
            Logger.getLogger(BootstrapClient.class.getName()).log(Level.INFO,
                    "Send Command :" + command);
            Logger.getLogger(BootstrapClient.class.getName()).log(Level.INFO,
                    "Received Response :" + response);
            return parseResponse(response);
        } catch (IOException ex) {
            Logger.getLogger(BootstrapClient.class.getName()).log(Level.SEVERE,
                    null, ex);
        }

        return null;
    }
    
    private boolean unregister(){
        String host = dfsProperties.getProperty("node.host", "");
        String port = dfsProperties.getProperty("node.port", "");
        String username = dfsProperties.getProperty("node.username", "");
        String command = " UNREG " + host + " " + port + " " + username;
        int length = command.toCharArray().length;
        length += 5;
        if (length > 999) {
            command = length + " " + command;
        } else if (length > 99) {
            command = "0" + length + " " + command;
        } else if (length > 9) {
            command = "00" + length + " " + command;
        } else {
            command = "000" + length + " " + command;
        }

        try {
            Socket socket = new Socket(dfsProperties.getProperty("bs.host", ""),
                    Integer.parseInt(dfsProperties.getProperty("bs.port", "")));
            PrintWriter out
                    = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in
                    = new BufferedReader(
                            new InputStreamReader(socket.getInputStream()));
            out.print(command);
            out.flush();
            String response = in.readLine();
            Logger.getLogger(BootstrapClient.class.getName()).log(Level.INFO,
                    "Send Command :" + command);            
            Logger.getLogger(BootstrapClient.class.getName()).log(Level.INFO,
                    "Received Response :" + response);
            return true;
        } catch (IOException ex) {
            Logger.getLogger(BootstrapClient.class.getName()).log(Level.SEVERE,
                    null, ex);
        }
        return false;
    }

    private String[] parseResponse(String response) {
        int length = Integer.parseInt(response.toCharArray()[11] + "");
        if(length==0){
            return null;
        }
        String[] nodes = new String[length];
        response = response.substring(12).trim();
        String[] parameters = response.split(" ");
        for(int i=0;i<length;i++){
            nodes[i] = parameters[i*2] + parameters[i*2+1];
        }
        return nodes;
    }
}
