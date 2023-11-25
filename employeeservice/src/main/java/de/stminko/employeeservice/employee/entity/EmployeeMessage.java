package de.stminko.employeeservice.employee.entity;

import lombok.Data;

/**
 * Represents a message related to employee events in the system. This class is used as a
 * data carrier for employee event information. It holds details about the type of event
 * and the employee involved in that event.
 * <p>
 * The {@code EventType} enum inside this class categorizes the type of event, such as
 * creation, update, or deletion of an employee.
 * <p>
 * Instances of this class are created and published by the {@code EmployeeEventPublisher}
 * to notify other components or services about the changes in the state of an employee.
 * <p>
 * Usage Example: <pre>
 *     EmployeeMessage message = new EmployeeMessage();
 *     message.setEventType(EmployeeMessage.EventType.EMPLOYEE_CREATED);
 *     message.setEmployee(newEmployee);
 * </pre>
 *
 * @author Stéphan Minko
 */
@Data
public class EmployeeMessage {

	/**
	 * Enumerates the types of events that can occur to an employee. This helps in
	 * categorizing and responding to different events differently. The event types
	 * include: - EMPLOYEE_CREATED: Indicates a new employee has been added to the system.
	 * - EMPLOYEE_UPDATED: Indicates an existing employee's details have been modified. -
	 * EMPLOYEE_DELETED: Indicates an employee has been removed from the system.
	 */
	public enum EventType {

		EMPLOYEE_CREATED,

		EMPLOYEE_UPDATED,

		EMPLOYEE_DELETED

	}

	/**
	 * The type of event that this message represents.
	 */
	private EventType eventType;

	/**
	 * The employee involved in the event.
	 */
	private Employee employee;

}
