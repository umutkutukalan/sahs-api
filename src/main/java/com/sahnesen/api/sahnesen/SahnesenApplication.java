package com.sahnesen.api.sahnesen;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.sahnesen.api.sahnesen.config.ConfigLoader;

@SpringBootApplication
public class SahnesenApplication {

	public static void main(String[] args) {
		ConfigLoader.load();
		SpringApplication.run(SahnesenApplication.class, args);
	}

}
