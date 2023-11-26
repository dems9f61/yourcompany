package de.stminko.eventservice.employee.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * Enumeration defining usable date formats.
 *
 * <p>
 * This enum provides a centralized definition of date formats used across the
 * application. It simplifies the management of date format strings and ensures
 * consistency.
 * </p>
 *
 * <p>
 * Currently, it defines a single format {@code DEFAULT}, which is set to
 * {@code Constants.DEFAULT_DATE_FORMAT}.
 * </p>
 *
 * <p>
 * The inner class {@code Constants} contains the actual date format string. It is
 * designed to be non-instantiable, serving only as a container for constants.
 * </p>
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

		static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";

		private Constants() {
			throw new AssertionError("Don't meant to be initiated!");
		}

	}

}
