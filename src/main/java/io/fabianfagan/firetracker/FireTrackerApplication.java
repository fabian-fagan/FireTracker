package io.fabianfagan.firetracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling //scheduling enabled for daily updates

/**
 * Entry point for application (Spring default)
 */
public class FireTrackerApplication {

	public static void main(String[] args) {
		SpringApplication.run(FireTrackerApplication.class, args);
	}

}
