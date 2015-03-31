package cs4262.dfs.thrift;

import cs4262.dfs.Main;
import cs4262.dfs.MessageWindow;
import cs4262.dfs.Node;
import cs4262.dfs.RoutingTable;
import cs4262.dfs.utils.DFSProperties;
import java.util.ArrayList;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

public class ThriftNode extends Node {

    private final DFSProperties dfsProperties;
    private static ThriftNode instance;
    private final MessageWindow messageWindow;

    public static ThriftNode getInstance() {
        if (instance == null) {
            instance = new ThriftNode();
        }
        return instance;
    }

    private ThriftNode() {
        this.dfsProperties = DFSProperties.getInstance();
        this.messageWindow = MessageWindow.getInstance();
    }

    @Override
    public void run() {
        try {
            ThriftNodeAPIHandler handler = new ThriftNodeAPIHandler();
            ThriftNodeAPI.Processor<ThriftNodeAPI.Iface> apiServer =
                new ThriftNodeAPI.Processor<ThriftNodeAPI
                        .Iface>(handler);            
            String port = dfsProperties.getProperty("node.port", "");
            TServerTransport serverTransport = new TServerSocket(Integer.parseInt(port));
            TThreadPoolServer.Args options = new TThreadPoolServer.Args(serverTransport);
            options.minWorkerThreads = 30;
            TServer server = new TThreadPoolServer(options.processor(apiServer));
            server.serve();
        } catch (TTransportException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void forwardQuery(int queryId, String ip, int port, String input, int i) {
        String command = "SER " + queryId + " " + ip + " " + port + " " + input;
        if (i > 0) {
            command += " " + i;
            ArrayList<String> peers = RoutingTable.getInstance().getRandomThreeNeighBours();
            for (int j = 0; j < peers.size(); j++) {
                String peerHost = peers.get(j).split(" ")[0];
                String peerPort = peers.get(j).split(" ")[1];
                try {
                    TTransport transport;
                    transport = new TSocket(peerHost, Integer.parseInt(peerPort));
                    transport.open();
                    TProtocol protocol = new TBinaryProtocol(transport);
                    ThriftNodeAPI.Client client = new ThriftNodeAPI.Client(protocol);
                    client.ser(queryId, ip, port, input, i);
                    Main.forwardedMessageCounts[queryId]++;
                    if (Main.debug) {
                        System.out.println("Sent SEARCH Message :" + command 
                            + " to "+peerHost+":"+peerPort);
                    }
                } catch (TTransportException ex) {
                    ex.printStackTrace();
                } catch (TException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    @Override
    public void informJoinToPeers() {
        String host = dfsProperties.getProperty("node.host", "");
        String port = dfsProperties.getProperty("node.port", "");
        String command = "JOIN " + host + " " + port;
        ArrayList<String> peers = RoutingTable.getInstance().getNodes();
        for (int j = 0; j < peers.size(); j++) {
            String peerHost = peers.get(j).split(" ")[0];
            String peerPort = peers.get(j).split(" ")[1];
            try {
                TTransport transport;
                transport = new TSocket(peerHost, Integer.parseInt(peerPort));
                transport.open();
                TProtocol protocol = new TBinaryProtocol(transport);
                ThriftNodeAPI.Client client = new ThriftNodeAPI.Client(protocol);
                client.join(host, Integer.parseInt(port));
                if (Main.debug) {
                    System.out.println("Sent JOIN Message :" + command 
                            + " to "+peerHost+":"+peerPort);
                }
            } catch (TTransportException ex) {
                ex.printStackTrace();
            } catch (TException ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void informLeaveToPeers() {
        String host = dfsProperties.getProperty("node.host", "");
        String port = dfsProperties.getProperty("node.port", "");
        String command = "LEAVE " + host + " " + port;
        ArrayList<String> peers = RoutingTable.getInstance().getNodes();
        for (int j = 0; j < peers.size(); j++) {
            String peerHost = peers.get(j).split(" ")[0];
            String peerPort = peers.get(j).split(" ")[1];
            try {
                TTransport transport;
                transport = new TSocket(peerHost, Integer.parseInt(peerPort));
                transport.open();
                TProtocol protocol = new TBinaryProtocol(transport);
                ThriftNodeAPI.Client client = new ThriftNodeAPI.Client(protocol);
                client.leave(host, Integer.parseInt(port));
                if (Main.debug) {
                   System.out.println("Sent LEAVE Message :" + command 
                            + " to "+peerHost+":"+peerPort);
                }
            } catch (TTransportException ex) {
                ex.printStackTrace();
            } catch (TException ex) {
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
