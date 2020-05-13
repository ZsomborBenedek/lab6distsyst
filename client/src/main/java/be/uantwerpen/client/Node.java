package be.uantwerpen.client;

public class Node {
    String ip;
    String name;

    /**
     * @return the ip
     */
    public String getIp() {
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
    public void setIp(String ip) {
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
        return "Node{" + "name=''" + name + '\'' + ", name='" + ip + '\'' + '}';
    }
}