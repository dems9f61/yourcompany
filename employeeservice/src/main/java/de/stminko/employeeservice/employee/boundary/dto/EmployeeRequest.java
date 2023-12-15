package de.stminko.employeeservice.employee.boundary.dto;

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
import lombok.EqualsAndHashCode;
import lombok.ToString;

import org.springframework.format.annotation.DateTimeFormat;

/**
 * represents a request object for employee-related operations. This record encapsulates
 * various fields for handling employee data processing in RESTful web services.
 * <p>
 * Fields: - emailAddress: The employee's email address, matching EMAIL_REGEX pattern. -
 * firstName: The employee's first name. Required for PUT requests. - lastName: The
 * employee's last name. Required for PUT requests. - birthday: The employee's date of
 * birth. Required for PUT requests. - departmentName: The department's name the employee
 * belongs to. Required for POST and PUT requests.
 * </p>
 *
 * @author Stéphan Minko
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@EqualsAndHashCode
@ToString
public class EmployeeRequest {

	/**
	 * Regex emailPattern to valid email address:at the beginning only a-z, A-Z, 0-9 -_.
	 * are valid except for @ after an @ a-z, A-Z, 0-9 - is valid except for further @
	 * after the last dot (.) only the defined characters [a-zA-Z] are valid with a
	 * minimum of 2 Does not support special chars any longer, besides -_.
	 */
	public static final String EMAIL_REGEX = "^[a-zA-Z0-9\\-_.]+@[a-zA-Z0-9\\-.]+\\.[a-zA-Z]{2,}$";

	@JsonView({ DataView.POST.class, DataView.PUT.class, DataView.PATCH.class, DataView.GET.class })
	@NullOrNotBlank(groups = { DataView.GET.class, DataView.POST.class })
	@NotBlank(groups = DataView.PUT.class, message = "{errors.employee.email.not-blank}")
	@Pattern(regexp = EMAIL_REGEX, groups = { DataView.PATCH.class, DataView.POST.class, DataView.PUT.class })
	@Schema(description = "Employee's email address", example = "employee@example.com")
	private final String emailAddress;

	@JsonView({ DataView.POST.class, DataView.PUT.class, DataView.PATCH.class })
	@NullOrNotBlank(groups = { DataView.PATCH.class, DataView.POST.class })
	@NotBlank(groups = DataView.PUT.class, message = "{errors.employee.first-name.not-blank}")
	@Schema(description = "Employee's first name", example = "John")
	private final String firstName;

	@JsonView({ DataView.POST.class, DataView.PUT.class, DataView.PATCH.class })
	@NullOrNotBlank(groups = { DataView.PATCH.class, DataView.POST.class })
	@NotBlank(groups = DataView.PUT.class, message = "{errors.employee.last-name.not-blank}")
	@Schema(description = "Employee's last name", example = "Doe")
	private final String lastName;

	@JsonView({ DataView.POST.class, DataView.PUT.class, DataView.PATCH.class })
	@JsonDeserialize(using = JsonDateDeserializer.class)
	@JsonSerialize(using = JsonDateSerializer.class)
	@DateTimeFormat(pattern = UsableDateFormat.Constants.DEFAULT_DATE_FORMAT)
	@NotNull(groups = { DataView.PUT.class }, message = "{errors.employee.birthday.not-null}")
	@Schema(description = "Employee's birthday", example = "1990-01-01")
	private final ZonedDateTime birthday;

	@JsonView({ DataView.POST.class, DataView.PUT.class, DataView.PATCH.class })
	@NullOrNotBlank(groups = { DataView.PATCH.class })
	@NotBlank(groups = { DataView.POST.class, DataView.PUT.class },
			message = "{errors.employee.department-name.not-blank}")
	@Schema(description = "Name of the employee's department", example = "Human Resources")
	private final String departmentName;

	@JsonCreator
	public EmployeeRequest(@JsonProperty("emailAddress") String emailAddress,
			@JsonProperty("firstName") String firstName, @JsonProperty("lastName") String lastName,
			@JsonProperty("birthday") ZonedDateTime birthday, @JsonProperty("departmentName") String departmentName) {
		this.emailAddress = emailAddress;
		this.firstName = firstName;
		this.lastName = lastName;
		this.birthday = birthday;
		this.departmentName = departmentName;
	}

	public String emailAddress() {
		return this.emailAddress;
	}

	public String firstName() {
		return this.firstName;
	}

	public String lastName() {
		return this.lastName;
	}

	public ZonedDateTime birthday() {
		return this.birthday;
	}

	public String departmentName() {
		return this.departmentName;
	}

}
