package de.stminko.employeeservice.employee.boundary;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonView;
import de.stminko.employeeservice.department.boundary.dto.DepartmentResponse;
import de.stminko.employeeservice.employee.boundary.dto.EmployeeRequest;
import de.stminko.employeeservice.employee.boundary.dto.EmployeeResponse;
import de.stminko.employeeservice.employee.control.EmployeeService;
import de.stminko.employeeservice.employee.entity.Employee;
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
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.history.Revision;
import org.springframework.data.web.PageableDefault;
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
@ApiResponse(responseCode = "500", description = "An unexpected server error occurred")
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
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
	 * Creates a page of EmployeeResponse objects from a page of Employee objects.
	 * @param employeePage the page of Employee objects to be converted
	 * @return a page of EmployeeResponse objects
	 */
	public static Page<EmployeeResponse> createEmployeeResponsePage(@NonNull Page<Employee> employeePage) {
		List<EmployeeResponse> employeeResponses = employeePage.getContent()
			.stream()
			.map(EmployeeController::createEmployeeResponse)
			.toList();
		return new PageImpl<>(employeeResponses, employeePage.getPageable(), employeePage.getTotalElements());
	}

	private static EmployeeResponse createEmployeeResponse(Employee employee) {
		Employee.FullName fullName = employee.getFullName();
		assert employee.getId() != null;
		return EmployeeResponse.builder()
			.employeeId(employee.getId())
			.emailAddress(employee.getEmailAddress())
			.firstName((fullName != null) ? fullName.getFirstName() : null)
			.lastName((fullName != null) ? fullName.getLastName() : null)
			.birthday(employee.getBirthday())
			.departmentName(employee.getDepartment().getDepartmentName())
			.build();
	}

	/**
	 * Creates a new employee based on the provided request and returns the created
	 * employee's details.
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
	public ResponseEntity<EmployeeResponse> createEmployee(@io.swagger.v3.oas.annotations.parameters.RequestBody(
			description = "Employee request data", required = true, content = @Content(schema = @Schema(
					implementation = EmployeeRequest.class))) @RequestBody EmployeeRequest employeeRequest) {
		log.info("createEmployee( employeeRequest= [{}] )", employeeRequest);
		Employee employee = this.employeeService.create(employeeRequest);
		EmployeeResponse employeeResponse = createEmployeeResponse(employee);
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.LOCATION,
				ServletUriComponentsBuilder.fromCurrentRequestUri()
					.path("/{departmentId}")
					.buildAndExpand(employee.getId())
					.toUri()
					.toASCIIString());
		return ResponseEntity.status(HttpStatus.CREATED).headers(headers).body(employeeResponse);
	}

	/**
	 * Finds and returns a single employee by their ID.
	 * @param employeeId the unique identifier of the employee.
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
	public EmployeeResponse findEmployee(@Parameter(description = "Unique identifier of the employee",
			required = true) @PathVariable("id") String employeeId) {
		log.info("findEmployee( departmentId=[{}] )", employeeId);
		Employee employee = this.employeeService.findById(employeeId);
		return createEmployeeResponse(employee);
	}

	/**
	 * Retrieves a paginated list of all employees.
	 *
	 * <p>
	 * This endpoint returns a page of employees, with each page containing up to 50
	 * employee records. The response is provided in JSON format and includes only the
	 * fields defined in the {@link DataView.GET} view.
	 * </p>
	 * @param pageable an object that encapsulates pagination information. This can be
	 * overridden by the client by specifying 'page' and 'size' request parameters.
	 * @return a {@link Page} of {@link EmployeeResponse} representing the paginated
	 * employee data.
	 * @see EmployeeResponse
	 * @see DataView.GET
	 */
	@Operation(summary = "Get all employees", description = "Retrieves a paginated list of all employees.")
	@ApiResponse(responseCode = "200", description = "Successful retrieval of employee list",
			content = @Content(mediaType = "application/json", schema = @Schema(implementation = PageImpl.class)))
	@GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	@JsonView(DataView.GET.class)
	@ResponseStatus(HttpStatus.OK)
	public Page<EmployeeResponse> findAllEmployees(@PageableDefault(50) Pageable pageable) {
		log.info("findAllEmployees()");
		return createEmployeeResponsePage(this.employeeService.findAll(pageable));
	}

	/**
	 * Retrieves a paginated list of revisions for a specific employee.
	 * <p>
	 * This method returns a page of {@link Revision} objects, each containing a
	 * {@link EmployeeResponse} that represents a state of the department at a certain
	 * point in time. Each revision includes metadata such as the revision type (insert,
	 * update, delete) and the timestamp of the revision.
	 * @param employeeId the ID of the employee for which to retrieve the revisions.
	 * @param pageable a {@link Pageable} object specifying the pagination information
	 * (page number, page size).
	 * @return a {@link Page} of {@link Revision} objects containing
	 * {@link EmployeeResponse} and revision metadata.
	 */
	@Operation(summary = "Find all revisions for a employee",
			description = "Returns a page of revisions for the specified employee ID")
	@ApiResponse(responseCode = "200", description = "Successfully retrieved the revisions",
			content = @Content(mediaType = "application/json", schema = @Schema(implementation = PageImpl.class)))
	@GetMapping(value = "/{employeeId}/revisions", produces = MediaType.APPLICATION_JSON_VALUE)
	@JsonView(DataView.GET.class)
	public Page<Revision<Long, EmployeeResponse>> findAllRevisions(
			@Parameter(description = "Unique identifier of the employee",
					required = true) @PathVariable String employeeId,
			@PageableDefault(50) Pageable pageable) {
		log.info("findAllRevisions( employeeId= [{}] )", employeeId);
		Page<Revision<Long, Employee>> employeeRevisionsPage = this.employeeService.findRevisions(employeeId, pageable);
		List<Revision<Long, EmployeeResponse>> responseRevisions = employeeRevisionsPage.getContent()
			.stream()
			.map(this::createEmployeeResponseRevision)
			.toList();

		return new PageImpl<>(responseRevisions, employeeRevisionsPage.getPageable(),
				employeeRevisionsPage.getTotalElements());
	}

	/**
	 * Find the latest {@link Revision} for an employee identified by its departmentId.
	 * @param employeeId the departmentId of the employee to retrieve the latest
	 * {@link Revision} for
	 * @return the latest {@link Revision} of the given employee
	 * @throws NotFoundException if no such {@link Revision} entry exists
	 */
	@Operation(summary = "Find the latest change revision of a department",
			description = "Returns the latest revision of a department by its ID")
	@ApiResponse(responseCode = "200", description = "Successful retrieval",
			content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
					schema = @Schema(implementation = DepartmentResponse.class)))
	@ApiResponse(responseCode = "404", description = "Revision not found")
	@GetMapping(value = "/{employeeId}/revisions/latest", produces = MediaType.APPLICATION_JSON_VALUE)
	@JsonView(DataView.GET.class)
	public Revision<Long, EmployeeResponse> findLastChangeRevision(
			@Parameter(description = "ID of the department") @PathVariable String employeeId) {
		log.info("findLastChangeRevision( employeeId= [{}])", employeeId);
		Revision<Long, Employee> lastChangeRevision = this.employeeService.findLastChangeRevision(employeeId);
		return createEmployeeResponseRevision(lastChangeRevision);
	}

	/**
	 * Partially updates an existing employee's data.
	 * <p>
	 * This endpoint allows for partial updates to an employee's information. Only the
	 * provided fields in the request will be updated. If the employee is not found, a 404
	 * error is generated.
	 * </p>
	 * @param employeeId the unique identifier of the employee to be updated.
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
	public void doPartialUpdate(
			@Parameter(description = "Unique identifier of the employee",
					required = true) @PathVariable("id") String employeeId,
			@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Partial employee data for update",
					required = true, content = @Content(schema = @Schema(
							implementation = EmployeeRequest.class))) @RequestBody EmployeeRequest employeeRequest) {
		log.info("doPartialUpdate( departmentId= [{}], request= [{}])", employeeId, employeeRequest);
		this.employeeService.doPartialUpdate(employeeId, employeeRequest);
	}

	/**
	 * Fully updates an existing employee's data.
	 * <p>
	 * This endpoint allows for complete updates to an employee's information. All fields
	 * will be updated to the values provided in the request. If the employee is not
	 * found, a 404 error is generated.
	 * </p>
	 * @param employeeId the unique identifier of the employee to be updated.
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
	public void doFullUpdate(
			@Parameter(description = "Unique identifier of the employee",
					required = true) @PathVariable("id") String employeeId,
			@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Full employee data for update",
					required = true, content = @Content(schema = @Schema(
							implementation = EmployeeRequest.class))) @RequestBody EmployeeRequest employeeRequest) {
		log.info("doFullUpdate( departmentId= [{}], request= [{}])", employeeId, employeeRequest);
		this.employeeService.doFullUpdate(employeeId, employeeRequest);
	}

	/**
	 * Deletes an employee by their ID.
	 * <p>
	 * This endpoint removes an employee from the system based on the provided ID. If the
	 * employee is not found, a 404 error is generated.
	 * </p>
	 * @param employeeId the unique identifier of the employee to be deleted.
	 */
	@Operation(summary = "Deletes an employee", description = "Deletes an employee by their ID")
	@ApiResponses({
			@ApiResponse(responseCode = "204",
					description = "Employee successfully deleted, no content in the response"),
			@ApiResponse(responseCode = "404", description = "Employee not found with the provided ID") })
	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteEmployee(@Parameter(description = "Unique identifier of the employee",
			required = true) @PathVariable("id") String employeeId) {
		log.info("deleteEmployee( departmentId= [{}] )", employeeId);
		this.employeeService.deleteById(employeeId);
	}

	private Revision<Long, EmployeeResponse> createEmployeeResponseRevision(
			Revision<Long, Employee> lastChangeRevision) {
		EmployeeResponse employeeResponse = createEmployeeResponse(lastChangeRevision.getEntity());
		return Revision.of(lastChangeRevision.getMetadata(), employeeResponse);
	}

}
