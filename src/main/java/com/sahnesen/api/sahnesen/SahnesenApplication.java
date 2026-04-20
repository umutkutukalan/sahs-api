package com.sahnesen.api.sahnesen;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.sahnesen.api.sahnesen.config.ConfigLoader;

@SpringBootApplication
@EnableScheduling
/**
 * Scheduler'ların çalışabilmesi için bu anotasyonu ekliyoruz (Spring'e "Ben
 * arkada iş koşturacağım" diyoruz)
 */
public class SahnesenApplication {

	public static void main(String[] args) {
		ConfigLoader.load();
		SpringApplication.run(SahnesenApplication.class, args);
	}

}
