package de.stminko.eventservice.employee.entity;

import java.time.Instant;
import java.util.Date;

import de.stminko.eventservice.employee.boundary.dto.EventType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Represents a persistent entity for storing employee event data in MongoDB.
 *
 * <p>
 * This class models the structure of employee event data to be stored in a MongoDB
 * collection named "employee-events". It includes various details about the employee
 * event such as type, employee ID, email address, name, birthday, department name, and
 * the creation timestamp.
 * </p>
 *
 * <p>
 * Marked with {@code @Document(collection = "employee-events")}, it indicates the MongoDB
 * collection in which instances of this class are stored.
 * </p>
 *
 * <p>
 * Lombok annotations {@code @Getter}, {@code @Setter}, {@code @ToString}, and
 * {@code @EqualsAndHashCode} are used to automatically generate the corresponding
 * methods, reducing boilerplate code.
 * </p>
 *
 * <p>
 * The {@code @CreatedDate} annotation is used to automatically set the creation timestamp
 * when the entity is persisted.
 * </p>
 *
 * @author Stéphan Minko
 * @see org.springframework.data.mongodb.core.mapping.Document
 * @see org.springframework.data.annotation.Id
 * @see org.springframework.data.annotation.CreatedDate
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode
@Document(collection = "employee-events")
public class PersistentEmployeeEvent {

	@Id
	private String id;

	private EventType eventType;

	private String employeeId;

	private String emailAddress;

	private String firstName;

	private String lastName;

	private Date birthday;

	private String departmentName;

	@CreatedDate
	private Instant createdAt;

}
