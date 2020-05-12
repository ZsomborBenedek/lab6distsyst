package be.uantwerpen.client;

import java.net.InetAddress;

public class Node {
    InetAddress ip;
    String name;

    /**
     * @return the ip
     */
    public InetAddress getIp() {
        return ip;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param ip the ip to set
     */
    public void setIp(InetAddress ip) {
        this.ip = ip;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Node{" + "name=''" + name + '\'' + ", name='" + ip.getHostAddress() + '\'' + '}';
    }
}