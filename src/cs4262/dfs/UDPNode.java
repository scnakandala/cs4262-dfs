package cs4262.dfs;

import cs4262.dfs.communicators.UDPClient;
import cs4262.dfs.utils.DFSProperties;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UDPNode extends Node {

    private final DFSProperties dfsProperties;
    private static UDPNode instance;
    private MessageWindow messageWindow;

    public static UDPNode getInstance() {
        if (instance == null) {
            instance = new UDPNode();
        }
        return instance;
    }

    private UDPNode() {
        this.dfsProperties = DFSProperties.getInstance();
        this.messageWindow = new MessageWindow();
    }

    @Override
    public void run() {
        try {
            String port = dfsProperties.getProperty("node.port", "");
            String host = dfsProperties.getProperty("node.host", "");
            final DatagramSocket s = new DatagramSocket(Integer.parseInt(port));

            byte[] data = new byte[100];
            DatagramPacket dgp = new DatagramPacket(data, data.length);

            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    //When shutting down close the UDP Socket
                    s.close();
                }
            });

            while (true) {
                s.receive(dgp);
                byte[] receivedData = dgp.getData();
                String receivedString = new String(receivedData);
                int msgLength = Integer.parseInt(receivedString.split(" ")[0]);
                receivedString = receivedString.substring(0, msgLength);
                String commandType = receivedString.split(" ")[1];
                
                if(Main.debug)
                Logger.getLogger(BootstrapClient.class.getName()).log(Level.INFO,
                        "Received Message :{0}", receivedString);
                
                switch (commandType) {
                    case "JOIN":
                        String joinHost = receivedString.split(" ")[2];
                        String joinPort = receivedString.split(" ")[3];
                        RoutingTable.getInstance().addNode(joinHost + " " + joinPort);
                        break;
                    case "LEAVE":
                        String leaveHost = receivedString.split(" ")[2];
                        String leavePort = receivedString.split(" ")[3];
                        RoutingTable.getInstance().removeNode(leaveHost + " " + leavePort);
                        break;
                    case "SER":
                        String queryHost = receivedString.split(" ")[2];
                        String queryPort = receivedString.split(" ")[3];
                        String fileName = receivedString.split(" ")[4];
                        int hopCount = Integer.parseInt(receivedString.split(" ")[5].trim());
                        if (FileRepository.getInstance().checkFileExists(fileName)) {
                            ArrayList<String> matchingFiles = FileRepository.getInstance()
                                    .getAllFilesForQuery(fileName);
                            if(Main.debug)
                            Logger.getLogger(BootstrapClient.class.getName()).log(Level.INFO,
                                    "This node has matching file for the query{0} :",
                                    receivedString);
                            
                            String fileList = "";
                            for (int i = 0; i < matchingFiles.size(); i++) {
                                fileList += fileList + " " + matchingFiles.get(i).replaceAll(" ", "_");
                            }
                            fileList = fileList.trim();
                            String command = "SEROK " + (matchingFiles.size())
                                    + " " + host + " " + port + " " + (hopCount)
                                    + " " + fileList;
                            int length = command.toCharArray().length;
                            length += 5;
                            command = String.format("%04d", length) + " " + command;
                            UDPClient udpClient = new UDPClient();
                            try {
                                udpClient.sendAndForgetQuery(command, queryHost,
                                        Integer.parseInt(queryPort));
                                if(Main.debug)
                                Logger.getLogger(BootstrapClient.class.getName()).
                                        log(Level.INFO,
                                                "Sending SEROK message{0} :",
                                                command);
                            } catch (UnknownHostException ex) {
                                if(Main.debug)
                                Logger.getLogger(UDPNode.class.getName()).
                                        log(Level.SEVERE, null, ex);
                            } catch (IOException ex) {
                                if(Main.debug)
                                Logger.getLogger(UDPNode.class.getName()).
                                        log(Level.SEVERE, null, ex);
                            }
                        } else {
                            forwardQuery(queryHost, Integer.parseInt(queryPort),
                                    fileName, hopCount - 1);
                        }
                        break;
                    case "SEROK":
                        String[] temp = receivedString.split(" ");
                        System.out.println("-----Matching files found at host:"
                                + temp[3] + " port:" + temp[4] + " - Hop Count: " + temp[5] + "-----");
                        for (int i = 6; i < temp.length; i++) {
                            System.out.println(" * " + temp[i].replaceAll("_", " "));
                        }
                        System.out.println();
                        break;
                    default:
                        if(Main.debug)
                        Logger.getLogger(UDPNode.class.getName()).log(Level.SEVERE,
                                "Invalid Message :{0}", receivedString);
                        break;
                }
            }
        } catch (IOException ex) {
            if(Main.debug)
            Logger.getLogger(UDPNode.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(-1);
        }
    }

    @Override
    public void forwardQuery(String ip, int port, String input, int i) {
        String command = "SER " + ip + " " + port + " " + input + " " + i;
        int length = command.toCharArray().length;
        length += 5;
        command = String.format("%04d", length) + " " + command;
        if (i > 0 && messageWindow.shouldForwardMessage(command)) {
            ArrayList<String> peers = RoutingTable.getInstance().getNodes();
            for (int j = 0; j < peers.size(); j++) {
                String peerHost = peers.get(j).split(" ")[0];
                String peerPort = peers.get(j).split(" ")[1];
                UDPClient udpClient = new UDPClient();
                try {
                    udpClient.sendAndForgetQuery(command, peerHost,
                            Integer.parseInt(peerPort));
                    if(Main.debug)
                    Logger.getLogger(BootstrapClient.class.getName()).log(Level.INFO,
                            "Sent SEARCH Message :{0}", command);
                } catch (UnknownHostException ex) {
                    if(Main.debug)
                    Logger.getLogger(UDPNode.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    if(Main.debug)
                    Logger.getLogger(UDPNode.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    @Override
    public void informJoinToPeers() {
        String host = dfsProperties.getProperty("node.host", "");
        String port = dfsProperties.getProperty("node.port", "");
        String username = dfsProperties.getProperty("node.username", "");
        String command = "JOIN " + host + " " + port + " " + username;
        int length = command.toCharArray().length;
        length += 5;
        command = String.format("%04d", length) + " " + command;
        ArrayList<String> peers = RoutingTable.getInstance().getNodes();
        for (int i = 0; i < peers.size(); i++) {
            String temp = peers.get(i);
            String peerHost = temp.split(" ")[0];
            int peerPort = Integer.parseInt(temp.split(" ")[1]);
            UDPClient udpClient = new UDPClient();
            try {
                udpClient.sendAndForgetQuery(command, peerHost, peerPort);
                if(Main.debug)
                Logger.getLogger(BootstrapClient.class.getName()).log(Level.INFO,
                        "Sent JOIN Message :{0}", command);
            } catch (UnknownHostException ex) {
                if(Main.debug)
                Logger.getLogger(UDPNode.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                if(Main.debug)
                Logger.getLogger(UDPNode.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public void informLeaveToPeers() {
        String host = dfsProperties.getProperty("node.host", "");
        String port = dfsProperties.getProperty("node.port", "");
        String username = dfsProperties.getProperty("node.username", "");
        String command = "LEAVE " + host + " " + port + " " + username;
        int length = command.toCharArray().length;
        length += 5;
        command = String.format("%04d", length) + " " + command;
        ArrayList<String> peers = RoutingTable.getInstance().getNodes();
        for (int i = 0; i < peers.size(); i++) {
            String temp = peers.get(i);
            String peerHost = temp.split(" ")[0];
            int peerPort = Integer.parseInt(temp.split(" ")[1]);
            UDPClient udpClient = new UDPClient();
            try {
                udpClient.sendAndForgetQuery(command, peerHost, peerPort);
                if(Main.debug)
                Logger.getLogger(BootstrapClient.class.getName()).log(Level.INFO,
                        "Sent LEAVE Message :{0}", command);
            } catch (UnknownHostException ex) {
                if(Main.debug)
                Logger.getLogger(UDPNode.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                if(Main.debug)
                Logger.getLogger(UDPNode.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}