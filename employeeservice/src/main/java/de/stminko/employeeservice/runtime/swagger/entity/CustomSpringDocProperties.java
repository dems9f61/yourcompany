package de.stminko.employeeservice.runtime.swagger.entity;

import de.stminko.employeeservice.runtime.swagger.boundary.SpringDocStarterAutoConfiguration;
import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties class for customizing SpringDoc integration.
 *
 * <p>
 * This class is used to bind and manage configuration properties for SpringDoc in a
 * Spring Boot application. It is annotated with {@code @ConfigurationProperties} to
 * indicate that its fields are bound to properties in an external configuration file.
 * </p>
 *
 * <p>
 * Properties are grouped under the namespace defined in
 * {@code SpringDocStarterAutoConfiguration.CONFIGURATION_NAMESPACE}. This allows for
 * structured and organized configuration of SpringDoc related settings.
 * </p>
 *
 * <p>
 * The class includes properties such as:
 * </p>
 * <ul>
 * <li>{@code enabled}: A boolean flag to enable or disable SpringDoc integration.
 * Defaults to {@code true}.</li>
 * <li>{@code packagesToScan}: A comma-separated list of packages to scan for documentable
 * REST controllers and operations.</li>
 * <li>{@code pathsToMatch}: A comma-separated list of URL base paths to scan for
 * documentable REST controllers and operations.</li>
 * </ul>
 *
 * <p>
 * Use this class to configure SpringDoc behavior, such as specifying which controllers
 * and paths should be included in the API documentation.
 * </p>
 *
 * @author Stéphan Minko
 */
@Data
@Configuration
@ConfigurationProperties(SpringDocStarterAutoConfiguration.CONFIGURATION_NAMESPACE)
public class CustomSpringDocProperties {

	/**
	 * Enable SpringDoc integration - defaults to <code>true</code>.
	 */
	private boolean enabled = true;

	/**
	 * A comma separated String of Packages to scan for documentable Rest controllers and
	 * operations.
	 */
	private String packagesToScan;

	/**
	 * A comma separated String of URL Base Paths to scan for documentable Rest
	 * controllers and operations.
	 */
	private String pathsToMatch;

}
