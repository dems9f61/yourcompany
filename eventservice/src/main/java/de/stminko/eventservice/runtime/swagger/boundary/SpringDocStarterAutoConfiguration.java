package de.stminko.eventservice.runtime.swagger.boundary;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * Configuration class for SpringDoc integration in a Spring application.
 * <p>
 * This class is annotated with {@link Configuration}, {@link ComponentScan},
 * and {@link PropertySource} to set up the necessary configuration for SpringDoc.
 * It defines a namespace for the configuration properties and includes property sources
 * from a specific classpath location.
 * </p>
 *
 * @author Stéphan Minko
 */
@Configuration
@ComponentScan
@PropertySource("classpath:config/stminko-springdoc.properties")
public class SpringDocStarterAutoConfiguration {

    /**
     * Constant representing the configuration namespace for SpringDoc.
     */
    public static final String CONFIGURATION_NAMESPACE = "stminko.springdoc";

}
