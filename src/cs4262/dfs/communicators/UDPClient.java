package cs4262.dfs.communicators;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class UDPClient {
    
    public String sendAndReceiveQuery(String query,String ip, int port)
            throws SocketException,UnknownHostException,IOException{
        DatagramSocket clientSocket = new DatagramSocket();
        InetAddress IPAddress = InetAddress.getByName(ip);
        byte[] sendData = new byte[1024];
        byte[] receiveData = new byte[1024];
        sendData = query.getBytes();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,
                IPAddress, port);
        clientSocket.send(sendPacket);
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        clientSocket.receive(receivePacket);
        String newQuery =  new String(receivePacket.getData(), 0, receivePacket.getLength());
        return newQuery;
    }

    public void sendAndForgetQuery(String query,String ip, int port)
            throws SocketException,UnknownHostException,IOException{
        DatagramSocket clientSocket = new DatagramSocket();
        InetAddress IPAddress = InetAddress.getByName(ip);
        byte[] sendData = new byte[1024];
        sendData = query.getBytes();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,
                IPAddress, port);
        clientSocket.send(sendPacket);
    }
}
