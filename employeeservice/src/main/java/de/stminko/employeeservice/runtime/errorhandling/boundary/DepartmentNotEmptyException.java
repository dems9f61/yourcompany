package de.stminko.employeeservice.runtime.errorhandling.boundary;

/**
 * exception thrown when an attempt is made to delete a department that still contains
 * employees. This ensures the integrity of business logic by preventing the deletion of
 * departments while they still have employees assigned to them.
 * <p>
 * This exception should typically be mapped to an appropriate HTTP status code in a web
 * application context, such as HTTP 409 Conflict, to indicate that the operation cannot
 * be completed due to a conflict in the application state.
 *
 * @author Stéphan Minko
 */
public class DepartmentNotEmptyException extends RuntimeException {

	public DepartmentNotEmptyException(String message) {
		super(message);
	}

}
