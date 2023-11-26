package de.stminko.employeeservice.employee.boundary;

import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonView;
import de.stminko.employeeservice.employee.control.EmployeeService;
import de.stminko.employeeservice.employee.entity.Employee;
import de.stminko.employeeservice.employee.entity.EmployeeRequest;
import de.stminko.employeeservice.employee.entity.EmployeeResponse;
import de.stminko.employeeservice.runtime.rest.bondary.ApiVersions;
import de.stminko.employeeservice.runtime.rest.bondary.DataView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * rest controller for handling employee-related operations.
 * <p>
 * Provides endpoints for managing employees, including creating new employees, finding
 * employees by ID, listing all employees, updating employee information, and deleting
 * employees. It interacts with {@link EmployeeService} for business logic and data
 * processing.
 * </p>
 *
 * @author Stéphan Minko
 * @see EmployeeService
 * @see Employee
 * @see EmployeeRequest
 * @see EmployeeResponse
 */
@Slf4j
@RestController
@Tag(name = "Employee", description = "The Employee API")
@RequestMapping(EmployeeController.BASE_URI)
@RequiredArgsConstructor
public class EmployeeController {

	/**
	 * The base URI for all employee-related endpoints.
	 * <p>
	 * This constant defines the root path for all employee-related RESTful web services
	 * in the application. It is composed of a version identifier from
	 * {@link ApiVersions}, ensuring consistent versioning across the API, and the
	 * '/employees' path segment, which specifically denotes the resources related to
	 * employee operations. This base URI is utilized to construct various endpoint URLs
	 * for operations like creating, updating, finding, and deleting employees.
	 * </p>
	 *
	 * @see ApiVersions for information on API versioning strategy
	 */
	public static final String BASE_URI = ApiVersions.V1 + "/employees";

	private final EmployeeService employeeService;

	/**
	 * Creates a new employee based on the provided request and returns the created
	 * employee's details.
	 * <p>
	 * This endpoint accepts a request to create a new employee, validates the request,
	 * and if valid, creates a new employee in the system. It returns the details of the
	 * created employee along with a 'Location' header in the response pointing to the URI
	 * of the new employee.
	 * </p>
	 * @param employeeRequest the request object containing the new employee's details.
	 * @return a {@link ResponseEntity} containing the created {@link EmployeeResponse}
	 * and the HTTP status code.
	 */
	@Operation(summary = "Create a new employee", description = "Creates a new employee and returns it")
	@ApiResponses({
			@ApiResponse(responseCode = "201", description = "employee successfully created",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = Employee.class))),
			@ApiResponse(responseCode = "400",
					description = "on any client related errors e.g., constraints violation, non unique email") })
	@PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	@JsonView(DataView.GET.class)
	@ResponseStatus(HttpStatus.CREATED)
	ResponseEntity<EmployeeResponse> createEmployee(@io.swagger.v3.oas.annotations.parameters.RequestBody(
			description = "Employee request data", required = true, content = @Content(schema = @Schema(
					implementation = EmployeeRequest.class))) @RequestBody EmployeeRequest employeeRequest) {
		log.info("createEmployee( employeeRequest= [{}] )", employeeRequest);
		Employee employee = this.employeeService.create(employeeRequest);
		String newId = employee.getId();
		EmployeeResponse employeeResponse = new EmployeeResponse(newId, employee.getEmailAddress(),
				employee.getFullName().getFirstName(), employee.getFullName().getLastName(), employee.getBirthday(),
				employee.getDepartment().getDepartmentName());
		HttpHeaders headers = new HttpHeaders();
		assert newId != null;
		headers.add(HttpHeaders.LOCATION,
				ServletUriComponentsBuilder.fromCurrentRequestUri()
					.path("/{id}")
					.buildAndExpand(newId)
					.toUri()
					.toASCIIString());
		return ResponseEntity.status(HttpStatus.CREATED).headers(headers).body(employeeResponse);
	}

	/**
	 * Finds and returns a single employee by their ID.
	 * <p>
	 * This endpoint retrieves the details of an employee specified by the provided ID. If
	 * the employee is found, their information is returned; otherwise, a 404 error is
	 * generated.
	 * </p>
	 * @param id the unique identifier of the employee.
	 * @return the {@link EmployeeResponse} containing the employee's details.
	 */
	@Operation(summary = "Find an employee by ID", description = "Returns a single employee by their ID")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Successfully found and returned the employee details",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = EmployeeResponse.class))),
			@ApiResponse(responseCode = "404", description = "Employee not found with the provided ID") })
	@GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	@JsonView(DataView.GET.class)
	@ResponseStatus(HttpStatus.OK)
	EmployeeResponse findEmployee(@Parameter(description = "Unique identifier of the employee",
			required = true) @PathVariable("id") String id) {
		log.info("findEmployee( id=[{}] )", id);
		Employee employee = this.employeeService.findById(id);
		Employee.FullName fullName = employee.getFullName();
		return new EmployeeResponse(employee.getId(), employee.getEmailAddress(),
				(fullName != null) ? fullName.getFirstName() : null, (fullName != null) ? fullName.getLastName() : null,
				employee.getBirthday(), employee.getDepartment().getDepartmentName());
	}

	/**
	 * Lists all employees in the system.
	 * <p>
	 * This endpoint returns a list of all employees currently stored in the system. If no
	 * employees are found, an empty list is returned.
	 * </p>
	 * @return a list of {@link EmployeeResponse} representing all the employees.
	 */
	@Operation(summary = "List all employees", description = "Returns a list of all employees")
	@ApiResponse(responseCode = "200", description = "Successfully retrieved and returned the list of all employees",
			content = @Content(mediaType = "application/json",
					schema = @Schema(implementation = EmployeeResponse.class, type = "array")))
	@GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	@JsonView(DataView.GET.class)
	@ResponseStatus(HttpStatus.OK)
	List<EmployeeResponse> findAllEmployees() {
		log.info("findAllEmployees()");
		return this.employeeService.findAll().stream().map((Employee employee) -> {
			Employee.FullName fullName = employee.getFullName();
			String firstName = (fullName != null) ? fullName.getFirstName() : null;
			String lastName = (fullName != null) ? fullName.getLastName() : null;
			return new EmployeeResponse(employee.getId(), employee.getEmailAddress(), firstName, lastName,
					employee.getBirthday(), employee.getDepartment().getDepartmentName());
		}).collect(Collectors.toList());
	}

	/**
	 * Partially updates an existing employee's data.
	 * <p>
	 * This endpoint allows for partial updates to an employee's information. Only the
	 * provided fields in the request will be updated. If the employee is not found, a 404
	 * error is generated.
	 * </p>
	 * @param id the unique identifier of the employee to be updated.
	 * @param employeeRequest the request object containing the fields to be updated.
	 */
	@Operation(summary = "Partially update an employee", description = "Updates a subset of an employee's data")
	@ApiResponses({
			@ApiResponse(responseCode = "204",
					description = "Employee data partially updated successfully, no content in the response"),
			@ApiResponse(responseCode = "400",
					description = "Bad request, possibly due to invalid data or missing fields in the request"),
			@ApiResponse(responseCode = "404", description = "Employee not found with the provided ID") })
	@PatchMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	void doPartialUpdate(
			@Parameter(description = "Unique identifier of the employee",
					required = true) @PathVariable("id") String id,

			@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Partial employee data for update",
					required = true, content = @Content(schema = @Schema(
							implementation = EmployeeRequest.class))) @RequestBody EmployeeRequest employeeRequest) {
		log.info("doPartialUpdate( id= [{}], request= [{}])", id, employeeRequest);
		this.employeeService.doPartialUpdate(id, employeeRequest);
	}

	/**
	 * Fully updates an existing employee's data.
	 * <p>
	 * This endpoint allows for complete updates to an employee's information. All fields
	 * will be updated to the values provided in the request. If the employee is not
	 * found, a 404 error is generated.
	 * </p>
	 * @param id the unique identifier of the employee to be updated.
	 * @param employeeRequest the request object containing the new details of the
	 * employee.
	 */
	@Operation(summary = "Fully update an employee", description = "Updates an employee's entire data")
	@ApiResponses({
			@ApiResponse(responseCode = "204",
					description = "Employee data fully updated successfully, no content in the response"),
			@ApiResponse(responseCode = "400",
					description = "Bad request, possibly due to invalid data or missing fields in the request"),
			@ApiResponse(responseCode = "404", description = "Employee not found with the provided ID") })
	@PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	void doFullUpdate(
			@Parameter(description = "Unique identifier of the employee",
					required = true) @PathVariable("id") String id,

			@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Full employee data for update",
					required = true, content = @Content(schema = @Schema(
							implementation = EmployeeRequest.class))) @RequestBody EmployeeRequest employeeRequest) {
		log.info("doFullUpdate( id= [{}], request= [{}])", id, employeeRequest);
		this.employeeService.doFullUpdate(id, employeeRequest);
	}

	/**
	 * Deletes an employee by their ID.
	 * <p>
	 * This endpoint removes an employee from the system based on the provided ID. If the
	 * employee is not found, a 404 error is generated.
	 * </p>
	 * @param id the unique identifier of the employee to be deleted.
	 */
	@Operation(summary = "Delete an employee", description = "Deletes an employee by their ID")
	@ApiResponses({
			@ApiResponse(responseCode = "204",
					description = "Employee successfully deleted, no content in the response"),
			@ApiResponse(responseCode = "404", description = "Employee not found with the provided ID") })
	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	void deleteEmployee(@Parameter(description = "Unique identifier of the employee",
			required = true) @PathVariable("id") String id) {
		log.info("deleteEmployee( id= [{}] )", id);
		this.employeeService.deleteById(id);
	}

}
