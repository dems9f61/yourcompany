package de.stminko.employeeservice.department.boundary;

import com.fasterxml.jackson.annotation.JsonView;
import de.stminko.employeeservice.department.entity.Department;
import de.stminko.employeeservice.department.entity.DepartmentRequest;
import de.stminko.employeeservice.department.entity.DepartmentResponse;
import de.stminko.employeeservice.runtime.rest.bondary.ApiVersions;
import de.stminko.employeeservice.runtime.rest.bondary.DataView;
import io.swagger.v3.oas.annotations.Operation;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * REST Controller for managing department-related operations in the Employee Service application.
 * <p>
 * This controller provides a set of endpoints for various operations related to departments,
 * such as creating new departments. It utilizes {@link DepartmentService} for the underlying business logic
 * and data handling.
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
@RequestMapping(value = DepartmentController.BASE_URI)
@RequiredArgsConstructor
public class DepartmentController {

    /**
     * The base URI for all department-related endpoints.
     * <p>
     * This constant represents the base path for all department-related RESTful web services.
     * It is a combination of a version identifier from {@link ApiVersions} and the specific path segment for departments.
     * This base URI is used to construct endpoints for various department-related operations such as creating a new department.
     * </p>
     *
     * @see ApiVersions for information on API versioning strategy
     */
    public static final String BASE_URI = ApiVersions.V1 + "/departments";

    private final DepartmentService departmentService;

    /**
     * Creates a new department based on the provided request and returns it.
     * <p>
     * This endpoint accepts a request to create a new department. It validates the request and,
     * if valid, creates a new department. The endpoint responds with the created department's details.
     * </p>
     *
     * @param departmentRequest The request object containing the details for the new department.
     * @return A {@link ResponseEntity} containing the created {@link DepartmentResponse} and the HTTP status code.
     * The response includes a 'Location' header with the URL of the created department.
     * @author Stéphan Minko
     */
    @Operation(summary = "Create a new department", description = "Creates a new department and returns it")
    @ApiResponses({@ApiResponse(responseCode = "201", description = "department successfully created",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Department.class))),
            @ApiResponse(responseCode = "400",
                    description = "on any client related errors e.g., constraints violation, already existing department name"),
            @ApiResponse(responseCode = "500", description = "Internal server error")})
    @PostMapping(produces = {MediaType.APPLICATION_JSON_VALUE}, consumes = {MediaType.APPLICATION_JSON_VALUE})
    @JsonView(DataView.GET.class)
    ResponseEntity<DepartmentResponse> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Department request data",
                    required = true,
                    content = @Content(schema = @Schema(implementation = DepartmentRequest.class)))
            @RequestBody DepartmentRequest departmentRequest) {
        log.info("create( departmentRequest=[{}] )", departmentRequest);
        Department department = this.departmentService.create(departmentRequest);
        DepartmentResponse departmentResponse = new DepartmentResponse(department.getId(),
                department.getDepartmentName());
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.LOCATION, ServletUriComponentsBuilder.fromCurrentRequestUri().path("/{id}")
                .buildAndExpand(department.getId()).toUri().toASCIIString());
        return ResponseEntity.status(HttpStatus.CREATED).headers(headers).body(departmentResponse);
    }

}
