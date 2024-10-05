package com.hmdrinks;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.hmdrinks")
public class HmdrinksApplication {

	public static void main(String[] args) {
		SpringApplication.run(HmdrinksApplication.class, args);
	}

}
