package de.stminko.employeeservice.employee.entity;

import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import de.stminko.employeeservice.runtime.rest.bondary.DataView;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.NonNull;

import org.springframework.format.annotation.DateTimeFormat;

/**
 * Represents a response object for employee-related operations. This record is used to
 * encapsulate the details of an employee that are sent as a response in RESTful web
 * services. It includes fields like ID, email address, first name, last name, birthday,
 * and department name.
 * <p>
 * Fields: - id: The unique identifier of the employee. It's non-null. - emailAddress: The
 * email address of the employee. - firstName: The first name of the employee. - lastName:
 * The last name of the employee. - birthday: The birthday of the employee, formatted
 * according to UsableDateFormat.Constants.DEFAULT_DATE_FORMAT. - departmentName: The name
 * of the department the employee belongs to.
 *
 * @param id The unique identifier of the employee. It's non-null.
 * @param emailAddress The email address of the employee.
 * @param firstName The first name of the employee.
 * @param lastName The last name of the employee.
 * @param birthday The birthday of the employee, formatted according to
 * UsableDateFormat.Constants.DEFAULT_DATE_FORMAT.
 * @param departmentName The name of the department the employee belongs to.
 * @author Stéphan Minko
 */
@JsonView(DataView.GET.class)
public record EmployeeResponse(
		@NonNull @Schema(description = "The unique identifier of the employee", example = "12345") String id,
		@Schema(description = "Employee's email address", example = "employee@example.com") String emailAddress,
		@Schema(description = "Employee's first name", example = "John") String firstName,

		@Schema(description = "Employee's last name", example = "Doe") String lastName,
		@JsonDeserialize(using = JsonDateDeserializer.class) @JsonSerialize(
				using = JsonDateSerializer.class) @DateTimeFormat(
						pattern = UsableDateFormat.Constants.DEFAULT_DATE_FORMAT) @Schema(
								description = "Employee's birthday", example = "1990-01-01") ZonedDateTime birthday,
		@JsonView(DataView.GET.class) @Schema(description = "Name of the employee's department",
				example = "Human Resources") String departmentName) {

	@JsonCreator
	public EmployeeResponse(@JsonProperty("id") String id, @JsonProperty("emailAddress") String emailAddress,
			@JsonProperty("firstName") String firstName, @JsonProperty("lastName") String lastName,
			@JsonProperty("birthday") ZonedDateTime birthday,
			@JsonProperty(value = "departmentName", required = true) String departmentName) {
		this.id = id;
		this.emailAddress = emailAddress;
		this.firstName = firstName;
		this.lastName = lastName;
		this.birthday = birthday;
		this.departmentName = departmentName;
	}

}
