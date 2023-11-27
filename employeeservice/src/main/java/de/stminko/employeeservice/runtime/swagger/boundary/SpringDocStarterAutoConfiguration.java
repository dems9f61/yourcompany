package de.stminko.employeeservice.runtime.swagger.boundary;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * Configuration class for SpringDoc integration in Spring Boot applications.
 *
 * <p>
 * This configuration class is responsible for setting up necessary components for
 * SpringDoc integration. It performs component scanning and loads properties from a
 * specified properties file in the classpath. The properties file,
 * {@code stminko-springdoc.properties}, contains configurations specific to SpringDoc's
 * behavior in the application.
 * </p>
 *
 * <p>
 * This class defines a constant {@code CONFIGURATION_NAMESPACE} that represents the
 * namespace for configuration properties related to SpringDoc. This namespace is used to
 * organize and define properties in a structured manner, making it easier to manage
 * SpringDoc-related configurations.
 * </p>
 *
 * <p>
 * Include this configuration in your Spring Boot application to enable and customize
 * SpringDoc features, such as API documentation and interactive API UIs, according to
 * your application's requirements.
 * </p>
 *
 * @author Stéphan Minko
 */
@Configuration
@PropertySource("classpath:config/stminko-springdoc.properties")
public class SpringDocStarterAutoConfiguration {

	/**
	 * Configuration property namespace that this class is designed to handle.
	 */
	public static final String CONFIGURATION_NAMESPACE = "stminko.springdoc";

}
