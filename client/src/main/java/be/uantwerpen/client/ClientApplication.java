package be.uantwerpen.client;

import java.net.InetAddress;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class ClientApplication {

	public static void main(String[] args) {
		SpringApplication.run(ClientApplication.class, args);
	}

	@Bean
	public void init() throws Exception {
	 	InetAddress	inetAddress = InetAddress.getLocalHost();
		UDPHandler udpHandler = new UDPHandler(inetAddress);
		TCPHandler tcpHandler = new TCPHandler(inetAddress);
		Menu menu = new Menu(udpHandler, tcpHandler);
		menu.run();
	}
}
