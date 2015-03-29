package cs4262.dfs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConsoleReader extends Thread {

    private FileRepository fileRepository;
    private RoutingTable routingTable;
    private BootstrapClient bootstrapClient;
    private Node node;
    
    public ConsoleReader(Node node){
        this.fileRepository = FileRepository.getInstance();
        this.routingTable = RoutingTable.getInstance();
        this.bootstrapClient = new BootstrapClient();
        this.node = node;
    }
    
    @Override
    @SuppressWarnings("empty-statement")
    public void run() {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        try {
            System.out.println("-----------Distributed File Sharing System-----------");
            System.out.println(" * Type the file name you want to search");
            System.out.println(" * Type r to see the routing table");
            System.out.println(" * Type l to local files in the node");
            System.out.println(" * Type q to gracefully stop the node");            
            System.out.println("-----------------------------------------------------");
            while (true) {
                String input = in.readLine();
                if(input.trim().equals("q")){
                    System.exit(0);
                }else if(input.trim().equals("r")){
                    printRoutingTable();
                }else if(input.trim().equals("l")){
                    FileRepository.getInstance().printFileList();
                }else if(input.length()>0){
                    //Query for a file
                    if(fileRepository.checkFileExists(input)){
                        ArrayList<String> matchingFiles = fileRepository
                                .getAllFilesForQuery(input);
                        System.out.println("--------Local node contains matching files-------");
                        for(int i=0;i<matchingFiles.size();i++){
                            System.out.println(" * "+matchingFiles.get(i));
                        }
                        System.out.println("-------------------------------------------------");
                    }else{
                        System.out.println("Not Found in Local Node."
                                + "\nPassing the message to neighbors");
                        String ip = RoutingTable.getInstance().getMyIP();
                        int port = RoutingTable.getInstance().getPort();
                        
                        //hop count is set to
                        node.forwardQuery(ip, port, input, Main.hopCount);
                    }
                }
            }
        } catch (IOException ex) {
            if(Main.debug)
            Logger.getLogger(ConsoleReader.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void printRoutingTable() {
        System.out.println("-----------Printing Routing Entries-------------");
        for(int i=0;i<routingTable.getNodes().size();i++){
            System.out.println("routing entry "+(i+1)+": "+ routingTable.getNodes().get(i));
        }
        System.out.println("-----------End of routing entries---------------");
    }
}
