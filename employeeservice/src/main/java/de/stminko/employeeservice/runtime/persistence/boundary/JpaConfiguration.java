package de.stminko.employeeservice.runtime.persistence.boundary;

import java.time.ZonedDateTime;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.domain.AuditorAware;

/**
 * configure the CustomAuditorAware bean.
 *
 * @author Stéphan Minko
 */
@Configuration
@Slf4j
public class JpaConfiguration {

	@Bean
	public AuditorAware<String> auditorProvider() {
		return () -> Optional.of("System");
	}

	@Bean
	public DateTimeProvider dateTimeProvider() {
		return () -> Optional.of(ZonedDateTime.now());
	}

}
