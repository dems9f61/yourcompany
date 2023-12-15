package de.stminko.employeeservice.department.boundary.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import de.stminko.employeeservice.runtime.rest.bondary.DataView;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

/**
 * Response data structure for department-related operations.
 * <p>
 * This record is used to encapsulate the department data sent in responses from the
 * server. It includes the department's identifier and name. The {@link JsonView}
 * annotation is used to control the serialization visibility in different scenarios.
 * </p>
 *
 * @param departmentId The unique identifier of the department.
 * @param departmentName The name of the department.
 * @author Stéphan Minko
 */
@JsonView(DataView.GET.class)
@Builder
public record DepartmentResponse(
		@Schema(description = "The unique identifier of the department", example = "1") Long departmentId,
		@Schema(description = "Name of the department", example = "Human Resources") String departmentName) {

	@JsonCreator
	public DepartmentResponse(@JsonProperty("departmentId") Long departmentId,
			@JsonProperty("departmentName") String departmentName) {
		this.departmentId = departmentId;
		this.departmentName = departmentName;
	}

}
