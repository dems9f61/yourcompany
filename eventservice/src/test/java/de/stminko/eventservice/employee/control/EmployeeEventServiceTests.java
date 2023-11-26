package de.stminko.eventservice.employee.control;

import java.util.Date;
import java.util.UUID;

import de.stminko.eventservice.AbstractUnitTestSuite;
import de.stminko.eventservice.employee.entity.Employee;
import de.stminko.eventservice.employee.entity.EmployeeEvent;
import de.stminko.eventservice.employee.entity.PersistentEmployeeEvent;
import info.solidsoft.mockito.java8.AssertionMatcher;
import org.apache.commons.lang3.RandomUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@DisplayName("Unit tests for the employee event service")
class EmployeeEventServiceTests extends AbstractUnitTestSuite {

	@Mock
	private EmployeeEventRepository employeeEventRepository;

	@InjectMocks
	private EmployeeEventService employeeEventService;

	@Nested
	@DisplayName("when Handle event")
	class WhenHandleEvent {

		@Test
		@DisplayName("Handle an employee events persists that event")
		void givenEmployeeVent_whenHandle_thenPersist() {
			// Arrange
			EmployeeEvent employeeEvent = EmployeeEventServiceTests.this.employeeEventTestFactory.createDefault();
			Mockito.doReturn(null)
				.when(EmployeeEventServiceTests.this.employeeEventRepository)
				.save(ArgumentMatchers.any());

			// Act
			EmployeeEventServiceTests.this.employeeEventService.handleEmployeeEvent(employeeEvent);

			// Assert
			Mockito.verify(EmployeeEventServiceTests.this.employeeEventRepository)
				.save(AssertionMatcher.assertArg((PersistentEmployeeEvent persistentEmployeeEvent) -> {
					Employee employee = employeeEvent.getEmployee();
					Assertions.assertThat(persistentEmployeeEvent.getDepartmentName())
						.isEqualTo(employee.getDepartment().getDepartmentName());
					Assertions.assertThat(persistentEmployeeEvent.getFirstName())
						.isEqualTo(employee.getFullName().getFirstName());
					Assertions.assertThat(persistentEmployeeEvent.getLastName())
						.isEqualTo(employee.getFullName().getLastName());
					Assertions.assertThat(persistentEmployeeEvent.getEmployeeId()).isEqualTo(employee.getId());
					Assertions.assertThat(persistentEmployeeEvent.getEmailAddress())
						.isEqualTo(employee.getEmailAddress());
					Assertions.assertThat(persistentEmployeeEvent.getEventType())
						.isEqualTo(employeeEvent.getEventType());
					Assertions.assertThat(persistentEmployeeEvent.getBirthday())
						.isEqualTo(Date.from(employee.getBirthday().toInstant()));
				}));
		}

	}

	@Nested
	@DisplayName("When access")
	class WhenAccess {

		@Test
		@DisplayName("Finding all employee events invokes the underlying repository")
		void givenEmployeeVents_whenFindByUuid_thenInvokeRelyOnRepository() {
			// Arrange
			String employeeId = UUID.randomUUID().toString();
			Pageable mockPageable = Mockito.mock(Pageable.class);
			int expectedPageNumber = RandomUtils.nextInt(0, 23);
			Mockito.doReturn(expectedPageNumber).when(mockPageable).getPageNumber();

			Page<PersistentEmployeeEvent> mockPageableResult = (Page<PersistentEmployeeEvent>) Mockito.mock(Page.class);
			Mockito.doReturn(mockPageableResult)
				.when(EmployeeEventServiceTests.this.employeeEventRepository)
				.findByEmployeeId(ArgumentMatchers.eq(employeeId), ArgumentMatchers.any(Pageable.class));

			// Act
			EmployeeEventServiceTests.this.employeeEventService.findByEmployeeIdOrderByCreatedAtAsc(employeeId,
					mockPageable);

			// Assert
			Mockito.verify(EmployeeEventServiceTests.this.employeeEventRepository)
				.findByEmployeeId(ArgumentMatchers.eq(employeeId), AssertionMatcher.assertArg((Pageable pageable) -> {
					Assertions.assertThat(pageable.getPageNumber()).isEqualTo(expectedPageNumber);
					Assertions.assertThat(pageable.getPageSize()).isEqualTo(EmployeeEventService.MAX_PAGE_SIZE);
					Assertions.assertThat(pageable.getSort()).isEqualTo(EmployeeEventService.CREATED_AT_WITH_ASC_SORT);
				}));
		}

	}

}
