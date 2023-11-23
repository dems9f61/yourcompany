package de.stminko.eventservice.runtime.swagger.entity;

import de.stminko.eventservice.runtime.swagger.boundary.SpringDocStarterAutoConfiguration;
import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties class for customizing SpringDoc in a Spring application.
 * <p>
 * This class is annotated with {@link Data}, {@link Configuration}, and
 * {@link ConfigurationProperties} to bind and manage custom properties for SpringDoc
 * configuration. It defines several properties to control aspects of SpringDoc
 * integration, such as enabling/disabling it, specifying packages and paths
 * to scan for REST controllers and operations.
 * </p>
 *
 * @author Stéphan Minko
 */
@Data
@Configuration
@ConfigurationProperties(SpringDocStarterAutoConfiguration.CONFIGURATION_NAMESPACE)
public class CustomSpringDocProperties {

    /**
     * Flag to enable SpringDoc integration. Default is {@code true}.
     * When set to {@code false}, SpringDoc integration will be disabled.
     */
    private boolean enabled = true;

    /**
     * A comma-separated list of package names to scan for documentable REST controllers
     * and operations. This helps in narrowing down the scanning process to specific
     * packages, thereby optimizing performance and reducing startup time.
     */
    private String packagesToScan;


    /**
     * A comma-separated list of URL base paths to scan for documentable REST
     * controllers and operations. This allows specifying certain URL patterns
     * which should be included in the documentation.
     */
    private String pathsToMatch;

}
