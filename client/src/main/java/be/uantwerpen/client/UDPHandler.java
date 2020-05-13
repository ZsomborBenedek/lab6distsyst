package be.uantwerpen.client;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.SocketAddress;
import java.util.ArrayList;

public class UDPHandler implements Runnable {

    String thisName;
    String thisIp;
    String previousName;
    String previousIP;
    String nextName;
    String nextIP;
    String namingServerIp;

    boolean setupb;
    boolean first;
    boolean running;

    ArrayList<String> files = new ArrayList<>();

    /**
     * @return the namingServerIp
     */
    public String getNamingServerIp() {
        return namingServerIp;
    }

    public UDPHandler(InetAddress inetAddress) throws IOException {
        thisName = inetAddress.getHostName();
        thisIp = inetAddress.getHostAddress();
        previousIP = "";
        nextIP = "";
        first = false;
        running = true;
        namingServerIp = "";
        sendUDPMessage("newNode:" + thisName + "::" + thisIp, "230.0.0.0", 10000);
        System.out.println("Name: " + thisName);
        System.out.println("Ip: " + thisIp);
        System.out.println("Starting...");
        setupb = false;
        checkFiles();
    }

    // Check locally stored files
    private void checkFiles() {
        File folder = new File("src/main/java/be/uantwerpen/client/Files");
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

    // Send UDP Messages
    public void sendUDPMessage(String message, String ipAddress, int port) throws IOException {
        DatagramSocket socket = new DatagramSocket();
        InetAddress group = InetAddress.getByName(ipAddress);
        byte[] msg = message.getBytes();
        DatagramPacket packet = new DatagramPacket(msg, msg.length, group, port);
        socket.send(packet);
        socket.close();
    }

    // Recieve UDP Messages
    public void receiveUDPMessage(String ip, int port) throws IOException {
        byte[] buffer = new byte[1024];
        MulticastSocket socket = new MulticastSocket(port);
        SocketAddress group = new InetSocketAddress(InetAddress.getByName(ip), port);
        socket.joinGroup(group, null);
        while (running) {
            System.out.println("Waiting for multicast message...");
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            socket.receive(packet);
            String msg = new String(packet.getData(), packet.getOffset(), packet.getLength());
            System.out.println(msg);
            if (msg.contains("newNode"))
                getNameAndIp(msg.replace("newNode ", ""));
            else if (msg.contains("nodeCount"))
                setUp(msg.replace("nodeCount ", ""));
            else if (msg.contains("namingServer")){
                getNameAndIp(msg.replace("namingServer ", ""));
                System.out.println("Naming server: " + namingServerIp);
            }
            else if (msg.contains("shutdown"))
                shutdown();
        }
        socket.leaveGroup(group, null);
        socket.close();
    }

    public void receiveUDPUnicastMessage(String ip, int port) throws IOException {
        byte[] buffer = new byte[1024];
        MulticastSocket socket = new MulticastSocket(port);
        SocketAddress group = new InetSocketAddress(InetAddress.getByName(ip), port);
        socket.joinGroup(group, null);
        while (true) {
            System.out.println("Waiting for multicast message...");
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            socket.receive(packet);
            String msg = new String(packet.getData(), packet.getOffset(), packet.getLength());
            System.out.println(msg);
            if (msg.contains("nodeCount"))
                setUp(msg.replace("nodeCount ", ""));
            else if (msg.contains("newNode"))
                getNameAndIp(msg.replace("newNode ", ""));
            else if (msg.contains("previous"))
                previous(msg.replace("previous ", ""));
            else if (msg.contains("next"))
                next(msg.replace("next ", ""));
            else if (msg.contains("namingServer")){
                namingServerIp = msg.replace("namingServer ", "");
                System.out.println("Naming server: " + namingServerIp);
            }
            else if ("shutdown".equals(msg)) {
                shutdown();
                break;
            }
        }
        socket.leaveGroup(group, null);
        socket.close();
    }

    // Parse message to set up new next node
    private void next(String msg) {
        if (!msg.isEmpty()) {
            String[] tokens = msg.split("::");
            nextName = tokens[0];
            nextIP = tokens[1];
            System.out.println("The next is " + tokens[0] + " | " + tokens[1]);
        }
    }

    // Parse message to set up new previous node
    private void previous(String msg) {
        if (!msg.isEmpty()) {
            String[] tokens = msg.split("::");
            previousName = tokens[0];
            previousIP = tokens[1];
            System.out.println("The previous is " + tokens[0] + " | " + tokens[1]);
        }
    }

    // Parse name and IP of other nodes From UDP MultiCast messages
    private void getNameAndIp(String msg) throws IOException { // true if parameter is naming server
        if (!msg.isEmpty()) {
            String name = msg.split("::")[0];
            String ip = msg.split("::")[1];
            if (setupb) {
                if (first) {
                    System.out.println("The second:");
                    sendUDPMessage("previous " + thisName + "::ip " + thisIp, ip, 10000);
                    sendUDPMessage("next " + thisName + "::ip " + thisIp, ip, 10000);
                    nextName = name;
                    nextIP = ip;
                    previousName = name;
                    previousIP = ip;
                    System.out.println("The next node: " + nextName + " | " + nextIP);
                    System.out.println("The previous node: " + previousName + " | " + previousIP);
                    first = false;
                } else {
                    if (hashFunction(thisName, true) < hashFunction(name, true)
                            && hashFunction(name, true) < hashFunction(nextName, true)) {
                        sendUDPMessage("previous:" + thisName + "::ip " + thisIp, ip, 10000);
                        nextName = name;
                        nextIP = ip;
                    }
                    if (hashFunction(previousName, true) < hashFunction(name, true)
                            && hashFunction(name, true) < hashFunction(thisName, true)) {
                        sendUDPMessage("next:" + thisName + "::ip " + thisIp, ip, 10000);
                        previousName = ip;
                        previousIP = ip;
                    }
                }
            }
        }
    }

    // ShutDown
    public void shutdown() throws IOException {
        sendUDPMessage("next " + nextName + "::" + nextIP, previousIP, 10000);
        sendUDPMessage("previous " + previousName + "::" + previousIP, nextIP, 10000);
        for (String file : files) {
            sendUDPMessage("shutdownFile " + file, previousIP, 10000);
        }
        running = false;
        System.out.println("thread shut down");
    }

    private void setUp(String msg) {
        if (Integer.parseInt(msg) <= 1) {
            System.out.println("This is the first node.");
            nextName = previousName = thisName;
            nextIP = previousIP = thisIp;
            first = true;
        }
        setupb = true;
    }

    // Hash function, boolean specifies if the string is a node or not
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