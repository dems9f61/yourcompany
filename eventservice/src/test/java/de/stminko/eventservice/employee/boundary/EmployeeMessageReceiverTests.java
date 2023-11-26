package de.stminko.eventservice.employee.boundary;

import de.stminko.eventservice.AbstractUnitTestSuite;
import de.stminko.eventservice.employee.entity.EmployeeEvent;
import de.stminko.eventservice.employee.entity.EmployeeMessage;
import info.solidsoft.mockito.java8.AssertionMatcher;
import org.apache.commons.lang3.RandomStringUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(OutputCaptureExtension.class)
@DisplayName("Unit tests for the employee message receiver ")
class EmployeeMessageReceiverTests extends AbstractUnitTestSuite {

	@Mock
	private ApplicationEventPublisher eventPublisher;

	@InjectMocks
	private EmployeeMessageReceiver employeeMessageReceiver;

	@DisplayName("Receiving an employee message should trigger an employee event")
	@Test
	void givenEmployeeMessage_whenReceive_thenPublish() {
		// Arrange
		EmployeeMessage employeeMessage = this.employeeMessageTestFactory.createDefault();
		Mockito.doNothing().when(this.eventPublisher).publishEvent(ArgumentMatchers.any());

		// Act
		this.employeeMessageReceiver.receiveEmployeeMessage(employeeMessage);

		// Assert
		Mockito.verify(this.eventPublisher).publishEvent(AssertionMatcher.assertArg((ApplicationEvent event) -> {
			Assertions.assertThat(event).isInstanceOf(EmployeeEvent.class);
			EmployeeEvent employeeEvent = (EmployeeEvent) event;
			Assertions.assertThat(employeeEvent.getEmployee()).isEqualTo(employeeMessage.getEmployee());
			Assertions.assertThat(employeeEvent.getEventType()).isEqualTo(employeeMessage.getEventType());
		}));
	}

	@DisplayName("Receiving an employee message should trigger an employee event")
	@Test
	void givenExceptionThrown_whenReceive_thenLogErrorMessage(CapturedOutput output) {
		// Arrange
		EmployeeMessage employeeMessage = this.employeeMessageTestFactory.createDefault();
		String errorMessage = RandomStringUtils.randomAlphabetic(23);
		Mockito.doThrow(new RuntimeException(errorMessage))
			.when(this.eventPublisher)
			.publishEvent(ArgumentMatchers.any());

		// Act
		this.employeeMessageReceiver.receiveEmployeeMessage(employeeMessage);

		// Assert
		Assertions.assertThat(output.getOut()).contains(errorMessage);
	}

}
