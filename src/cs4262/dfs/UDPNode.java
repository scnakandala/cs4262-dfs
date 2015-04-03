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
    private final MessageWindow messageWindow;

    public static UDPNode getInstance() {
        if (instance == null) {
            instance = new UDPNode();
        }
        return instance;
    }

    private UDPNode() {
        this.dfsProperties = DFSProperties.getInstance();
        this.messageWindow = MessageWindow.getInstance();
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

                if (Main.debug) {
                    System.out.println("Received Message :" + receivedString);
                }

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
                        int queryId = Integer.parseInt(receivedString.split(" ")[2]);
                        if(!messageWindow.shouldCheckOrForwardMessage(queryId)){
                            break;
                        }                                                
                        String queryHost = receivedString.split(" ")[3];
                        String queryPort = receivedString.split(" ")[4];
                        String fileName = receivedString.split(" ")[5];
                        fileName = fileName.replaceAll("_", " ");
                        int hopCount = Integer.parseInt(receivedString.split(" ")[6].trim());
                        if (FileRepository.getInstance().checkFileExists(fileName)) {
                            ArrayList<String> matchingFiles = FileRepository.getInstance()
                                    .getAllFilesForQuery(fileName);
                            if (Main.debug) {
                                System.out.println("This node has matching file for the query :"+
                                        receivedString);
                            }

                            String fileList = "";
                            for (int i = 0; i < matchingFiles.size(); i++) {
                                fileList += fileList + " " + matchingFiles.get(i).replaceAll(" ", "_");
                            }
                            fileList = fileList.trim();
                            String command = "SEROK " + queryId + " " + (matchingFiles.size())
                                    + " " + host + " " + port + " " + (hopCount)
                                    + " " + fileList;
                            int length = command.toCharArray().length;
                            length += 5;
                            command = String.format("%04d", length) + " " + command;
                            UDPClient udpClient = new UDPClient();
                            try {
                                udpClient.sendAndForgetQuery(command, queryHost,
                                        Integer.parseInt(queryPort));
                                if (Main.debug) {
                                    System.out.println("Sending SEROK message :" +
                                                    command);
                                }
                            } catch (UnknownHostException ex) {
                                ex.printStackTrace();
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        } else {
                            forwardQuery(queryId, queryHost, Integer.parseInt(queryPort),
                                    fileName.replaceAll(" ", "_"), hopCount - 1);
                        }
                        break;
                    case "SEROK":
                        String[] temp = receivedString.split(" ");
                        System.out.println("-----Matching files found for query id:" + temp[2] + "at host:"
                                + temp[4] + " port:" + temp[5] + " - Hop Count: " 
                                + (Main.hopCount - Integer.parseInt(temp[6]) + 1) + "-----");
                        for (int i = 7; i < temp.length; i++) {
                            System.out.println(" * " + temp[i].replaceAll("_", " "));
                        }
                        long elapsedTime = (System.currentTimeMillis()-Main.queryStartedTime);
                        System.out.println(" Time (ms):" + elapsedTime);
                        System.out.println();
                        
                        int arrayIndex = Integer.parseInt(temp[2]);
                        if(Main.queryExecutionSummary[arrayIndex][0]==0 
                                || Main.queryExecutionSummary[arrayIndex][0]>elapsedTime){
                           Main.queryExecutionSummary[arrayIndex][0] = elapsedTime; 
                           Main.queryExecutionSummary[arrayIndex][1] 
                                   = (Main.hopCount - Integer.parseInt(temp[6]) + 1);
                        }
                        break;
                    default:
                        if (Main.debug) {
                            System.out.println("Invalid Message :"+ receivedString);
                        }
                        break;
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            System.exit(-1);
        }
    }

    @Override
    public void forwardQuery(int queryId, String ip, int port, String input, int i) {
        input = input.replaceAll(" ", "_");
        String command = "SER " + queryId + " " + ip + " " + port + " " + input + " " + i;
        int length = command.toCharArray().length;
        length += 5;
        command = String.format("%04d", length) + " " + command;
        if (i > 0) {
            ArrayList<String> peers = RoutingTable.getInstance().getRandomThreeNeighBours();
            for (int j = 0; j < peers.size(); j++) {
                String peerHost = peers.get(j).split(" ")[0];
                String peerPort = peers.get(j).split(" ")[1];
                UDPClient udpClient = new UDPClient();
                try {
                    udpClient.sendAndForgetQuery(command, peerHost,
                            Integer.parseInt(peerPort));
                    Main.forwardedMessageCounts[queryId]++;
                    if (Main.debug) {
                        System.out.println("Sent SEARCH Message " + command + " to " + peerHost +":"+peerPort);
                    }
                } catch (UnknownHostException ex) {
                    ex.printStackTrace();
                } catch (IOException ex) {
                    ex.printStackTrace();
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
        ArrayList<String> peers = RoutingTable.getInstance().getRandomThreeNeighBours();
        for (int i = 0; i < peers.size(); i++) {
            String temp = peers.get(i);
            String peerHost = temp.split(" ")[0];
            int peerPort = Integer.parseInt(temp.split(" ")[1]);
            UDPClient udpClient = new UDPClient();
            try {
                udpClient.sendAndForgetQuery(command, peerHost, peerPort);
                if (Main.debug) {
                    System.out.println("Sent JOIN Message :" + command 
                            + " to "+peerHost+":"+peerPort);
                }
            } catch (UnknownHostException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
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
                if (Main.debug) {
                    System.out.println("Sent LEAVE Message :" + command 
                            + " to "+peerHost+":"+peerPort);
                }
            } catch (UnknownHostException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void startNode() {
        this.informJoinToPeers();
        this.start();
    }
}
