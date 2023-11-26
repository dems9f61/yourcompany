package de.stminko.employeeservice.employee.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * Enum defining usable date formats within the application. This enum is designed to
 * centralize the management of date formats, allowing easy maintenance and consistency.
 * <p>
 * Usage Example: <pre>
 *     String dateFormat = UsableDateFormat.DEFAULT.getDateFormat();
 * </pre>
 * <p>
 * Constants Class: - Contains the actual string representations of the date formats used.
 * - DEFAULT_DATE_FORMAT: Represents the default date format used in the application
 * ("yyyy-MM-dd").
 * <p>
 * Note: The Constants class is not meant to be instantiated and throws an AssertionError
 * if an attempt is made.
 *
 * @author Stéphan Minko
 */
@Getter
@ToString
@RequiredArgsConstructor
public enum UsableDateFormat {

	/**
	 * the default date format used across the Event Service application. This format is
	 * set to 'yyyy-MM-dd', representing a common pattern for representing dates. It is
	 * used in scenarios where a standardized, simple date format is required, such as in
	 * logging, data storage, or user interfaces.
	 */
	DEFAULT(Constants.DEFAULT_DATE_FORMAT);

	private final String dateFormat;

	/**
	 * inner class to hold constants related to the {@link UsableDateFormat} enum. This
	 * includes the default date format pattern.
	 */
	public static final class Constants {

		/**
		 * The default date format used across the Event Service application. This format
		 * is set to 'yyyy-MM-dd', representing a common pattern for representing dates.
		 * It is used in scenarios where a standardized, simple date format is required,
		 * such as in logging, data storage, or user interfaces.
		 */
		static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";

		private Constants() {
			throw new AssertionError("Don't meant to be initiated!");
		}

	}

}
