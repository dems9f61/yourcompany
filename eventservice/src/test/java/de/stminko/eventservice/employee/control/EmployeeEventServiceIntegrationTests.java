package de.stminko.eventservice.employee.control;

import java.sql.Date;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import de.stminko.eventservice.AbstractIntegrationTestSuite;
import de.stminko.eventservice.employee.entity.Employee;
import de.stminko.eventservice.employee.entity.EmployeeEvent;
import de.stminko.eventservice.employee.entity.PersistentEmployeeEvent;
import org.apache.commons.lang3.RandomUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@DisplayName("Integration tests for employee event service")
class EmployeeEventServiceIntegrationTests extends AbstractIntegrationTestSuite {

	@Autowired
	private EmployeeEventRepository employeeEventRepository;

	@Autowired
	private EmployeeEventService employeeEventService;

	@DisplayName("All published employee events to a specific id appear in ascending order")
	@Test
	void givenPublishedEmployeeEventsForAnyEmployee_whenFindAll_thenReturnDescendingOrderedList() {
		// Arrange
		String employeeId = UUID.randomUUID().toString();
		int eventCount = RandomUtils.nextInt(50, 60);
		receiveRandomMessageFor(employeeId, eventCount);
		PageRequest pageRequest = PageRequest.of(0, 10);

		// Act
		Page<PersistentEmployeeEvent> allDescOrderedByCreatedAt = this.employeeEventService
			.findByEmployeeIdOrderByCreatedAtAsc(employeeId, pageRequest);

		// Assert
		List<PersistentEmployeeEvent> sortedPersistentEmployeeEvents = allDescOrderedByCreatedAt.stream().toList();
		IntStream.range(0, sortedPersistentEmployeeEvents.size() - 1).forEach((int i) -> {
			Instant current = sortedPersistentEmployeeEvents.get(i).getCreatedAt();
			Instant next = sortedPersistentEmployeeEvents.get(i + 1).getCreatedAt();
			Assertions.assertThat(current).isBefore(next);
		});
	}

	@DisplayName("Finding published employee events returns an empty collection For unknown id ")
	@Test
	void givenUnknownUuid_whenFindAll_thenReturnEmptyList() {
		// Arrange
		String unknownEmployeeId = UUID.randomUUID().toString();
		int eventCount = RandomUtils.nextInt(10, 20);
		receiveRandomMessageFor(eventCount);
		PageRequest pageRequest = PageRequest.of(0, eventCount);

		// Act
		Page<PersistentEmployeeEvent> events = this.employeeEventService
			.findByEmployeeIdOrderByCreatedAtAsc(unknownEmployeeId, pageRequest);

		// Assert
		Assertions.assertThat(events).isNotNull().isEmpty();
	}

	@DisplayName("Handling an employee events makes it persistent")
	@Test
	void givenEmployeeEvent_whenHandle_thenPersistEvent() {
		// Arrange
		this.employeeEventRepository.deleteAll();
		Employee employee = this.employeeTestFactory.createDefault();
		EmployeeEvent employeeEvent = this.employeeEventTestFactory.builder().employee(employee).create();

		// Act
		this.employeeEventService.handleEmployeeEvent(employeeEvent);

		// Assert
		List<PersistentEmployeeEvent> allEvents = this.employeeEventRepository.findAll();
		Assertions.assertThat(allEvents).isNotEmpty().hasSize(1);
		PersistentEmployeeEvent persistentEmployeeEvent = allEvents.get(0);
		Assertions.assertThat(persistentEmployeeEvent.getId()).isNotNull();
		Assertions.assertThat(persistentEmployeeEvent.getCreatedAt()).isNotNull().isBefore(Instant.now());
		Assertions.assertThat(persistentEmployeeEvent.getBirthday())
			.isEqualTo(Date.from(employee.getBirthday().toInstant()));
		Assertions.assertThat(persistentEmployeeEvent.getDepartmentName())
			.isEqualTo(employee.getDepartment().getDepartmentName());
		Assertions.assertThat(persistentEmployeeEvent.getEmailAddress()).isEqualTo(employee.getEmailAddress());
		Assertions.assertThat(persistentEmployeeEvent.getEventType())
			.isNotNull()
			.isEqualTo(employeeEvent.getEventType());
		Assertions.assertThat(persistentEmployeeEvent.getEmployeeId()).isEqualTo(employee.getId());
		Assertions.assertThat(persistentEmployeeEvent.getFirstName()).isEqualTo(employee.getFullName().getFirstName());
		Assertions.assertThat(persistentEmployeeEvent.getLastName()).isEqualTo(employee.getFullName().getLastName());
	}

}
