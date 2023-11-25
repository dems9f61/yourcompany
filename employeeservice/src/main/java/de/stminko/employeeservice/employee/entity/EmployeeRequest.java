package de.stminko.employeeservice.employee.entity;

import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import de.stminko.employeeservice.runtime.rest.bondary.DataView;
import de.stminko.employeeservice.runtime.validation.constraints.boundary.NullOrNotBlank;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import org.springframework.format.annotation.DateTimeFormat;

/**
 * Represents a request object for employee-related operations. This record encapsulates
 * various fields like email address, first name, last name, birthday, and department name
 * for handling employee data processing.
 * <p>
 * It is annotated with {@code JsonView} and validation constraints to facilitate
 * serialization and validation in different contexts like POST, PUT, PATCH, and GET
 * requests. This record is particularly designed to be used with Spring Web MVC and
 * Jackson for RESTful web services.
 * <p>
 * Usage of {@code JsonIgnoreProperties} ensures that any unknown properties in the JSON
 * request do not cause deserialization issues.
 * <p>
 * Fields: - emailAddress: The employee's email address. Must match the defined
 * EMAIL_REGEX pattern. - firstName: The employee's first name. Required for PUT requests.
 * - lastName: The employee's last name. Required for PUT requests. - birthday: The
 * employee's date of birth in ZonedDateTime format. Required for PUT requests. -
 * departmentName: The name of the department the employee belongs to. Required for POST
 * and PUT requests.
 * <p>
 * Note: The EMAIL_REGEX pattern defines the acceptable format for the email address
 * field: - this regex ensures that the provided email has a username part with allowed
 * characters, followed by an @ symbol, then a domain part with allowed characters, a
 * period, and a top-level domain of at least two letters.
 *
 * @author Stéphan Minko
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record EmployeeRequest(@JsonView( {
		DataView.POST.class, DataView.PUT.class, DataView.PATCH.class, DataView.GET.class }) @NullOrNotBlank(
				groups = { DataView.GET.class, DataView.POST.class }) @NotBlank(groups = DataView.PUT.class,
						message = "{errors.employee.email.not-blank}") @Pattern(regexp = EMAIL_REGEX,
								groups = { DataView.PATCH.class, DataView.POST.class, DataView.PUT.class }) @Schema(
										description = "Employee's email address",
										example = "employee@example.com") String emailAddress,
		@JsonView({ DataView.POST.class, DataView.PUT.class, DataView.PATCH.class }) @NullOrNotBlank(
				groups = { DataView.PATCH.class, DataView.POST.class }) @NotBlank(groups = DataView.PUT.class,
						message = "{errors.employee.first-name.not-blank}") @Schema(
								description = "Employee's first name", example = "John") String firstName,
		@JsonView({ DataView.POST.class, DataView.PUT.class, DataView.PATCH.class }) @NullOrNotBlank(
				groups = { DataView.PATCH.class, DataView.POST.class }) @NotBlank(groups = DataView.PUT.class,
						message = "{errors.employee.last-name.not-blank}") @Schema(description = "Employee's last name",
								example = "Doe") String lastName,
		@JsonView({ DataView.POST.class, DataView.PUT.class, DataView.PATCH.class }) @JsonDeserialize(
				using = JsonDateDeSerializer.class) @JsonSerialize(using = JsonDateSerializer.class) @DateTimeFormat(
						pattern = UsableDateFormat.Constants.DEFAULT_DATE_FORMAT) @NotNull(
								groups = { DataView.PUT.class },
								message = "{errors.employee.birthday.not-null}") @Schema(
										description = "Employee's birthday",
										example = "1990-01-01") ZonedDateTime birthday,
		@JsonView({ DataView.POST.class, DataView.PUT.class, DataView.PATCH.class }) @NullOrNotBlank(
				groups = { DataView.PATCH.class }) @NotBlank(groups = { DataView.POST.class, DataView.PUT.class },
						message = "{errors.employee.department-name.not-blank}") @Schema(
								description = "Name of the employee's department",
								example = "Human Resources") String departmentName){

	// Regex emailPattern to valid email address:
	// at the beginning only a-z, A-Z, 0-9 -_. are valid except for @
	// after an @ a-z, A-Z, 0-9 - is valid except for further @
	// after the last dot (.) only the defined characters [a-zA-Z] are valid with a
	// minimum of 2
	// Does not support special chars any longer, besides -_.
	public static final String EMAIL_REGEX = "^[a-zA-Z0-9\\-_.]+@[a-zA-Z0-9\\-.]+\\.[a-zA-Z]{2,}$";

	@JsonCreator
	public EmployeeRequest(@JsonProperty(value = "emailAddress") String emailAddress,
			@JsonProperty(value = "firstName") String firstName, @JsonProperty(value = "lastName") String lastName,
			@JsonProperty(value = "birthday") ZonedDateTime birthday,
			@JsonProperty(value = "departmentName") String departmentName) {
		this.emailAddress = emailAddress;
		this.firstName = firstName;
		this.lastName = lastName;
		this.birthday = birthday;
		this.departmentName = departmentName;
	}

}
