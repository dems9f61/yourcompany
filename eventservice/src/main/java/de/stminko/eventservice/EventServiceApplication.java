package de.stminko.eventservice;

import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

/**
 * The {@code EventServiceApplication} class is the main entry point for the Event Service
 * application, a Spring Boot based application. This class is responsible for
 * bootstrapping and launching the application, initializing the Spring application
 * context, and setting up necessary configurations.
 *
 * <p>
 * It is annotated with {@code @SpringBootApplication}, signifying that it is a Spring
 * Boot application. This annotation encompasses {@code @Configuration},
 * {@code @EnableAutoConfiguration}, and {@code @ComponentScan}, enabling
 * auto-configuration, component scanning, and configuration management within the
 * application.
 * </p>
 *
 * <p>
 * Additionally, the class is annotated with {@code @EnableMongoAuditing}. This annotation
 * enables MongoDB auditing via Spring Data, allowing for automatic capturing of
 * audit-related information such as timestamps and user information on MongoDB documents
 * whenever they are created or modified.
 * </p>
 *
 * <p>
 * The {@code main} method sets the default time zone to 'Europe/Berlin' and launches the
 * Spring application. This setup ensures that all date/time operations within the
 * application are aligned with the specified time zone.
 * </p>
 *
 * <p>
 * Usage: To run the Event Service application, execute this class as a Java application.
 * It will start up the embedded web server, initialize the Spring ApplicationContext, and
 * perform any other bootstrapping tasks defined in the application context.
 * </p>
 *
 * @author Stéphan Minko
 */
@EnableMongoAuditing
@SpringBootApplication
public class EventServiceApplication {

	public static void main(String[] args) {
		TimeZone.setDefault(TimeZone.getTimeZone("Europe/Berlin"));
		SpringApplication.run(EventServiceApplication.class, args);
	}

}
