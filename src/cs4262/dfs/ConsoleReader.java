package cs4262.dfs;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class ConsoleReader extends Thread {

    private final FileRepository fileRepository;
    private final RoutingTable routingTable;
    private final BootstrapClient bootstrapClient;
    private final Node node;

    public ConsoleReader(Node node) {
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
            System.out.println(" * Type the <file_name> you want to search OR");
            System.out.println(" * Type \"r\" to see the routing table");
            System.out.println(" * Type \"l\" to local files in the node");
            System.out.println(" * Type \"q\" to gracefully stop the node");

            System.out.println(" ---------Commands for performance test--------------");
            System.out.println(" * Type \"test\" to run the test queries");
            System.out.println(" * Type \"dump\" to dump the test results");
            System.out.println(" * Type \"reset\" to reset the test statistics");
            System.out.println("-----------------------------------------------------");
            while (true) {
                String input = in.readLine();
                if (input.trim().equals("q")) {
                    System.exit(0);
                } else if (input.trim().equals("r")) {
                    printRoutingTable();
                } else if (input.trim().equals("l")) {
                    FileRepository.getInstance().printFileList();
                } else if (input.trim().equals("test")) {
                    testQueries();
                } else if (input.trim().equals("dump")) {
                    dumpResults();
                } else if (input.trim().equals("reset")) {
                    resetStatistics();
                } else if (input.length() > 0) {
                    //Query for a file
                    Main.queryStartedTime = System.currentTimeMillis();
                    if (fileRepository.checkFileExists(input)) {
                        ArrayList<String> matchingFiles = fileRepository
                                .getAllFilesForQuery(input);
                        System.out.println("--------Local node contains matching files-------");
                        for (int i = 0; i < matchingFiles.size(); i++) {
                            System.out.println(" * " + matchingFiles.get(i));
                        }
                        System.out.println(" Time (ms):" + (System.currentTimeMillis() - Main.queryStartedTime));
                        System.out.println("-------------------------------------------------");
                    } else {
                        System.out.println("Not Found in Local Node."
                                + "\nPassing the message to neighbors");
                        String ip = RoutingTable.getInstance().getMyIP();
                        int port = RoutingTable.getInstance().getPort();

                        int queryId = (int) (10000 * Math.random());
                        //hop count is set to Main.hopCount
                        String command = "SER " + queryId + " " + ip + " " + port + " "
                                + input + " " + Main.hopCount;
                        if (MessageWindow.getInstance()
                                .shouldCheckOrForwardMessage(queryId)) {
                            node.forwardQuery(queryId, ip, port, input, Main.hopCount);
                        }
                    }
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    private void printRoutingTable() {
        System.out.println("-----------Printing Routing Entries-------------");
        for (int i = 0; i < routingTable.getNodes().size(); i++) {
            System.out.println("routing entry " + (i + 1) + ": " + routingTable.getNodes().get(i));
        }
        System.out.println("-----------End of routing entries---------------");
    }

    private void testQueries() throws InterruptedException {
        System.out.println("Executing Test Queries....");
        PerformanceTest performanceTest = new PerformanceTest(node);
        performanceTest.start();

        performanceTest.join();
        System.out.println("Finished Executing Test Queries");
    }

    private void dumpResults() throws IOException {
        System.out.println("Dumping statisitics data.......");
        BufferedWriter writer = new BufferedWriter(new FileWriter("query-summary.csv"));
        writer.write("query id, elapsed time, hop count\n");
        for (int i = 0; i < Main.queryExecutionSummary.length; i++) {
            writer.write(i + ",");
            writer.write(Main.queryExecutionSummary[i][0] + "," + Main.queryExecutionSummary[i][1] + "\n");
        }
        writer.close();

        writer = new BufferedWriter(new FileWriter("message-overhead.csv"));
        writer.write("query id, forwarded messages\n");
        for (int i = 0; i < Main.forwardedMessageCounts.length; i++) {
            writer.write(i + ",");
            writer.write(Main.forwardedMessageCounts[i] + "\n");
        }
        writer.close();
        System.out.println("Finished dumping statisitics data");
    }

    private void resetStatistics() {
        Main.forwardedMessageCounts = new long[250];
        Main.queryExecutionSummary = new long[250][2];
        System.out.println("Reseted statistics successfully");
    }
}
