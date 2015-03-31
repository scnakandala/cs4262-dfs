package cs4262.dfs;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PerformanceTest extends Thread {

    private final Node node;

    public PerformanceTest(Node node) {
        this.node = node;
    }

    @Override
    public void run() {
        try {
            executeQueries();
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    private void executeQueries() throws FileNotFoundException, IOException,
            InterruptedException {
        BufferedReader reader = new BufferedReader(new FileReader("queries.csv"));
        String temp = reader.readLine();
        temp = reader.readLine();
        while (temp != null && !temp.isEmpty()) {
            int queryId = Integer.parseInt(temp.split(",")[0].trim());
            String query = temp.split(",")[1].trim();
            System.out.println("     Executing query id:" + queryId + " file name:" + query);
            //Query for a file
            Main.queryStartedTime = System.currentTimeMillis();
            if (FileRepository.getInstance().checkFileExists(query)) {
                long elapasedTime = System.currentTimeMillis() - Main.queryStartedTime;
                Main.queryExecutionSummary[queryId][0] = elapasedTime;
                Main.queryExecutionSummary[queryId][1] = 0;
            } else {
                String ip = RoutingTable.getInstance().getMyIP();
                int port = RoutingTable.getInstance().getPort();
                MessageWindow.getInstance().shouldCheckOrForwardMessage(queryId);
                node.forwardQuery(queryId, ip, port, query, Main.hopCount);
            }

            Thread.sleep(1000);
            temp = reader.readLine();
        }
    }
}
