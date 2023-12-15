package de.stminko.eventservice.employee.boundary;

import de.stminko.eventservice.employee.boundary.dto.EmployeeEventResponse;
import de.stminko.eventservice.employee.control.EmployeeEventService;
import de.stminko.eventservice.runtime.rest.boundary.ApiVersions;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller for managing employee events.
 *
 * <p>
 * This controller handles HTTP requests related to employee events. It provides endpoints
 * for retrieving employee events in a paginated format.
 *
 * <p>
 * This controller uses {@code EmployeeEventService} for business logic and data
 * retrieval. The service layer abstraction helps maintain a clean separation between the
 * web layer and the service layer.
 * </p>
 *
 * @author Stéphan Minko
 */
@Tag(name = "Employee-Event", description = "API controller for managing employee events.")
@Slf4j
@RestController
@RequestMapping(value = EmployeeEventController.BASE_URI, produces = MediaType.APPLICATION_JSON_VALUE)
@ApiResponse(responseCode = "500", description = "An unexpected server error occurred")
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class EmployeeEventController {

	/**
	 * The base URI for all endpoints in this controller. It combines the API version
	 * prefix from {@link ApiVersions} with the "/events" path.
	 */
	public static final String BASE_URI = ApiVersions.V1 + "/events";

	private final EmployeeEventService employeeEventService;

	/**
	 * Retrieves a paginated list of employee events for a specified employee, ordered by
	 * creation date in ascending order.
	 *
	 * <p>
	 * This endpoint responds to GET requests at '/{employeeId}' and returns a page of
	 * {@code EmployeeEventResponse} objects. The events are specifically related to the
	 * employee identified by the provided employee ID. Pagination and sorting are
	 * supported, with sorting by the creation date of the events in ascending order.
	 * </p>
	 *
	 * <p>
	 * The default pagination setting is configured to return up to 50 records per page if
	 * not specified in the request. The client can modify pagination settings by
	 * providing standard Spring Data pagination parameters in the request (e.g., page,
	 * size, sort).
	 * </p>
	 *
	 * <p>
	 * This method maps the retrieved data to {@code EmployeeEventResponse} objects,
	 * providing a clean and structured response format suitable for client consumption.
	 * </p>
	 * @param employeeId the unique identifier of the employee for whom events are being
	 * retrieved
	 * @param pageable the pagination information (page number, page size, sorting
	 * criteria)
	 * @return a {@link Page} of {@link EmployeeEventResponse} containing the employee
	 * events
	 */
	@Operation(summary = "Find employee events by UUID",
			description = "Retrieves a paginated list of employee events for a specified employee, ordered by creation date in ascending order.",
			responses = @ApiResponse(responseCode = "200",
					description = "Successfully retrieved the list of employee events",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = EmployeeEventResponse.class))))
	@GetMapping("/{employeeId}")
	@ResponseStatus(HttpStatus.OK)
	public Page<EmployeeEventResponse> findByUuidOrderByCreatedAtAsc(
			@Parameter(description = "Unique identifier of the employee",
					required = true) @PathVariable("employeeId") String employeeId,
			@Parameter(description = "Pagination and sorting parameters",
					required = true) @PageableDefault(50) Pageable pageable) {
		log.info("findByUuidOrderByCreatedAtAsc( employeeId= [{}])", employeeId);
		return this.employeeEventService.findByEmployeeIdOrderByCreatedAtAsc(employeeId, pageable)
			.map(EmployeeEventResponse::new);
	}

}
