package de.stminko.employeeservice.runtime.errorhandling.boundary;

import java.util.Map;

import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.logging.LogLevel;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;

/**
 * Spring Boot configuration properties for "de.stminko.rest.errorhandling" configuration
 * property namespace.
 *
 * @author Stéphan Minko
 */
@Data
@Configuration
@ConfigurationProperties(RestErrorHandlingProperties.CONFIGURATION_NAMESPACE)
public class RestErrorHandlingProperties {

	/**
	 * Configuration property namespace that this class is designed to handle.
	 */
	public static final String CONFIGURATION_NAMESPACE = "de.stminko.rest.errorhandling";

	/**
	 * Default Log Level for all exceptions messages logged default Exception handler.
	 * Defaults to ERROR.
	 */
	private LogLevel defaultLogLevel;

	/**
	 * Error Response default Media type that is used of the ALL MediaType is provided as
	 * an accept-header - defaults to {@link MediaType#APPLICATION_JSON}.
	 */
	private MediaType defaultErrorMediaType;

	/**
	 * A map of FQDN Java Exception Classes to their desired HTTP Response Status code for
	 * dedicated Exception types
	 */
	private Map<String, Integer> runtimeHttpErrorCodes;

	/**
	 * A Map of HTTP Response Status Codes to LogLevel. Defines which Http Response Status
	 * code is logged on which level in the application.
	 */
	private Map<Integer, LogLevel> httpStatusLogLevel;

}
