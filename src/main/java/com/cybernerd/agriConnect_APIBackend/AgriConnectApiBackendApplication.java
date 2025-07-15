package com.cybernerd.agriConnect_APIBackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication(scanBasePackages = "com.cybernerd.agriConnect_APIBackend")
@EnableJpaAuditing
public class AgriConnectApiBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(AgriConnectApiBackendApplication.class, args);
	}

}
