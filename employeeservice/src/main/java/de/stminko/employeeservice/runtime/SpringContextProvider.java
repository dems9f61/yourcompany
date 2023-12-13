package de.stminko.employeeservice.runtime;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * spring component that provides access to the {@link ApplicationContext}. This class
 * stores a static reference to the {@link ApplicationContext} once it is set.
 *
 * <p>
 * This is useful for accessing Spring beans from non-Spring-managed classes or in
 * situations where dependency injection is not feasible.
 *
 * <p>
 * Note: Use of this class should be limited as it goes against the standard dependency
 * injection model of Spring and can make testing more difficult. It should only be used
 * when there is no alternative.
 *
 * @author Stéphan Minko
 * @see ApplicationContext
 * @see ApplicationContextAware
 */
@Component
@Slf4j
public class SpringContextProvider implements ApplicationContextAware {

	private static ApplicationContext context;

	/**
	 * Sets the {@link ApplicationContext} instance. This method is automatically called
	 * by the Spring framework during the initialization phase.
	 * @param applicationContext the Spring {@link ApplicationContext} to be set.
	 * @throws BeansException if there is an issue setting the application context.
	 */
	@Override
	public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
		context = applicationContext;
	}

	/**
	 * Retrieves the {@link ApplicationContext} instance.
	 *
	 * <p>
	 * This static method can be used to access the application context from
	 * non-Spring-managed classes.
	 * @return the {@link ApplicationContext} instance set by the Spring framework.
	 */
	public static ApplicationContext getApplicationContext() {
		return context;
	}

}
