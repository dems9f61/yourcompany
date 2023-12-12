package de.stminko.employeeservice.runtime.persistence.boundary;

import java.time.ZonedDateTime;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.domain.AuditorAware;

/**
 * configuration class for JPA-related settings.
 *
 * <p>
 * This class, marked with {@link Configuration}, is responsible for setting up
 * JPA-related beans in the Spring application context. It includes beans for auditor
 * awareness and date/time provisioning, which are essential for auditing entities in JPA.
 * </p>
 *
 * @author Stéphan Minko
 */
@Configuration
@Slf4j
public class JpaConfiguration {

	/**
	 * Provides an implementation of {@link AuditorAware} for auditing purposes.
	 *
	 * <p>
	 * This bean is used by JPA auditing to capture the current auditor, typically the
	 * current user. In this configuration, it is set to a fixed value "System", suitable
	 * for scenarios where user context is not available or for system-based operations.
	 * </p>
	 * @return an instance of {@link AuditorAware} with a fixed auditor value.
	 */
	@Bean
	public AuditorAware<String> auditorProvider() {
		return () -> Optional.of("System");
	}

	/**
	 * Provides a custom {@link DateTimeProvider}.
	 *
	 * <p>
	 * This bean is used to supply the current date and time for auditing purposes,
	 * particularly for timestamping creation and modification times of entities.
	 * </p>
	 * @return an instance of {@link DateTimeProvider} that returns the current date and
	 * time.
	 */
	@Bean
	public DateTimeProvider dateTimeProvider() {
		return () -> Optional.of(ZonedDateTime.now());
	}

}
