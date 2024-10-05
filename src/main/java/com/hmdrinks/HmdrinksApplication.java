package com.hmdrinks;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.hmdrinks")
@EnableJpaRepositories(basePackages = "com.hmdrinks.Repository")
public class HmdrinksApplication {

	public static void main(String[] args) {
		SpringApplication.run(HmdrinksApplication.class, args);
	}

}
