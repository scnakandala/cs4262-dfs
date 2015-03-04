package cs4262.dfs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConsoleReader extends Thread {

    private FileRepository fileRepository;
    private RoutingTable routingTable;
    private BootstrapClient bootstrapClient;
    public ConsoleReader(){
        this.fileRepository = FileRepository.getInstance();
        this.routingTable = RoutingTable.getInstance();
        this.bootstrapClient = new BootstrapClient();
    }
    
    @Override
    public void run() {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        try {
            System.out.println("-----------Distributed File Sharing System-----------");
            System.out.println("\tType the file name you want to search");
            System.out.println("\tType r to see the routing table");
            System.out.println("\tType q to gracefully stop the node");
            System.out.println("-----------------------------------------------------");
            while (true) {
                String input = in.readLine();
                if(input.trim().equals("q")){
                    //node is going down
                    this.bootstrapClient.unregister();
                    
                    //Todo
                    //has to inform other connected neighbours
                    
                    System.exit(0);
                }else if(input.trim().equals("r")){
                    printRoutingTable();
                }else{
                    //Query for a file
                    if(fileRepository.checkFileExists(input)){
                        System.out.println("local node contains the file "+input);
                    }else{
                        System.out.println("Not Found in Local Node."
                                + "\nPassing the message to neighbors");
                        Communicator.sendQuery(input);
                        //Todo
                        //Pass the query to neighbours
                    }
                }
            }
        } catch (IOException ex) {
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
