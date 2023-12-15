package de.stminko.eventservice.employee.boundary.dto;

/**
 * Represents the types of events that can occur in the system.
 * <p>
 * This enumeration defines different types of events related to employee operations,
 * allowing for a consistent way to handle various actions in the application.
 *
 * @author Stéphan Minko
 */
public enum EventType {

	/**
	 * Event type for when an employee is created.
	 */
	EMPLOYEE_CREATED,

	/**
	 * Event type for when an employee's information is updated.
	 */
	EMPLOYEE_UPDATED,

	/**
	 * Event type for when an employee is deleted.
	 */
	EMPLOYEE_DELETED

}
