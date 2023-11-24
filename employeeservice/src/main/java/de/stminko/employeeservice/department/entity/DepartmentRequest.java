package de.stminko.employeeservice.department.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import de.stminko.employeeservice.runtime.rest.bondary.DataView;
import de.stminko.employeeservice.runtime.validation.constraints.boundary.NullOrNotBlank;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Request data structure for Department-related operations.
 * <p>
 * This record is used to encapsulate the data received in requests for creating or updating
 * department details. It includes validation annotations to ensure that the provided data
 * meets the expected format and constraints.
 * </p>
 *
 * @param departmentName The name of the department, which must not be blank when creating or updating.
 *                       For PATCH requests, it can be null or not blank.
 *
 * @author Stéphan Minko
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record DepartmentRequest(
		@JsonView({DataView.POST.class, DataView.PUT.class, DataView.PATCH.class})
		@NullOrNotBlank(groups = {DataView.PATCH.class})
		@NotBlank(message = "{errors.department.name.not-blank}", groups = {DataView.POST.class, DataView.PUT.class})
		@Schema(description = "Name of the department", example = "Human Resources", requiredMode = Schema.RequiredMode.REQUIRED)
		String departmentName) {

	@JsonCreator
	public DepartmentRequest(@JsonProperty(value = "departmentName") String departmentName) {
		this.departmentName = departmentName;
	}

}
