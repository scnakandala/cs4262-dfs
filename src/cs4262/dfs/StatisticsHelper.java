package cs4262.dfs;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class StatisticsHelper {

    public static void main(String[] args) throws FileNotFoundException, IOException {
        BufferedReader[] overheadReaders = new BufferedReader[12];
        for (int i = 0; i < overheadReaders.length; i++) {
            overheadReaders[i] = new BufferedReader(new FileReader("output/node" + i + "/message-overhead.csv"));
            overheadReaders[i].readLine();
        }

        BufferedReader[] queryResultsReaders = new BufferedReader[5];
        for (int i = 0; i < queryResultsReaders.length; i++) {
            queryResultsReaders[i] = new BufferedReader(new FileReader("output/node" + i + "/query-summary.csv"));
            queryResultsReaders[i].readLine();
        }

        BufferedWriter writer = new BufferedWriter(new FileWriter("summary.csv"));
        String[] overheadStrings = new String[12];
        String[] queryResultStrings = new String[5];

        writer.write("id,time,hops,node0,node1,node2,node3,node4,node5,"
                + "node6,node7,node8,node9,node10,node11\n");
        for (int i = 0; i < 250; i++) {
            String temp = "";

            for (int j = 0; j < queryResultsReaders.length; j++) {
                queryResultStrings[j] = queryResultsReaders[j].readLine();
            }
            if (i < 50) {
                temp += queryResultStrings[0];
            } else if (i < 100) {
                temp += queryResultStrings[1];
            } else if (i < 150) {
                temp += queryResultStrings[2];
            } else if (i < 200) {
                temp += queryResultStrings[3];
            } else if (i < 250) {
                temp += queryResultStrings[4];
            }

            for (int j = 0; j < overheadReaders.length; j++) {
                overheadStrings[j] = overheadReaders[j].readLine().split(",")[1];
                temp += "," + overheadStrings[j];
            }
            writer.write(temp + "\n");
        }
        writer.flush();
        writer.close();
    }
}
