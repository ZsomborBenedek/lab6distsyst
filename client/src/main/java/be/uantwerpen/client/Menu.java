package be.uantwerpen.client;

import java.io.IOException;
import java.util.Scanner;

public class Menu implements Runnable {

    boolean running = true;
    UDPHandler udpHandler;
    TCPHandler tcpHandler;

    public Menu(UDPHandler udpHandler, TCPHandler tcpHandler) {
        this.udpHandler = udpHandler;
        this.tcpHandler = tcpHandler;
        udpHandler.run();
    }

    @Override
    public void run() {
        while (running) {

            System.out.println("What do you want to do?");
            System.out.println("1 - Request a file.");
            System.out.println("2 - Shut down the node.");
            Scanner input = new Scanner(System.in);
            int choice = input.nextInt();

            switch (choice) {
                case 1:
                    tcpHandler.setNamingServerIp(udpHandler.getNamingServerIp());
                    System.out.println("Enter the filename!");
                    String fileToFind = input.nextLine();
                    Node fileOwner = tcpHandler.getFileOwnerIp(fileToFind);
                    System.out.println("Owner node of file:");
                    System.out.println(fileOwner.toString());
                    break;
                case 2:
                    System.out.println("Shutting down...");
                    try {
                        udpHandler.shutdown();
                        running = false;
                    } catch (IOException e) {
                        e.printStackTrace();
                        System.out.println("Could not shutdown node: error accessing files!");
                    }
                    break;
                default:
                    System.out.println("Choose an option!");
                    break;
            }

            input.close();
        }
    }

}