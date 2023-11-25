package de.stminko.employeeservice.runtime.rest.bondary;

/**
 * Utility class containing constants for API version paths. This class provides constants
 * to represent the various version paths for the API.
 * <p>
 * It is designed as a utility class (final class with a private constructor) and should
 * not be instantiated.
 *
 * @author Stéphan Minko
 */
public final class ApiVersions {

	/**
	 * Constant for the base path of version 1 of the API.
	 */
	public static final String V1 = "/api/v1";

	/**
	 * Private constructor to prevent instantiation of this utility class.
	 * @throws AssertionError if attempted to instantiate.
	 */
	private ApiVersions() {
		throw new AssertionError("This is not meant to be instantiated");
	}

}
