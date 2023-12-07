package de.stminko.employeeservice.runtime.errorhandling.boundary;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import de.stminko.employeeservice.runtime.errorhandling.entity.ErrorInfo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.logging.LogLevel;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Global exception handler for the Spring application. This class is annotated
 * with @ControllerAdvice to provide centralized exception handling across
 * all @RequestMapping methods. It captures and handles various types of exceptions,
 * converting them into a structured format for client responses.
 * <p>
 * Features: - Logs the details of handled exceptions, including HTTP request information.
 * - Resolves appropriate HTTP status codes based on the exception type. - Serializes
 * error information into a consistent response format. - Handles constraint violations by
 * converting them into {@link ConstraintViolationInfo} objects.
 * <p>
 * Methods: - handleException: Captures and processes generic exceptions, returning a
 * structured error response. - createErrorResponseBody: Constructs the error response
 * body based on the exception and request details. - resolveHttpResponseStatus:
 * Determines the appropriate HTTP status code based on the exception type. -
 * getResponseContentType: Resolves the response content type based on the request's
 * Accept header. - getConstraintViolationInfos: Extracts constraint violation details
 * from exceptions.
 * <p>
 * Usage: This class is automatically used by Spring to handle exceptions thrown by
 * controllers. It simplifies error handling by providing a consistent structure for error
 * responses.
 *
 * @author Stéphan Minko
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

	private static final String REQUESTED_CONTENT_TYPE_NOT_SUPPORTED_HEADER = "X-Content-Type-Unsupported";

	private static final String REQUESTED_CONTENT_TYPE_NOT_SUPPORTED_ERROR_MESSAGE = "None of the requested Content Types [%s] is supported. Falling back to Content-Type: [%s]";

	private final RestErrorHandlingProperties configuration;

	private final Set<MediaType> supportedResponseMediaTypes = new HashSet<>();

	/**
	 * Default Constructor - must be overridden in extending classes.
	 * @param configuration the {@link RestErrorHandlingProperties} configuration.
	 * @param messageConverters a {@link List} of registered {@link HttpMessageConverter}
	 * beans from the spring context. Used to determine the supported Response Content
	 * Mime Types.
	 */
	@Autowired
	protected GlobalExceptionHandler(RestErrorHandlingProperties configuration,
			List<HttpMessageConverter<?>> messageConverters) {
		this.configuration = configuration;
		messageConverters.forEach((HttpMessageConverter<?> converter) -> this.supportedResponseMediaTypes
			.addAll(converter.getSupportedMediaTypes()));
		log.info("ExceptionHandler/ControllerAdvice: [{}], Response Body Type: [{}]", getClass().getName(),
				ErrorInfo.class.getName());
		log.info("Supported Mime-Types for ExceptionHandler error serialization: [{}]",
				this.supportedResponseMediaTypes);
	}

	/**
	 * Generic Exception handler method. Is called from any other Exception handler either
	 * directly or indirectly.
	 * @param caught the Exception to handle
	 * @param request the {@link HttpServletRequest} context
	 * @return the {@link ResponseEntity} containing the R generic as its response body
	 */
	@ExceptionHandler(Exception.class)
	public final ResponseEntity<ErrorInfo> handleException(Throwable caught, HttpServletRequest request) {
		Throwable rootCause = Optional.ofNullable(NestedExceptionUtils.getRootCause(caught)).orElse(caught);
		HttpStatus responseStatus = resolveHttpResponseStatus(caught, rootCause);
		var responseHeaders = new HttpHeaders();

		// If the most specific cause is a ConstraintViolation delegated to its
		// dedicated handler
		Set<ConstraintViolationInfo> constraintViolations = new HashSet<>();
		if (ConstraintViolationException.class.isAssignableFrom(rootCause.getClass())) {
			constraintViolations = getConstraintViolationInfos((ConstraintViolationException) rootCause);
		}
		if (MethodArgumentNotValidException.class.isAssignableFrom(rootCause.getClass())) {
			constraintViolations = getConstraintViolationInfos((MethodArgumentNotValidException) rootCause);
		}
		if (BindException.class.isAssignableFrom(rootCause.getClass())) {
			constraintViolations = getConstraintViolationInfos((BindException) rootCause);
		}
		logHandledException(responseStatus, caught, request);
		return ResponseEntity.status(responseStatus)
			.contentType(getResponseContentType(request, responseHeaders))
			.body(createErrorResponseBody(responseStatus, rootCause, request, constraintViolations));
	}

	/**
	 * Create the response body Entity.
	 * @param status the HTTP status for the response
	 * @param e the exception that caused an error response to be created
	 * @param request the HTTP request that is being responded to
	 * @param constraintViolations a {@link Set} of {@link ConstraintViolation}s, can be
	 * empty
	 * @return the R typed error response body object
	 */
	public ErrorInfo createErrorResponseBody(HttpStatus status, Throwable e, HttpServletRequest request,
			Set<ConstraintViolationInfo> constraintViolations) {
		return ErrorInfo.builder()
			.url(request.getRequestURI())
			.urlQueryString(request.getQueryString())
			.errorDateTime(ZonedDateTime.now())
			.errorMessage(e.getMessage())
			.httpMethod(request.getMethod())
			.httpStatus(status)
			.httpStatusCode(status.value())
			.constraintViolations(constraintViolations)
			.build();
	}

	/**
	 * Resolve a HTTP Response Status for given {@link Throwable} cause and its root cause
	 * {@link Throwable}.
	 * @param cause the {@link Throwable} that was handled by the ExceptionHandler
	 * @param rootCause the {@link Throwable} causes root cause {@link Throwable}
	 * @return the most specific HTTP Response Status retrieved via Lookup Hierarchy
	 */
	private HttpStatus resolveHttpResponseStatus(Throwable cause, Throwable rootCause) {
		HttpStatus rootCauseStatus = resolveHttpResponseStatusForException(rootCause);
		HttpStatus causeMapping = resolveHttpResponseStatusForException(cause);
		HttpStatus resultHttpStatus = Optional.ofNullable(Optional.ofNullable(rootCauseStatus).orElse(causeMapping))
			.orElse(HttpStatus.INTERNAL_SERVER_ERROR);
		log.debug(
				"The HTTP Response Code: [{}] / [{}] was resolved for Exception of Type: [{}] and its Root Cause Exception of Type: [{}]",
				resultHttpStatus.value(), resultHttpStatus.name(), cause.getClass().getName(),
				rootCause.getClass().getName());
		return resultHttpStatus;
	}

	/**
	 * Resolves an HTTP Status for a given Exception. Checks the Exception super class
	 * hierarchy for application.xml FQDN Exception Name 2 HTTP Status mappings
	 * / @ResponseCode annotations. Mappings always take precedence over annotations.
	 * @param cause the Exception to retrieve an HTTP Status for by looking for specific
	 * mappings and response status code annotations
	 * @return the resolved HTTP Response Status found for given Exception and its
	 * inheritance hierarchy.
	 */
	private HttpStatus resolveHttpResponseStatusForException(Throwable cause) {
		HttpStatus httpStatus = null;
		Class<?> currentExceptionClass = cause.getClass();
		do {
			// Lookup from application.yml mappings if one exists for the current
			// Exception Class
			if (this.configuration.getRuntimeHttpErrorCodes().containsKey(currentExceptionClass.getName())) {
				httpStatus = HttpStatus
					.valueOf(this.configuration.getRuntimeHttpErrorCodes().get(currentExceptionClass.getName()));
			}
			// Lookup Response Code if no Mapping was found
			ResponseStatus causeStatus = currentExceptionClass.getAnnotation(ResponseStatus.class);
			if ((httpStatus == null) && (causeStatus != null)) {
				httpStatus = causeStatus.value();
			}

			// Move up in the inheritance hierarchy of the Exception if here is any
			currentExceptionClass = currentExceptionClass.getSuperclass();
		}
		// Do for as long as there is an Exception Super class and no HttpStatus has been
		// resolved yet
		while ((currentExceptionClass != null) && (httpStatus == null));
		return httpStatus;
	}

	/**
	 * Log the handled Exception.
	 * @param status the HttpStatus resolved for the exception
	 * @param caught the exception handled
	 * @param request the {@link HttpServletRequest} context
	 */
	private void logHandledException(HttpStatus status, Throwable caught, HttpServletRequest request) {
		String queryString = Optional.ofNullable(request.getQueryString()).map((String q) -> "?" + q).orElse("");
		String message = "HTTP Error - URL: [%s%s], Method: [%s] - Error: [%s]".formatted(request.getRequestURI(),
				queryString, request.getMethod(), caught.getMessage());

		LogLevel logLevel = this.configuration.getHttpStatusLogLevel()
			.getOrDefault(status.value(), this.configuration.getDefaultLogLevel());

		switch (logLevel) {
			case TRACE -> log.trace(message, caught);
			case DEBUG -> log.debug(message, caught);
			case INFO -> log.info(message, caught);
			case WARN -> log.warn(message, caught);
			case ERROR -> log.error(message, caught);
			case OFF -> {
			} // No operation
				// No default needed as all cases are covered
		}
	}

	/**
	 * Retrieve Accept Header from request if there is one. Otherwise fallback to
	 * MediaType.APPLICATION_JSON
	 * @param request the inbound request that was responded to erroneously
	 * @param responseHeaders the Response Headers to have a header added to if the
	 * serialisation was not possibly for any requested Content Mime Types (Accept Header)
	 * @return the {@link MediaType} requested or {@link MediaType#APPLICATION_JSON} if
	 * there was no mediaType requested explicitly
	 */
	private MediaType getResponseContentType(HttpServletRequest request, HttpHeaders responseHeaders) {
		MediaType retVal = null;
		List<String> acceptHeaders = Collections.list(request.getHeaders(HttpHeaders.ACCEPT));
		try {
			// Filter out a potential "*/*" (ALL) Accept header value from the requested
			// Accept headers values. Will fall back to the default response content type
			// if it is the only Accept header value provided.
			List<MediaType> requestMediaTypes = MediaType.parseMediaTypes(acceptHeaders)
				.stream()
				.filter((MediaType mediaType) -> !mediaType.equals(MediaType.ALL))
				.toList();
			for (MediaType currentRequestedMediaType : requestMediaTypes) {
				// Find the first supported non wildcard type / subtype MediaType that is
				// supported by the runtime environment and return it.
				retVal = this.supportedResponseMediaTypes.stream()
					.filter((MediaType m) -> !m.isWildcardType() && !m.isWildcardSubtype()
							&& m.isCompatibleWith(currentRequestedMediaType))
					.findFirst()
					.orElse(retVal);
				// Break the loop as soon as a result was found for the first Accept
				// header that can be served
				if (retVal != null) {
					log.debug("Found supported Accept header: [{}], resolved to Response Content-Type: [{}]",
							currentRequestedMediaType, retVal);
					break;
				}
			}
		}
		catch (InvalidMediaTypeException caught) {
			log.debug("Invalid Media Types: [{}] have been provided for REST URL: [{}]. Error was: [{}]", acceptHeaders,
					request.getRequestURI(), caught.getMessage(), caught);
			responseHeaders.add(REQUESTED_CONTENT_TYPE_NOT_SUPPORTED_HEADER,
					String.format(REQUESTED_CONTENT_TYPE_NOT_SUPPORTED_ERROR_MESSAGE,
							request.getHeaders(HttpHeaders.ACCEPT), this.configuration.getDefaultErrorMediaType()));
		}
		// Return resolved Content Type or Default Content type
		retVal = Optional.ofNullable(retVal).orElse(this.configuration.getDefaultErrorMediaType());
		log.debug("REST Error Response Content-Type resolved to: [{}]", retVal);
		return retVal;
	}

	/**
	 * Extract a {@link Set} of a {@link ConstraintViolationInfo} from given
	 * {@link ConstraintViolationException}.
	 * @param constraintViolationException the {@link ConstraintViolationException} source
	 * to extract {@link ConstraintViolationInfo}s from
	 * @return a {@link Set} of {@link ConstraintViolationInfo} one for each
	 * {@link ConstraintViolation} on the {@link ConstraintViolationException}
	 */
	private Set<ConstraintViolationInfo> getConstraintViolationInfos(
			ConstraintViolationException constraintViolationException) {
		return constraintViolationException.getConstraintViolations()
			.stream()
			.map((ConstraintViolation<?> constraintViolation) -> ConstraintViolationInfo.builder()
				.invalidValue(Optional.ofNullable(constraintViolation.getInvalidValue()).orElse("null").toString())
				.message(constraintViolation.getMessage())
				.propertyPath(constraintViolation.getPropertyPath().toString())
				.build())
			.collect(Collectors.toSet());
	}

	/**
	 * Extract a {@link Set} of a {@link ConstraintViolationInfo} from given
	 * {@link BindingResult}.
	 * @param bindingResult the {@link BindingResult} source to extract
	 * {@link ConstraintViolationInfo}s from
	 * @return a {@link Set} of {@link ConstraintViolationInfo} one for each
	 * {@link ConstraintViolation} on the {@link ConstraintViolationException}
	 */
	private Set<ConstraintViolationInfo> getConstraintViolationInfos(BindingResult bindingResult) {
		Set<ConstraintViolationInfo> violationInfos;
		// Field errors
		violationInfos = bindingResult.getFieldErrors()
			.stream()
			.map((FieldError constraintViolation) -> ConstraintViolationInfo.builder()
				.invalidValue(Optional.ofNullable(constraintViolation.getRejectedValue()).orElse("null").toString())
				.message(constraintViolation.getDefaultMessage())
				.propertyPath(constraintViolation.getField())
				.build())
			.collect(Collectors.toSet());
		// Global Errors
		violationInfos.addAll(bindingResult.getGlobalErrors()
			.stream()
			.map(((ObjectError objectError) -> ConstraintViolationInfo.builder()
				.isGlobalError(true)
				.propertyPath(objectError.getObjectName())
				.message(objectError.getDefaultMessage())
				.build()))
			.collect(Collectors.toSet()));
		return violationInfos;
	}

}
