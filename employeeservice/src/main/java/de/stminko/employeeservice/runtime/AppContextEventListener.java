package de.stminko.employeeservice.runtime;

import java.util.Arrays;

import lombok.extern.slf4j.Slf4j;

import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.stereotype.Component;

/**
 * this class represents an event listener for the application context. It logs the
 * environment and configuration details when the application context is refreshed.
 *
 * @author Stéphan Minko
 */
@Slf4j
@Component
class AppContextEventListener {

	@EventListener
	void handleContextRefreshed(ContextRefreshedEvent event) {
		final Environment env = event.getApplicationContext().getEnvironment();
		log.info("====== Environment and configuration ======");
		log.info("Active profiles: [{}]", Arrays.toString(env.getActiveProfiles()));
		MutablePropertySources sources = ((AbstractEnvironment) env).getPropertySources();
		StringBuilder map = new StringBuilder();
		for (PropertySource<?> ps : sources) {
			if (ps instanceof EnumerablePropertySource<?> eps) {
				Arrays.stream(eps.getPropertyNames())
					.filter((String propName) -> !isSystemOrManagementProperty(propName))
					.forEach((String propName) -> {
						String propValue = env.getProperty(propName);
						map.append(propName).append(" = ").append((propValue != null) ? propValue : "").append('\n');
					});
			}
		}
		log.info("[{}]", map);
		log.info("===========================================");
	}

	private boolean isSystemOrManagementProperty(String propName) {
		return propName.matches(
				"^(?:[A-Z_]+|management\\..*|surefire\\.test.*|java\\..*|sun\\..*|.*(?:credential|password|secret).*)$");
	}

}
