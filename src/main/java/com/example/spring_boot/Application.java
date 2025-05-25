package com.example.spring_boot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
@EntityScan("com.example.spring_boot.entity")
public class Application {

	public static void main(String[] args) {
        Dotenv dotenv = Dotenv.load();
        System.setProperty("DB_USERNAME", dotenv.get("DB_USERNAME"));
        System.setProperty("DB_PASSWORD", dotenv.get("DB_PASSWORD"));
		SpringApplication.run(Application.class, args);
	}
}
