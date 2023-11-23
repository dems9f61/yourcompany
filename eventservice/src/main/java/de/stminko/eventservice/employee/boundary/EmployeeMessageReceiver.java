package de.stminko.eventservice.employee.boundary;

import de.stminko.eventservice.employee.entity.EmployeeEvent;
import de.stminko.eventservice.employee.entity.EmployeeMessage;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Component responsible for receiving employee-related messages from a RabbitMQ queue.
 *
 * <p>This class acts as a message listener for employee messages published on a specified RabbitMQ queue.
 *
 * <p>Messages are consumed from the queue defined by the '${amqp.queue-name}' property.
 * Upon receiving a message, the class logs the message details and attempts to process it by publishing an {@code EmployeeEvent}
 * using Spring's {@code ApplicationEventPublisher}.</p>
 *
 * <p>In case of any exceptions during message processing, the exception is caught and logged to avoid infinite loops
 * and to ensure the message is marked as processed in the message broker.</p>
 *
 * @author Stéphan Minko
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class EmployeeMessageReceiver {

    private final ApplicationEventPublisher eventPublisher;

    /**
     * Receives and processes employee messages from a RabbitMQ queue.
     *
     * <p>This method is invoked when a message is received from the RabbitMQ queue specified by the '${amqp.queue-name}' property.
     * It processes employee-related messages by publishing an {@link EmployeeEvent}.</p>
     *
     * <p>The method takes an {@link EmployeeMessage} object which contains the data from the message.
     * It logs the received message, then attempts to process it by publishing an {@link EmployeeEvent}
     * to the application's event system.</p>
     *
     * <p>In case of any exceptions during message processing, the exception is caught and logged.
     * This ensures that the message is marked as processed in the message broker, avoiding infinite loops in message processing.</p>
     *
     * @param employeeMessage the employee message received from the queue
     */
    @RabbitListener(queues = "${amqp.queue-name}")
    public void receiveEmployeeMessage(@NonNull EmployeeMessage employeeMessage) {
        log.info("###### Received Message on employee ##### [{}]", employeeMessage);
        // Catch any Exception to have the Message marked as processed on the Message
        // Broker
        // Avoids infinite Loops on Message Processing
        try {
            this.eventPublisher
                    .publishEvent(new EmployeeEvent(employeeMessage.getEmployee(), employeeMessage.getEventType()));
        } catch (Exception caught) {
            log.error("An error occurred during EmployeeMessageReceiver.receiveEmployeeMessage ( [{}] ). Error was: {}",
                    employeeMessage, caught.getMessage(), caught);
        }
    }

}
