package com.pennypilot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PennyPilotApplication {

	public static void main(String[] args) {
		SpringApplication.run(PennyPilotApplication.class, args);
	}

}
