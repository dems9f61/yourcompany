package de.stminko.employeeservice.employee.boundary.dto;

import de.stminko.employeeservice.employee.entity.Employee;
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
	 * Enum representing the various types of events related to employees within the Event
	 * Service application. This enumeration is crucial for identifying and responding to
	 * different employee-related actions.
	 */
	public enum EventType {

		/**
		 * Indicates that a new employee has been created. This event type is used when an
		 * employee record is added to the system, signaling the need for processes
		 * related to new employee onboarding or record creation.
		 */
		EMPLOYEE_CREATED,

		/**
		 * Signifies that an existing employee's details have been updated. This event
		 * type is triggered when changes are made to an employee's information, such as
		 * updates to personal details, job role, department, or employment status. It can
		 * be used to trigger processes that depend on up-to-date employee information.
		 */
		EMPLOYEE_UPDATED,

		/**
		 * Denotes the deletion of an employee from the system. This event type is used
		 * when an employee's record is removed, which might be relevant for processes
		 * related to off-boarding, archiving employee data, or updating organizational
		 * charts and directories.
		 */
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
