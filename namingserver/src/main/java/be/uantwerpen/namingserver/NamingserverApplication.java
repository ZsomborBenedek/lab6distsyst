package be.uantwerpen.namingserver;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.web.client.RestTemplate;
import be.uantwerpen.namingserver.controller.UDPHandler;
import be.uantwerpen.namingserver.database.ServerRepository;

@SpringBootApplication
public class NamingserverApplication {

	public static void main(String[] args) {
		SpringApplication.run(NamingserverApplication.class, args);
	}

	@Bean
	public RestTemplate getRestTemplate() {
		return new RestTemplate();
	}

    @EventListener(ApplicationReadyEvent.class)
	public void init() throws UnknownHostException {
		InetAddress inetAddress = InetAddress.getLocalHost();
		ServerRepository repo = new ServerRepository();
		UDPHandler udpHandler = new UDPHandler(repo, inetAddress);
		udpHandler.run();
	}
}
