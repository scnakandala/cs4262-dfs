package cs4262.dfs;

public abstract class Node extends Thread{
    public abstract void forwardQuery(String ip, int port, String input, int i);
    public abstract void informJoinToPeers();
    public abstract void informLeaveToPeers();
}
