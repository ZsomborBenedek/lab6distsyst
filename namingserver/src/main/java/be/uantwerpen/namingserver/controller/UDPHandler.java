package be.uantwerpen.namingserver.controller;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;

import be.uantwerpen.namingserver.database.ServerRepository;

public class UDPHandler implements Runnable {

    private ServerRepository repo;

    String thisName;
    String thisIp;

    public UDPHandler(ServerRepository repo, InetAddress inetAddress) {
        this.repo = repo;
        this.thisName = inetAddress.getHostName();
        this.thisIp = inetAddress.getHostAddress();
        System.out.println("Name: " + thisName);
        System.out.println("IP: " + thisIp);
        System.out.println("--Started--");
    }

    private void sendUDPMessage(String message, String ipAddress, int port) throws IOException {
        DatagramSocket socket = new DatagramSocket();
        InetAddress group = InetAddress.getByName(ipAddress);
        byte[] msg = message.getBytes();
        DatagramPacket packet = new DatagramPacket(msg, msg.length, group, port);
        socket.send(packet);
        socket.close();
    }

    private void receiveUDPMessageHandler(int port) throws IOException {
        byte[] buffer = new byte[1024];
        MulticastSocket socket = new MulticastSocket(port);
        InetAddress group = InetAddress.getByName("230.0.0.0");
        socket.joinGroup(group);
        while (true) {
            System.out.println("Waiting for multicast message...");
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            socket.receive(packet);
            String msg = new String(packet.getData(), packet.getOffset(), packet.getLength());
            if (msg.contains("next "))
                System.out.println("next forwarded: " + msg);
            if (msg.contains("previous "))
                System.out.println("previous forwarded: " + msg);
            if (msg.contains("newNode")) {
                String haha = msg.replace("newNode ", "");
                if (!haha.isEmpty()) {
                    String[] tokens = haha.split("::");
                    repo.addNode(tokens[0], tokens[1]);
                }
                System.out.println("Node added: " + haha);
                System.out.println("NodeCount is " + repo.getNodes().size());
                sendUDPMessage("nodeCount " + repo.getNodes().size(), "230.0.0.0", 10000);
                sendUDPMessage("namingServer " + thisIp, "230.0.0.0", 10000);
            }
            if (msg.contains("remNode")) {
                String haha = msg.replace("remNode ", "");
                if (!haha.isEmpty()) {
                    String[] tokens = haha.split("::");
                    repo.removeNode(tokens[0]);
                }
                System.out.println("Node removed: " + haha);
                System.out.println("NodeCount is " + repo.getNodes().size());
                sendUDPMessage("nodeCount " + repo.getNodes().size(), "230.0.0.0", 10000);
            }
            if ("OK".equals(msg)) {
                System.out.println("No more message. Exiting : " + msg);
                break;
            }
        }
        socket.leaveGroup(group);
        socket.close();
    }

    @Override
    public void run() {
        try {
            receiveUDPMessageHandler(10000);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}