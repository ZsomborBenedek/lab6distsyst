package be.uantwerpen.namingserver;

import javax.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
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

	@PostConstruct
	public void init() {
		ServerRepository repo = new ServerRepository();
		UDPHandler udpHandler = new UDPHandler(repo);
		udpHandler.run();
	}
}
