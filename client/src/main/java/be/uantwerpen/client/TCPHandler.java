package be.uantwerpen.client;

import java.net.InetAddress;
import org.springframework.web.client.RestTemplate;

public class TCPHandler {

    String thisName;
    String thisIp;
    String namingServerIp;

    /**
     * @param namingServerIp the namingServerIp to set
     */
    public void setNamingServerIp(String namingServerIp) {
        this.namingServerIp = namingServerIp;
    }

    RestTemplate restTemplate;

    public TCPHandler(InetAddress inetAddress) {
        thisName = inetAddress.getHostName();
        thisIp = inetAddress.getHostAddress();
        restTemplate = new RestTemplate();
    }

    public Node getFileOwnerIp(String fileName) {
        if (namingServerIp != "")
            return restTemplate.getForObject(namingServerIp + "/file/where/" + fileName, Node.class);
        else
            System.out.println("No naming server!");
        return null;
    }

}