
package cs4262.dfs;

import cs4262.dfs.utils.DFSProperties;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;
import org.apache.xmlrpc.*;

/**
 *
 * @author Ratnasekera DJKC
 * 
 */ 
public class XMLRPCNode extends Node {
    private final DFSProperties dfsProperties;
    private static XMLRPCNode instance;
    private final MessageWindow messageWindow;

    public static XMLRPCNode getInstance() {
        if (instance == null) {
            instance = new XMLRPCNode();
        }
        return instance;
    }

    private XMLRPCNode() {
        this.dfsProperties = DFSProperties.getInstance();
        this.messageWindow = MessageWindow.getInstance();
    }
    
    public Integer receiveSearchQuery(int queryId,String queryHost,String queryPort,String fileName, int hopCount){
        if (Main.debug) {
           System.out.println("Received Message : " + "SER"+" "+queryId+" "+queryHost+" "+queryPort+" "+fileName+" "+hopCount);
        }
        String port = dfsProperties.getProperty("node.port", "");
        String host = dfsProperties.getProperty("node.host", "");
        if(!messageWindow.shouldCheckOrForwardMessage(queryId)){
           return 1;
        }                                                
        fileName = fileName.replaceAll("_", " ");
        if (FileRepository.getInstance().checkFileExists(fileName)) {
            ArrayList<String> matchingFiles = FileRepository.getInstance().getAllFilesForQuery(fileName);
            if (Main.debug) {
                String receivedString="SER"+" "+queryId+" "+queryHost+" "+queryPort+" "+fileName+" "+hopCount;
                System.out.println("This node has matching file for the query :"+receivedString);
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
            //UDPClient udpClient = new UDPClient();
            try {
                XmlRpcClient server = new XmlRpcClient(queryHost,Integer.valueOf(queryPort)); 
                Vector params = new Vector();         
                params.addElement(new Integer(queryId));
                params.addElement(new Integer(matchingFiles.size()));
                params.addElement(host);
                params.addElement(port);
                params.addElement(hopCount);
                params.addElement(fileList);
                Object result = server.execute("rpc.receiveSearchQueryResponce", params);
                //udpClient.sendAndForgetQuery(command, queryHost,Integer.parseInt(queryPort));
                if (Main.debug) {
                     System.out.println("Sending SEROK message :" +command);
                }
            } catch (XmlRpcException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
                        
        } else {
            forwardQuery(queryId, queryHost, Integer.parseInt(queryPort),fileName.replaceAll(" ", "_"), hopCount - 1);
        }       
        return 1;
    }
       
    public Integer receiveSearchQueryResponce(int queryId,int fileSize,String queryHost,String queryPort, int hopCount,String fileList){
        if (Main.debug) {
           System.out.println("Received Message : " + "SEROK"+" "+queryId+" "+fileSize+" "+queryHost+" "+queryPort+" "+hopCount+" "+fileList);
        }
        String[] tempFiles = fileList.split(" ");
        System.out.println("-----Matching files found for query id:" + queryId + "at host:"
                                + queryHost + " port:" + queryPort + " - Hop Count: " 
                                + (Main.hopCount - hopCount + 1) + "-----");
        for (int i = 0; i < tempFiles.length; i++) {
            System.out.println(" * " + tempFiles[i].replaceAll("_", " "));
        }
        long elapsedTime = (System.currentTimeMillis()-Main.queryStartedTime);
        System.out.println(" Time (ms):" + elapsedTime);
        System.out.println();
        int arrayIndex = queryId;
        if(Main.queryExecutionSummary[arrayIndex][0]==0|| Main.queryExecutionSummary[arrayIndex][0]>elapsedTime){
            Main.queryExecutionSummary[arrayIndex][0] = elapsedTime; 
            Main.queryExecutionSummary[arrayIndex][1]= (Main.hopCount - hopCount + 1);
        }
        return 1;
    }    
    
    public Integer receiveJoin(String joinHost, String joinPort){
        if (Main.debug) {
           System.out.println("Received Message : " + "JOIN"+" "+joinHost+" "+joinPort);
        }
        RoutingTable.getInstance().addNode(joinHost + " " + joinPort);
        return 1;
    }
    
    public Integer receiveLeave(String leaveHost,String leavePort){
        if (Main.debug) {
           System.out.println("Received Message : " + "LEAVE"+" "+leaveHost+" "+leavePort);
        }
        RoutingTable.getInstance().removeNode(leaveHost + " " + leavePort);
        return 1;
    }    
    
    @Override
    public void run() {
         String port = dfsProperties.getProperty("node.port", "");
         String host = dfsProperties.getProperty("node.host", "");
         final WebServer server = new WebServer(Integer.valueOf(port));
         server.addHandler("rpc", instance);
         server.start();        
         Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    //When shutting down close the UDP Socket
                    server.shutdown();
                }
        });
    }

    @Override
    public void forwardQuery(int queryId, String ip, int port, String input, int i) {
        input = input.replaceAll(" ", "_");
        String command = "SER " + queryId + " " + ip + " " + port + " " + input + " " + i;
        int length = command.toCharArray().length;
        length += 5;
        command = String.format("%04d", length) + " " + command;
        if (i > 0) {
            ArrayList<String> peers = RoutingTable.getInstance().getNodes();
            for (int j = 0; j < peers.size(); j++) {
                String peerHost = peers.get(j).split(" ")[0];
                String peerPort = peers.get(j).split(" ")[1];
                //UDPClient udpClient = new UDPClient();
                try {
                    XmlRpcClient server = new XmlRpcClient(peerHost,Integer.valueOf(peerPort)); 
                    Vector params = new Vector();         
                    params.addElement(new Integer(queryId));
                    params.addElement(ip);
                    params.addElement(String.valueOf(port));
                    params.addElement(input);
                    params.addElement(i);
                    Object result = server.execute("rpc.receiveSearchQuery", params);
                    //udpClient.sendAndForgetQuery(command, peerHost,Integer.parseInt(peerPort));
                    Main.forwardedMessageCounts[queryId]++;
                    if (Main.debug) {
                        System.out.println("Sent SEARCH Message " + command + " to " + peerHost +":"+peerPort);
                    }
                } catch (XmlRpcException ex) {
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
            //UDPClient udpClient = new UDPClient();            
            try {
                XmlRpcClient server = new XmlRpcClient(peerHost,Integer.valueOf(peerPort)); 
                Vector params = new Vector();         
                params.addElement(host);
                params.addElement(port);
                Object result = server.execute("rpc.receiveJoin", params);
                //udpClient.sendAndForgetQuery(command, peerHost, peerPort);
                if (Main.debug) {
                    System.out.println("Sent JOIN Message :" + command 
                            + " to "+peerHost+":"+peerPort);
                }
            } catch (XmlRpcException ex) {
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
            //UDPClient udpClient = new UDPClient();
            try {
                XmlRpcClient server = new XmlRpcClient(peerHost,Integer.valueOf(peerPort)); 
                Vector params = new Vector();         
                params.addElement(host);
                params.addElement(port);
                Object result = server.execute("rpc.receiveLeave", params);
                //udpClient.sendAndForgetQuery(command, peerHost, peerPort);
                if (Main.debug) {
                    System.out.println("Sent LEAVE Message :" + command 
                            + " to "+peerHost+":"+peerPort);
                }
            } catch (XmlRpcException ex) {
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
