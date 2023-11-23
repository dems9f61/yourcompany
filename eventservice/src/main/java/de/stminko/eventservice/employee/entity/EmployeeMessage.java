package de.stminko.eventservice.employee.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Data object for employee messages.
 *
 * <p>This class encapsulates the data for employee-related messages, including the type of event and the employee details.
 * It is used in message-driven operations, particularly in interaction with message queues like RabbitMQ.</p>
 *
 * @author Stéphan Minko
 */
@Data
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeMessage {

    private EventType eventType;

    private Employee employee;

}
