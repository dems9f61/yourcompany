package de.stminko.employeeservice.employee.control;

import java.util.function.Consumer;

import de.stminko.employeeservice.employee.entity.Employee;
import de.stminko.employeeservice.employee.entity.EmployeeMessage;
import de.stminko.employeeservice.employee.entity.EmployeeTestFactory;
import de.stminko.employeeservice.runtime.amqp.boundary.AmqpConfig;
import info.solidsoft.mockito.java8.AssertionMatcher;
import org.apache.commons.lang3.RandomStringUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmployeeEventPublisherTest {

	@Mock
	private RabbitTemplate template;

	@Mock
	private AmqpConfig amqpConfig;

	private EmployeeEventPublisher publisher;

	private final EmployeeTestFactory employeeTestFactory = new EmployeeTestFactory();

	@BeforeEach
	void setUp() {
		publisher = new EmployeeEventPublisher(template, amqpConfig);
	}

	@DisplayName("Creating a employee lead to a EmployeeMessage of type EMPLOYEE_CREATED")
	@Test
	void givenEmployee_whenEmployeeCreated_thenSendCreatedEmployeeMessage() {

		givenEmployee_whenEmployeeProcessed_thenSendEmployeeMessage(
				(Employee value) -> publisher.employeeCreated(value), EmployeeMessage.EventType.EMPLOYEE_CREATED);
	}

	@DisplayName("Deleting a employee lead to a EmployeeMessage of type EMPLOYEE_DELEED")
	@Test
	void givenEmployee_whenEmployeeCreated_thenSendDeletedEmployeeMessage() {
		givenEmployee_whenEmployeeProcessed_thenSendEmployeeMessage(
				(Employee value) -> publisher.employeeDeleted(value), EmployeeMessage.EventType.EMPLOYEE_DELETED);
	}

	@DisplayName("Deleting a employee lead to a EmployeeMessage of type EMPLOYEE_UPDATED")
	@Test
	void givenEmployee_whenEmployeeCreated_thenSendUpdatedEmployeeMessage() {
		givenEmployee_whenEmployeeProcessed_thenSendEmployeeMessage(
				(Employee value) -> publisher.employeeUpdated(value), EmployeeMessage.EventType.EMPLOYEE_UPDATED);
	}

	private void givenEmployee_whenEmployeeProcessed_thenSendEmployeeMessage(Consumer<Employee> block,
			EmployeeMessage.EventType expectedEventType) {
		// Arrange
		Employee employee = employeeTestFactory.createDefault();
		String exchangeName = RandomStringUtils.randomAlphabetic(23);
		Mockito.doReturn(exchangeName).when(amqpConfig).getExchangeName();
		String routingKey = RandomStringUtils.randomAlphabetic(23);
		Mockito.doReturn(routingKey).when(amqpConfig).getRoutingKey();

		Mockito.doNothing().when(template).convertAndSend(ArgumentMatchers.anyString(), ArgumentMatchers.anyString(),
				any(Object.class));
		// Act
		block.accept(employee);

		// Assert
		verify(template).convertAndSend(ArgumentMatchers.eq(exchangeName), ArgumentMatchers.eq(routingKey),
				AssertionMatcher.assertArg((Object value) -> {
					Assertions.assertThat(value).isInstanceOf(EmployeeMessage.class);
					EmployeeMessage employeeMessage = (EmployeeMessage) value;
					Assertions.assertThat(employeeMessage.getEmployee()).isEqualTo(employee);
					Assertions.assertThat(employeeMessage.getEventType()).isEqualTo(expectedEventType);
				}));
	}

}
