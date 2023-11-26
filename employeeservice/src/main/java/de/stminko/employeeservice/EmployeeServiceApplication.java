package de.stminko.employeeservice;

import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.envers.repository.support.EnversRevisionRepositoryFactoryBean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * main application class for the Employee Service. This class is responsible for
 * bootstrapping and launching the application, initializing the Spring application
 * context, and setting up necessary configurations.
 *
 * <p>
 * The class is also annotated with {@code @EnableJpaAuditing} and
 * {@code @EnableJpaRepositories}, which enable JPA auditing and configure the repository
 * layer, respectively. The {@code @EnableJpaRepositories} annotation is specifically
 * configured to use {@code EnversRevisionRepositoryFactoryBean} for integration with
 * Hibernate Envers, facilitating auditing of the JPA entities.
 * </p>
 *
 * <p>
 * The {@code main} method sets the default time zone to 'Europe/Berlin' and launches the
 * Spring application. This setup ensures that all date/time operations within the
 * application are aligned with the specified time zone.
 * </p>
 *
 * @author Stéphan Minko
 */
@EnableJpaAuditing
@EnableJpaRepositories(repositoryFactoryBeanClass = EnversRevisionRepositoryFactoryBean.class)
@SpringBootApplication
public class EmployeeServiceApplication {

	public static void main(String[] args) {
		TimeZone.setDefault(TimeZone.getTimeZone("Europe/Berlin"));
		SpringApplication.run(EmployeeServiceApplication.class, args);
	}

}
