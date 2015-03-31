package cs4262.dfs.thrift;

import cs4262.dfs.FileRepository;
import cs4262.dfs.Main;
import cs4262.dfs.MessageWindow;
import cs4262.dfs.RoutingTable;
import cs4262.dfs.utils.DFSProperties;
import java.util.ArrayList;
import java.util.List;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

public class ThriftNodeAPIHandler implements ThriftNodeAPI.Iface {

    private final ThriftNode thriftNode;

    public ThriftNodeAPIHandler() {
        this.thriftNode = ThriftNode.getInstance();
    }

    @Override
    public void join(String joiningHost, int joiningPort) throws TException {
        String command = "JOIN " + joiningHost + " " + joiningPort;
        RoutingTable.getInstance().addNode(joiningHost + " " + joiningPort);
        if (Main.debug) {
            System.out.println("Received JOIN message :" + command);
        }
    }

    @Override
    public void leave(String leavingHost, int leavingPort) throws TException {
        String command = "LEAVE " + leavingHost + " " + leavingPort;
        RoutingTable.getInstance().removeNode(leavingHost + " " + leavingPort);
        if (Main.debug) {
            System.out.println("Received LEAVE message :" + command);
        }
    }

    @Override
    public void ser(int queryId, String queryingHost, int queryingPort, String fileName, int hopCount)
            throws TException {
        String command = "SER " + queryId + " " + queryingHost + " " + queryingPort + " " 
                + fileName + " " + hopCount;
        if(!MessageWindow.getInstance().shouldCheckOrForwardMessage(queryId)){
            return;
        }       
        if (FileRepository.getInstance().checkFileExists(fileName)) {
            ArrayList<String> matchingFiles = FileRepository.getInstance()
                    .getAllFilesForQuery(fileName);
            if (Main.debug) {
                System.out.println("This node has a matching file for the query :" +command);
            }
            TTransport transport;
            transport = new TSocket(queryingHost, queryingPort);
            transport.open();
            TProtocol protocol = new TBinaryProtocol(transport);
            ThriftNodeAPI.Client client = new ThriftNodeAPI.Client(protocol);

            String thisHost = DFSProperties.getInstance().getProperty("node.host", "");
            String thisPort = DFSProperties.getInstance().getProperty("node.port", "");
            client.serok(queryId, thisHost, Integer.parseInt(thisPort), matchingFiles, hopCount);
        } else {
            thriftNode.forwardQuery(queryId, queryingHost, queryingPort, fileName, hopCount - 1);
        }
    }

    @Override
    public void serok(int queryId, String foundHost, int foundPort, List<String> matchingFiles, int hopCount)
            throws TException {
        System.out.println("-----Matching files found for query id:" + queryId +" at host:"
                + foundHost + " port:" + foundPort + " - Hop Count: " 
                + (Main.hopCount-hopCount+1) + "-----");
        for (int i = 0; i < matchingFiles.size(); i++) {
            System.out.println(" * " + matchingFiles.get(i));
        }
        long elapsedTime = (System.currentTimeMillis()-Main.queryStartedTime);
        System.out.println(" Time (ms):" + elapsedTime);
        System.out.println();
        
        if(Main.queryExecutionSummary[queryId][0] == 0
                || Main.queryExecutionSummary[queryId][0]>elapsedTime){
            Main.queryExecutionSummary[queryId][0] = elapsedTime;
            Main.queryExecutionSummary[queryId][1] = (Main.hopCount-hopCount+1) ;
        }
    }

}
