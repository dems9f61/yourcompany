package de.stminko.employeeservice.employee.control;

import de.stminko.employeeservice.employee.entity.Employee;
import de.stminko.employeeservice.employee.entity.EmployeeMessage;
import de.stminko.employeeservice.runtime.amqp.boundary.AmqpConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * Component for publishing employee-related events through RabbitMQ messaging.
 * <p>
 * This class is responsible for sending messages related to employee lifecycle events
 * such as creation, deletion, and updates. It uses Spring AMQP's {@link RabbitTemplate}
 * for message sending and relies on {@link AmqpConfig} for configuration details like
 * exchange name and routing key.
 * </p>
 *
 * @see RabbitTemplate for AMQP-based messaging
 * @see AmqpConfig for configuration of AMQP settings
 * @see Employee for the entity representing an employee
 * @author Stéphan Minko
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class EmployeeEventPublisher {

	private final RabbitTemplate template;

	private final AmqpConfig amqpConfig;

	/**
	 * Publishes an event indicating that a new employee has been created.
	 * @param createdEmployee the employee that was created
	 */
	public void employeeCreated(Employee createdEmployee) {
		log.info("employeeCreated( createdEmployee= [{}] )", createdEmployee);
		EmployeeMessage createdEmployeeMessage = new EmployeeMessage();
		createdEmployeeMessage.setEventType(EmployeeMessage.EventType.EMPLOYEE_CREATED);
		createdEmployeeMessage.setEmployee(createdEmployee);
		this.template.convertAndSend(this.amqpConfig.getExchangeName(), this.amqpConfig.getRoutingKey(),
				createdEmployeeMessage);
	}

	/**
	 * Publishes an event indicating that an employee has been deleted.
	 * @param deletedEmployee the employee that was deleted
	 */
	public void employeeDeleted(Employee deletedEmployee) {
		log.info("employeeDeleted( deletedEmployee= [{}] )", deletedEmployee);
		EmployeeMessage deletedEmployeeMessage = new EmployeeMessage();
		deletedEmployeeMessage.setEmployee(deletedEmployee);
		deletedEmployeeMessage.setEventType(EmployeeMessage.EventType.EMPLOYEE_DELETED);
		this.template.convertAndSend(this.amqpConfig.getExchangeName(), this.amqpConfig.getRoutingKey(),
				deletedEmployeeMessage);
	}

	/**
	 * Publishes an event indicating that an employee's details have been updated.
	 * @param updatedEmployee the employee that was updated
	 */
	public void employeeUpdated(Employee updatedEmployee) {
		log.info("employeeUpdated( updatedEmployee= [{}] )", updatedEmployee);
		EmployeeMessage updatedEmployeeMessage = new EmployeeMessage();
		updatedEmployeeMessage.setEventType(EmployeeMessage.EventType.EMPLOYEE_UPDATED);
		updatedEmployeeMessage.setEmployee(updatedEmployee);
		this.template.convertAndSend(this.amqpConfig.getExchangeName(), this.amqpConfig.getRoutingKey(),
				updatedEmployeeMessage);
	}

}
