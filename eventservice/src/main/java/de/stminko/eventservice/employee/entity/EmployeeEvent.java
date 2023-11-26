package de.stminko.eventservice.employee.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

import org.springframework.context.ApplicationEvent;

/**
 * custom event representing actions or changes related to an {@link Employee}.
 *
 * <p>
 * This class extends {@link ApplicationEvent} and is used to encapsulate details about
 * events concerning {@link Employee} objects, such as creation, update, or deletion. It
 * holds references to the {@link Employee} involved in the event and the type of event.
 * </p>
 *
 * @author Stéphan Minko
 */
@Getter
@ToString
@EqualsAndHashCode(callSuper = false)
public class EmployeeEvent extends ApplicationEvent {

	private final Employee employee;

	private final EventType eventType;

	/**
	 * Constructs a new EmployeeEvent.
	 * @param employee the {@link Employee} object associated with this event.
	 * @param eventType the type of the event, represented by the {@link EventType} enum.
	 */
	public EmployeeEvent(@NonNull Employee employee, @NonNull EventType eventType) {
		super(employee);
		this.employee = employee;
		this.eventType = eventType;
	}

}
