package com.jsalva.trainerworkload;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient  // Eureka Client Annotation
public class TrainerworkloadApplication {

	public static void main(String[] args) {
		SpringApplication.run(TrainerworkloadApplication.class, args);
	}

}
