package be.uantwerpen.client;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;

public class Main implements Runnable {

    @Autowired
    RestTemplate restTemplate;

    InetAddress inetAddress;
    String name;
    String thisIp;
    String previous;
    String next;
    String previousIP;
    String nextIP;
    boolean setupb;
    ArrayList<String> files = new ArrayList<>();
    boolean first;
    boolean running;

    public Main() throws IOException {
        inetAddress = InetAddress.getLocalHost();
        name = inetAddress.getHostName();
        thisIp = inetAddress.getHostAddress();
        previousIP = "";
        nextIP = "";
        first = false;
        running = true;
        sendUDPMessage("newNode:" + name + "::" + thisIp, "230.0.0.0", 10000);
        System.out.println("Name: " + name);
        System.out.println("Ip: " + thisIp);
        checkFiles();
        System.out.println("Starting...");
        setupb = false;
    }

    //Send UDP Messages
    public static void sendUDPMessage(String message,
                                      String ipAddress, int port) throws IOException {
        DatagramSocket socket = new DatagramSocket();
        InetAddress group = InetAddress.getByName(ipAddress);
        byte[] msg = message.getBytes();
        DatagramPacket packet = new DatagramPacket(msg, msg.length,
                group, port);
        socket.send(packet);
        socket.close();
    }

    //Recieve UDP Messages
    public void receiveUDPMessage(String ip, int port) throws
            IOException {
        byte[] buffer = new byte[1024];
        MulticastSocket socket = new MulticastSocket(port);
        SocketAddress group = new InetSocketAddress(InetAddress.getByName(ip), port);
        socket.joinGroup(group, null);
        while (running) {
            System.out.println("Waiting for multicast message...");
            DatagramPacket packet = new DatagramPacket(buffer,
                    buffer.length);
            socket.receive(packet);
            String msg = new String(packet.getData(),
                    packet.getOffset(), packet.getLength());
            System.out.println(msg);
            if (msg.contains("newNode"))
                getNameAndIp(msg);
            else if (msg.contains("nodeCount")) {
                setUp(msg);
            } else if (msg.contains("shutdown"))
                shutdown();
        }
        socket.leaveGroup(group, null);
        socket.close();
    }

    public void receiveUDPUnicastMessage(String ip, int port) throws
            IOException {
        byte[] buffer = new byte[1024];
        MulticastSocket socket = new MulticastSocket(port);
        SocketAddress group = new InetSocketAddress(InetAddress.getByName(ip), port);
        socket.joinGroup(group, null);
        while (true) {
            System.out.println("Waiting for multicast message...");
            DatagramPacket packet = new DatagramPacket(buffer,
                    buffer.length);
            socket.receive(packet);
            String msg = new String(packet.getData(),
                    packet.getOffset(), packet.getLength());
            System.out.println(msg);
            if (msg.contains("nodeCount"))
                setUp(msg);
            if (msg.contains("newNode"))
                getNameAndIp(msg);
            if (msg.contains("previous"))
                previous(msg);
            else if (msg.contains("next"))
                next(msg);
            if ("shutdown".equals(msg)) {
                shutdown();
                break;
            }
        }
        socket.leaveGroup(group, null);
        socket.close();
    }

    //Parse message to set up new next node
    private void next(String msg) {
        String haha = msg.replace("next:", "");
        if (!haha.isEmpty()) {
            String[] tokens = haha.split("::");
            next = tokens[0];
            nextIP = tokens[1];
            System.out.println("The next is " + tokens[0] + " | " + tokens[1]);
        }
    }

    //Parse message to set up new previous node
    private void previous(String msg) {
        String haha = msg.replace("previous:", "");
        if (!haha.isEmpty()) {
            String[] tokens = haha.split("::");
            previous = tokens[0];
            previousIP = tokens[1];
            System.out.println("The previous is " + tokens[0] + " | " + tokens[1]);
        }
    }

    //Parse name and IP of other nodes From UDP MultiCast messages
    private void getNameAndIp(String msg) throws IOException {
        ArrayList<String> temp = new ArrayList<>();
        if (msg.contains("newNode")) {
            String s = msg.replace("newNode:", "");
            if (!s.isEmpty()) {
                String[] tokens = s.split("::");
                temp.addAll(Arrays.asList(tokens));
            }

        }
        if (setupb) {
            if (first) {
                System.out.println("The second:");
                sendUDPMessage("previous:" + name + "::ip " + thisIp, temp.get(1), 10000);
                sendUDPMessage("next:" + name + "::ip " + thisIp, temp.get(1), 10000);
                next = temp.get(0);
                nextIP = temp.get(1);
                previous = temp.get(0);
                previousIP = temp.get(1);
                System.out.println("The next node: " + next + " | " + nextIP);
                System.out.println("The previous node: " + previous + " | " + previousIP);
                first = false;
            } else {
                if (hashFunction(name, true) < hashFunction(temp.get(0), true) && hashFunction(temp.get(0), true) < hashFunction(next, true)) {
                    sendUDPMessage("previous:" + name + "::ip " + thisIp, temp.get(1), 10000);
                    next = temp.get(0);
                    nextIP = temp.get(1);
                }
                if (hashFunction(previous, true) < hashFunction(temp.get(0), true) && hashFunction(temp.get(0), true) < hashFunction(name, true)) {
                    sendUDPMessage("next:" + name + "::ip " + thisIp, temp.get(1), 10000);
                    previous = temp.get(0);
                    previousIP = temp.get(1);
                }
            }
        }
    }

    //Check locally stored files
    private void checkFiles() {
        File folder = new File("Files");
        File[] listOfFiles = folder.listFiles();
        try {
            if (listOfFiles != null)
                for (File file : listOfFiles) {
                    if (file.isFile()) {
                        String fileString = file.getName().replace("Files\\", "");
                        files.add(fileString);
                        System.out.println("A local file " + fileString);
                    } else if (file.isDirectory()) {
                        System.out.println("Directory " + file.getName());
                    }
                }
        } catch (NullPointerException ex) {
            System.out.println(ex.getMessage());
            System.out.println("Cannot find files!");
        }
    }

    //ShutDown
    private void shutdown() throws IOException {
        sendUDPMessage("next:" + next + "::" + nextIP, previousIP, 10000);
        sendUDPMessage("previous:" + previous + "::" + previousIP, nextIP, 10000);
        for (String file : files) {
            sendUDPMessage("File:" + file, previousIP, 10000);
        }
        running = false;
        System.out.println("thread shut down");
    }

    private void setUp(String msg) {
        String haha = msg.replace("nodeCount ", "");
        if (Integer.parseInt(haha) <= 1) {
            System.out.println("This is the first node.");
            next = previous = name;
            nextIP = previousIP = thisIp;
            first = true;
        }
        setupb = true;
    }

    //Hash function, boolean specifies if the string is a node or not
    private int hashFunction(String name, boolean node) {
        int hash = 0;
        int temp = 0;
        int i;
        for (i = 0; i < name.length(); i++) {
            hash = 3 * hash + name.charAt(i);
            temp = temp + name.charAt(i);
        }
        hash = hash / (temp / 7);

        if (node) {
            hash = (hash) / (5);
        } else
            hash = hash / 53;
        return hash;
    }

    @Override
    public void run() {
        while (running) {
            try {
                receiveUDPUnicastMessage("230.0.0.0", 10000);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}

