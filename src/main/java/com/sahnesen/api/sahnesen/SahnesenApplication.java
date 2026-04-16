package com.sahnesen.api.sahnesen;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

import com.sahnesen.api.sahnesen.config.ConfigLoader;

@EnableCaching // Önbellekleme mekanizmasını etkinleştirir
@SpringBootApplication
public class SahnesenApplication {

	public static void main(String[] args) {
		ConfigLoader.load();
		SpringApplication.run(SahnesenApplication.class, args);
	}

}
