package de.stminko.employeeservice.department.boundary.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import de.stminko.employeeservice.runtime.rest.bondary.DataView;
import de.stminko.employeeservice.runtime.validation.constraints.boundary.NullOrNotBlank;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * represents a request model for department-related operations in the Employee Service
 * application.
 *
 * <p>
 * This class is used for encapsulating department data received from API requests,
 * especially in operations such as creating or updating department details. The class
 * structure and validation annotations ensure that incoming data adheres to the expected
 * format and business rules.
 * </p>
 *
 * <p>
 * Key features of this class include:
 * </p>
 * <ul>
 * <li><b>Department Name Validation:</b> The department name is validated based on the
 * type of request. It must not be blank for POST and PUT requests, while it can be null
 * or not blank for PATCH requests, allowing for partial updates.</li>
 * <li><b>JSON Views:</b> Integration with {@link DataView} for handling serialization and
 * deserialization differently based on the context of the HTTP request method.</li>
 * <li><b>API Documentation:</b> Enhanced with Swagger annotations to provide clear API
 * documentation for client consumers, including descriptions and example values.</li>
 * </ul>
 *
 * <p>
 * Use this class as a DTO (Data Transfer Object) for department-related API endpoints to
 * maintain clean and understandable code structure.
 * </p>
 *
 * @author Stéphan Minko
 * @see DataView
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@EqualsAndHashCode
@Builder
@ToString
public class DepartmentRequest {

	@JsonView({ DataView.POST.class, DataView.PUT.class, DataView.PATCH.class })
	@NullOrNotBlank(groups = { DataView.PATCH.class })
	@NotBlank(message = "{errors.department.name.not-blank}", groups = { DataView.POST.class, DataView.PUT.class })
	@Schema(description = "Name of the department", example = "Human Resources",
			requiredMode = Schema.RequiredMode.REQUIRED)
	private final String departmentName;

	@JsonCreator
	public DepartmentRequest(@JsonProperty("departmentName") String departmentName) {
		this.departmentName = departmentName;
	}

	public String departmentName() {
		return this.departmentName;
	}

}
