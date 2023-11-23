package de.stminko.eventservice.employee.boundary;


import de.stminko.eventservice.AbstractUnitTestSuite;
import de.stminko.eventservice.employee.entity.EmployeeEvent;
import de.stminko.eventservice.employee.entity.EmployeeMessage;
import info.solidsoft.mockito.java8.AssertionMatcher;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import org.springframework.context.ApplicationEventPublisher;

@DisplayName("Unit tests for the employee message receiver ")
class EmployeeMessageReceiverTest extends AbstractUnitTestSuite {

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private EmployeeMessageReceiver employeeMessageReceiver;

    @DisplayName("Receiving an employee message should trigger an employee event")
    @Test
    void givenEmployeeMessage_whenReceive_thenPublish() {
        // Arrange
        EmployeeMessage employeeMessage = employeeMessageTestFactory.createDefault();
        Mockito.doNothing().when(eventPublisher).publishEvent(ArgumentMatchers.any());

        // Act
        employeeMessageReceiver.receiveEmployeeMessage(employeeMessage);

        // Assert
        Mockito.verify(eventPublisher)
                .publishEvent(AssertionMatcher.assertArg(event -> {
                    Assertions.assertThat(event).isInstanceOf(EmployeeEvent.class);
                    EmployeeEvent employeeEvent = (EmployeeEvent) event;
                    Assertions.assertThat(employeeEvent.getEmployee()).isEqualTo(employeeMessage.getEmployee());
                    Assertions.assertThat(employeeEvent.getEventType()).isEqualTo(employeeMessage.getEventType());
                }));
    }

}