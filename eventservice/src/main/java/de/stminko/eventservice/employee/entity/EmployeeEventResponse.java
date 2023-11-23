package de.stminko.eventservice.employee.entity;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import org.springframework.format.annotation.DateTimeFormat;

/**
 * Response object for employee event data.
 *
 * <p>This class encapsulates details about employee events. It includes information such as the type of event,
 * employee's ID, email address, name, birthday, department name, and the timestamp when the event was created.</p>
 *
 * <p>The birthday field is handled with custom JSON serialization and deserialization to manage the {@link ZonedDateTime} format.</p>
 *
 * @author Stéphan Minko
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
public class EmployeeEventResponse {

    @Schema(description = "Type of the employee event", example = "EMPLOYEE_CREATED")
    private EventType eventType;

    @Schema(description = "Unique identifier of the employee", example = "12345")
    private String employeeId;

    @Schema(description = "Employee's email address", example = "employee@example.com")
    private String emailAddress;

    @Schema(description = "Employee's first name", example = "John")
    private String firstName;

    @Schema(description = "Employee's last name", example = "Doe")
    private String lastName;

    @JsonDeserialize(using = JsonDateDeSerializer.class)
    @JsonSerialize(using = JsonDateSerializer.class)
    @DateTimeFormat(pattern = UsableDateFormat.Constants.DEFAULT_DATE_FORMAT)
    @Schema(description = "Employee's birthday", example = "1990-01-01")
    private ZonedDateTime birthday;

    @Schema(description = "Name of the employee's department", example = "Human Resources")
    private String departmentName;

    @Schema(description = "Timestamp when the event was created", example = "2023-04-05T10:15:30.00Z")
    private Instant createdAt;

    public EmployeeEventResponse(PersistentEmployeeEvent employeeEvent) {
        this.eventType = employeeEvent.getEventType();
        this.employeeId = employeeEvent.getEmployeeId();
        this.emailAddress = employeeEvent.getEmailAddress();
        this.firstName = employeeEvent.getFirstName();
        this.lastName = employeeEvent.getLastName();
        this.birthday = ZonedDateTime.ofInstant(employeeEvent.getBirthday().toInstant(), ZoneId.systemDefault());
        this.departmentName = employeeEvent.getDepartmentName();
        this.createdAt = employeeEvent.getCreatedAt();
    }

}
