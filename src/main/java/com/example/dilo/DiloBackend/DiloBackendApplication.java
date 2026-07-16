package com.example.dilo.DiloBackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class DiloBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(DiloBackendApplication.class, args);
	}

}
