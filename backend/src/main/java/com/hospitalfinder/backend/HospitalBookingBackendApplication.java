package com.hospitalfinder.backend;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.admin.SpringApplicationAdminJmxAutoConfiguration;
import org.springframework.boot.autoconfigure.jmx.JmxAutoConfiguration;
import org.springframework.boot.autoconfigure.websocket.servlet.WebSocketServletAutoConfiguration;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication(exclude = {
		SpringApplicationAdminJmxAutoConfiguration.class,
		JmxAutoConfiguration.class,
		WebSocketServletAutoConfiguration.class
})
@EnableScheduling
@EnableAsync
public class HospitalBookingBackendApplication {

	private static final Logger logger = LoggerFactory.getLogger(HospitalBookingBackendApplication.class);

	private final Environment env;

	public HospitalBookingBackendApplication(Environment env) {
		this.env = env;
	}

	public static void main(String[] args) {
		// Load .env variables into System properties
		try {
			Dotenv dotenv = Dotenv.configure()
					.directory("./")
					.ignoreIfMissing()
					.load();

			dotenv.entries().forEach(entry -> {
				System.setProperty(entry.getKey(), entry.getValue());
			});
			System.out.println("Loaded .env variables successfully.");
		} catch (Exception e) {
			System.err.println("Failed to load .env: " + e.getMessage());
		}

		SpringApplication.run(HospitalBookingBackendApplication.class, args);
	}

	@EventListener(ApplicationReadyEvent.class)
	public void onApplicationReady() {
		int port = env.getProperty("server.port", Integer.class, 9000);
		logger.info("============================================================");
		logger.info("APPLICATION STARTED SUCCESSFULLY AND READY TO ACCEPT REQUESTS");
		logger.info("Server listening on port: {}", port);
		logger.info("============================================================");
	}

}
