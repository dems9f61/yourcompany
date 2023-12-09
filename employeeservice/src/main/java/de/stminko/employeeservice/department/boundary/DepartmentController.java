package de.stminko.employeeservice.department.boundary;

import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonView;
import de.stminko.employeeservice.department.entity.Department;
import de.stminko.employeeservice.department.entity.DepartmentRequest;
import de.stminko.employeeservice.department.entity.DepartmentResponse;
import de.stminko.employeeservice.employee.entity.Employee;
import de.stminko.employeeservice.employee.entity.EmployeeResponse;
import de.stminko.employeeservice.runtime.errorhandling.boundary.NotFoundException;
import de.stminko.employeeservice.runtime.rest.bondary.ApiVersions;
import de.stminko.employeeservice.runtime.rest.bondary.DataView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * REST Controller for managing department-related operations in the Employee Service
 * application.
 * <p>
 * This controller provides a set of endpoints for various operations related to
 * departments, such as creating new departments. It utilizes {@link DepartmentService}
 * for the underlying business logic and data handling.
 * </p>
 *
 * @author Stéphan Minko
 * @see DepartmentService for the service layer used by this controller
 * @see ApiVersions for API versioning details
 * @see Department for the entity representing a department
 * @see DepartmentRequest for the request object used for creating a department
 * @see DepartmentResponse for the response object returned after creating a department
 */

@Slf4j
@RestController
@Tag(name = "Department", description = "The Department API")
@RequestMapping(DepartmentController.BASE_URI)
@ApiResponse(responseCode = "500", description = "An unexpected server error occurred")
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DepartmentController {

	/**
	 * The base URI for all department-related endpoints.
	 * <p>
	 * This constant represents the base path for all department-related RESTful web
	 * services. It is a combination of a version identifier from {@link ApiVersions} and
	 * the specific path segment for departments. This base URI is used to construct
	 * endpoints for various department-related operations such as creating a new
	 * department.
	 * </p>
	 *
	 * @see ApiVersions for information on API versioning strategy
	 */
	public static final String BASE_URI = ApiVersions.V1 + "/departments";

	private final DepartmentService departmentService;

	/**
	 * Creates a new department based on the provided request and returns it.
	 * <p>
	 * This endpoint accepts a request to create a new department. It validates the
	 * request and, if valid, creates a new department. The endpoint responds with the
	 * created department's details.
	 * </p>
	 * @param departmentRequest the request object containing the details for the new
	 * department.
	 * @return a {@link ResponseEntity} containing the created {@link DepartmentResponse}
	 * and the HTTP status code. The response includes a 'Location' header with the URL of
	 * the created department.
	 * @author Stéphan Minko
	 */
	@Operation(summary = "Create a new department", description = "Creates a new department and returns it")
	@ApiResponses({
			@ApiResponse(responseCode = "201", description = "department successfully created",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = Department.class))),
			@ApiResponse(responseCode = "400",
					description = "on any client related errors e.g., constraints violation, already existing department name") })
	@PostMapping(produces = { MediaType.APPLICATION_JSON_VALUE }, consumes = { MediaType.APPLICATION_JSON_VALUE })
	@JsonView(DataView.GET.class)
	public ResponseEntity<DepartmentResponse> create(@io.swagger.v3.oas.annotations.parameters.RequestBody(
			description = "Department request data", required = true, content = @Content(schema = @Schema(
					implementation = DepartmentRequest.class))) @RequestBody DepartmentRequest departmentRequest) {
		log.info("create( departmentRequest=[{}] )", departmentRequest);
		Department department = this.departmentService.create(departmentRequest);
		DepartmentResponse departmentResponse = new DepartmentResponse(department.getId(),
				department.getDepartmentName());
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.LOCATION,
				ServletUriComponentsBuilder.fromCurrentRequestUri()
					.path("/{id}")
					.buildAndExpand(department.getId())
					.toUri()
					.toASCIIString());
		return ResponseEntity.status(HttpStatus.CREATED).headers(headers).body(departmentResponse);
	}

	/**
	 * Finds and returns a single department by their ID.
	 * <p>
	 * This endpoint retrieves the details of an department specified by the provided ID.
	 * If the employee is found, their information is returned; otherwise, a 404 error is
	 * generated.
	 * </p>
	 * @param id the unique identifier of the employee.
	 * @return the {@link DepartmentResponse} containing the department's details.
	 */
	@Operation(summary = "Find an department by ID", description = "Returns a single department by their ID")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Successfully found and returned the department details",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = DepartmentResponse.class))),
			@ApiResponse(responseCode = "404", description = "Department not found with the provided ID") })
	@GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	@JsonView(DataView.GET.class)
	@ResponseStatus(HttpStatus.OK)
	public DepartmentResponse findDepartment(@Parameter(description = "Unique identifier of the department",
			required = true) @PathVariable("id") Long id) {
		log.info("findDepartment( id=[{}] )", id);
		Department department = this.departmentService.findById(id);
		return new DepartmentResponse(department.getId(), department.getDepartmentName());
	}

	/**
	 * Retrieves a set of all employees belonging to a specific department identified by
	 * its ID.
	 * <p>
	 * This method returns a set of {@link EmployeeResponse} objects representing the
	 * employees of the specified department. If the department has no employees or if the
	 * department itself is not found, an empty set is returned. In case the department
	 * with the given ID does not exist, a 404 error is returned.
	 * @param id the unique identifier of the department.
	 * @return a set of {@link EmployeeResponse} objects for the employees in the given
	 * department.
	 * @throws NotFoundException if no department is found with the provided ID.
	 */
	@Operation(summary = "List all employees by the department's ID",
			description = "Returns a set of all employees by the department's ID")
	@ApiResponses({ @ApiResponse(responseCode = "200",
			description = "Successfully found and returned a set of all employees by the department's ID or an empty set if there are no employees",
			content = @Content(mediaType = "application/json",
					schema = @Schema(implementation = EmployeeResponse.class, type = "array"))),
			@ApiResponse(responseCode = "404", description = "Department not found with the provided ID") })
	@GetMapping(value = "/{id}/employees", produces = MediaType.APPLICATION_JSON_VALUE)
	@JsonView(DataView.GET.class)
	@ResponseStatus(HttpStatus.OK)
	public Set<EmployeeResponse> findEmployeesByDepartment(@Parameter(
			description = "Unique identifier of the department", required = true) @PathVariable("id") Long id) {
		log.info("findEmployeesByDepartment( id= [{}] )", id);
		Set<Employee> employeeSet = this.departmentService.findAllEmployeesById(id);
		return employeeSet.stream().map((Employee employee) -> {
			Employee.FullName fullName = employee.getFullName();
			return new EmployeeResponse(employee.getId(), employee.getEmailAddress(),
					(fullName != null) ? fullName.getFirstName() : null,
					(fullName != null) ? fullName.getLastName() : null, employee.getBirthday(),
					employee.getDepartment().getDepartmentName());
		}).collect(Collectors.toSet());
	}

	/**
	 * Fully updates an existing department's data.
	 * <p>
	 * This endpoint allows for complete updates to an department's information. All
	 * fields will be updated to the values provided in the request. If the department is
	 * not found, a 404 error is generated.
	 * </p>
	 * @param id the unique identifier of the department to be updated.
	 * @param departmentRequest the request object containing the new details of the
	 * employee.
	 */
	@Operation(summary = "Fully update an department", description = "Updates an department's entire data")
	@ApiResponses({
			@ApiResponse(responseCode = "204",
					description = "Department data fully updated successfully, no content in the response"),
			@ApiResponse(responseCode = "400",
					description = "Bad request, possibly due to invalid data or missing fields in the request"),
			@ApiResponse(responseCode = "404", description = "Department not found with the provided ID") })
	@PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void doFullUpdate(
			@Parameter(description = "Unique identifier of the department",
					required = true) @PathVariable("id") Long id,
			@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Full department data for update",
					required = true, content = @Content(schema = @Schema(
							implementation = DepartmentRequest.class))) @RequestBody DepartmentRequest departmentRequest) {
		log.info("doFullUpdate( id= [{}], request= [{}])", id, departmentRequest);
		this.departmentService.doFullUpdate(id, departmentRequest);
	}

	/**
	 * Deletes a department by their ID.
	 * <p>
	 * This endpoint removes an department from the system based on the provided ID. If
	 * the department is not found, a 404 error is generated. If department is associated
	 * to some employee, a 409 is generated
	 * </p>
	 * @param id the unique identifier of the department to be deleted.
	 */
	@Operation(summary = "Deletes an department", description = "Deletes an department by their ID")
	@ApiResponses({
			@ApiResponse(responseCode = "204",
					description = "Department successfully deleted, no content in the response"),
			@ApiResponse(responseCode = "404", description = "Department not found with the provided ID"),
			@ApiResponse(responseCode = "409", description = "Department still associated to some employee") })
	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteDepartment(@Parameter(description = "Unique identifier of the department",
			required = true) @PathVariable("id") Long id) {
		log.info("deleteDepartment( id= [{}] )", id);
		this.departmentService.deleteById(id);
	}

}
