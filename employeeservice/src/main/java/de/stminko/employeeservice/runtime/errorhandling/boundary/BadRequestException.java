package de.stminko.employeeservice.runtime.errorhandling.boundary;

/**
 * Exception class representing a bad request error. This class is used to signal when a
 * request cannot be processed due to issues with the request itself, such as invalid
 * input or malformed data. It extends {@link RuntimeException}, allowing it to be used as
 * an unchecked exception.
 *
 * <p>
 * Usage: This exception can be thrown in situations where the request parameters or the
 * request body does not meet the expected format or criteria. It is typically used in web
 * applications or services where input validation fails.
 * <p>
 * Example: <pre>
 *     if (userInput == null) {
 *         throw new BadRequestException("User input cannot be null");
 *     }
 * </pre>
 *
 * @author Stéphan Minko
 */
public class BadRequestException extends RuntimeException {

	public BadRequestException(String message) {
		super(message);
	}

}
