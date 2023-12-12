package de.stminko.employeeservice.runtime.rest.bondary;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * A logging advice aspect for Rest Controllers. It wraps all EntityControllers for
 * logging purposes.
 *
 * @author Stéphan Minko
 */
@Slf4j
@Aspect
@ConditionalOnProperty(value = RestControllerLoggingAdvice.ENABLED_CONFIG_PROPERTY, havingValue = "true",
		matchIfMissing = true)
@Component
public class RestControllerLoggingAdvice {

	static final String ENABLED_CONFIG_PROPERTY = "rest.controller.loggingInterceptor.enabled";

	RestControllerLoggingAdvice() {
		log.info(
				"Created @RestContoller method logging interceptor: [{}]. Set application.yml property: [{}] to 'false' to deactivate this autoconfiguration",
				this.getClass().getName(), ENABLED_CONFIG_PROPERTY);
	}

	@Around("execution(public * de.stminko.employeeservice.*.boundary.*Controller.*(..))")
	Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
		long start = System.currentTimeMillis();
		log.debug("Entering Method: {}.{} ( {} )", joinPoint.getTarget().getClass().getName(),
				joinPoint.getSignature().getName(), joinPoint.getArgs());
		Object retVal = joinPoint.proceed();
		log.debug("Leaving Method {}.{} ( {} ). Return Value is: [{}]. Method execution took: [{}] ms to complete",
				joinPoint.getTarget().getClass().getName(), joinPoint.getSignature().getName(), joinPoint.getArgs(),
				retVal, System.currentTimeMillis() - start);
		return retVal;
	}

}
