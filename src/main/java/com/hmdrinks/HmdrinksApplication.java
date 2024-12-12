package com.hmdrinks;

import com.hmdrinks.Config.RateLimitingFilter;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

@OpenAPIDefinition(servers = {@Server(url = "/", description = "HMDrinks Server URL")})
@SpringBootApplication(scanBasePackages = "com.hmdrinks")
public class HmdrinksApplication {
	public static void main(String[] args) {
		SpringApplication.run(HmdrinksApplication.class, args);
	}

}
