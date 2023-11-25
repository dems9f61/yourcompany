package de.stminko.eventservice.employee.entity;

import java.io.Serializable;
import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.ToString;

/**
 * Represents an employee entity.
 *
 * <p>
 * This class models the employee data, encapsulating various attributes of an employee
 * such as ID, email address, full name, birthday, and department. It is designed to be
 * serializable for compatibility with Java's serialization mechanism.
 * </p>
 *
 * <p>
 * The {@link FullName} static inner class represents the full name of the employee,
 * containing first and last name fields.
 * </p>
 *
 * <p>
 * It is marked with {@code @JsonIgnoreProperties(ignoreUnknown = true)} to ignore any
 * unknown JSON properties during deserialization, enhancing JSON processing flexibility.
 * </p>
 *
 * @author Stéphan Minko
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Employee implements Serializable {

	private String id;

	private String emailAddress;

	private FullName fullName = new FullName();

	private ZonedDateTime birthday;

	private Department department;

	/**
	 * Represents the full name of an employee.
	 */
	@Data
	@ToString
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class FullName implements Serializable {

		private String firstName;

		private String lastName;

	}

}
