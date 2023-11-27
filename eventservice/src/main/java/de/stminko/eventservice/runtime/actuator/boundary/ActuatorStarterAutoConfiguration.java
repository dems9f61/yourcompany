package de.stminko.eventservice.runtime.actuator.boundary;

import lombok.Data;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * configuration class for initializing Actuator settings in the Event Service
 * application. This class automatically loads properties from
 * 'stminko-actuator.properties' to configure aspects of the application's actuator, such
 * as metrics, health checks, and other management endpoints. The class is annotated
 * with @Data, @Configuration, and @PropertySource to facilitate this configuration.
 *
 * @author Stéphan Minko
 */
@Data
@Configuration
@PropertySource("classpath:config/stminko-actuator.properties")
public class ActuatorStarterAutoConfiguration {

}
