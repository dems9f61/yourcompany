package de.stminko.employeeservice.runtime.errorhandling.boundary;

/**
 * Custom exception class representing a 'Not Found' condition. This exception is
 * typically thrown when a requested resource or entity is not found in the system. It
 * extends {@link RuntimeException}, making it an unchecked exception.
 * <p>
 * Usage: This exception can be used in scenarios like database lookups, file retrievals,
 * or any situation where an expected element is not available. It is commonly used in web
 * applications, especially REST APIs, to indicate a 404 Not Found status.
 * <p>
 * Example: <pre>
 *     if (userRepository.findById(userId) == null) {
 *         throw new NotFoundException("User with id " + userId + " not found");
 *     }
 * </pre>
 *
 * @author Stéphan Minko
 */
public class NotFoundException extends RuntimeException {

	public NotFoundException(String message) {
		super(message);
	}

}
