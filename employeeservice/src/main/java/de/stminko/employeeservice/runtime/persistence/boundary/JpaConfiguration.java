package de.stminko.employeeservice.runtime.persistence.boundary;

import java.time.ZonedDateTime;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

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

	/**
	 * enable Jpa Auditing via annotation, log the referenced {@link AuditorAware} and
	 * {@link DateTimeProvider} instance information of the beans used.
	 *
	 * @author Thomas Zirke
	 */
	@EnableJpaAuditing(auditorAwareRef = "auditorProvider", dateTimeProviderRef = "dateTimeProvider")
	static class SpringEnversAuditingConfiguration {

		SpringEnversAuditingConfiguration(AuditorAware<?> auditorAware, DateTimeProvider dateTimeProvider) {
			log.info(
					"SpringEnversAuditionConfiguration applied, JPA Auditing enabled, using AuditorAware bean of Type:"
							+ " [{}], using DateTimeProvider of Type: [{}]",
					auditorAware.getClass().getName(), dateTimeProvider.getClass().getName());
		}

	}

}
