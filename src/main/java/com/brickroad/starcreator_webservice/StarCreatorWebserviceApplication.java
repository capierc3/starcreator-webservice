package com.brickroad.starcreator_webservice;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(
		info = @Info(title = "Galaxy Creator API", version = "0.0.1", description = "API for calling the galaxy creator web service to generate Planets and Solar Systems"),
		servers = {
				@Server(url = "http://localhost:8080", description = "Local Development Server"),
				@Server(url = "https://Galaxy-Creator.com", description = "Production Server")
		})
public class StarCreatorWebserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(StarCreatorWebserviceApplication.class, args);
	}

}
