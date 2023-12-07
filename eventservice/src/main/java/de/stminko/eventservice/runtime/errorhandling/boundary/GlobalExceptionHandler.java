package de.stminko.eventservice.runtime.errorhandling.boundary;

import java.time.ZonedDateTime;

import de.stminko.eventservice.runtime.errorhandling.entity.ErrorInfo;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Global exception handler for the application.
 *
 * <p>
 * This class provides a centralized exception handling mechanism across the entire
 * application. It intercepts exceptions thrown by any controller and processes them
 * accordingly.
 * </p>
 *
 * @author Stéphan Minko
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

	/**
	 * Handles generic exceptions.
	 *
	 * <p>
	 * This method is invoked when an unhandled exception occurs in the application. It
	 * logs the exception and returns a standardized error response.
	 * </p>
	 * @param httpServletRequest the HttpServletRequest in which the exception occurred
	 * @param exception the caught exception
	 * @return a ResponseEntity containing an {@link ErrorInfo} object detailing the error
	 */
	@ExceptionHandler({ Exception.class })
	protected ResponseEntity<ErrorInfo> handleException(HttpServletRequest httpServletRequest, Exception exception) {
		String localizedMessage = exception.getLocalizedMessage();
		log.error("Unhandled Exception occurred. Error: {}", localizedMessage, exception);
		ErrorInfo errorInfo = ErrorInfo.builder()
			.url(httpServletRequest.getRequestURI())
			.errorDateTime(ZonedDateTime.now())
			.errorMessage(exception.getMessage())
			.httpMethod(HttpMethod.valueOf(httpServletRequest.getMethod()).name())
			.httpStatus(HttpStatus.INTERNAL_SERVER_ERROR)
			.httpStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
			.build();
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorInfo);
	}

}
