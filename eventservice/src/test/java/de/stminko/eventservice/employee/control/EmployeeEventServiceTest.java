package de.stminko.eventservice.employee.control;

import java.util.Date;
import java.util.UUID;

import de.stminko.eventservice.AbstractUnitTestSuite;
import de.stminko.eventservice.employee.entity.Employee;
import de.stminko.eventservice.employee.entity.EmployeeEvent;
import de.stminko.eventservice.employee.entity.PersistentEmployeeEvent;
import info.solidsoft.mockito.java8.AssertionMatcher;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@DisplayName("Unit tests for the employee event service")
class EmployeeEventServiceTest extends AbstractUnitTestSuite {

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
			EmployeeEvent employeeEvent = employeeEventTestFactory.createDefault();
			Mockito.doReturn(null).when(employeeEventRepository).save(ArgumentMatchers.any());

			// Act
			employeeEventService.handleEmployeeEvent(employeeEvent);

			// Assert
			verify(employeeEventRepository).save(AssertionMatcher.assertArg(persistentEmployeeEvent -> {
				Employee employee = employeeEvent.getEmployee();
				assertThat(persistentEmployeeEvent.getDepartmentName())
						.isEqualTo(employee.getDepartment().getDepartmentName());
				assertThat(persistentEmployeeEvent.getFirstName()).isEqualTo(employee.getFullName().getFirstName());
				assertThat(persistentEmployeeEvent.getLastName()).isEqualTo(employee.getFullName().getLastName());
				assertThat(persistentEmployeeEvent.getEmployeeId()).isEqualTo(employee.getId());
				assertThat(persistentEmployeeEvent.getEmailAddress()).isEqualTo(employee.getEmailAddress());
				assertThat(persistentEmployeeEvent.getEventType()).isEqualTo(employeeEvent.getEventType());
				assertThat(persistentEmployeeEvent.getBirthday())
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
			Pageable mockPageable = mock(Pageable.class);
			int expectedPageNumber = RandomUtils.nextInt(0, 23);
			Mockito.doReturn(expectedPageNumber).when(mockPageable).getPageNumber();

			Page<PersistentEmployeeEvent> mockPageableResult = (Page<PersistentEmployeeEvent>) mock(Page.class);
			Mockito.doReturn(mockPageableResult).when(employeeEventRepository).findByEmployeeId(eq(employeeId),
					ArgumentMatchers.any(Pageable.class));

			// Act
			employeeEventService.findByEmployeeIdOrderByCreatedAtAsc(employeeId, mockPageable);

			// Assert
			verify(employeeEventRepository).findByEmployeeId(ArgumentMatchers.eq(employeeId),
					AssertionMatcher.assertArg(pageable -> {
						assertThat(pageable.getPageNumber()).isEqualTo(expectedPageNumber);
						assertThat(pageable.getPageSize()).isEqualTo(EmployeeEventService.MAX_PAGE_SIZE);
						assertThat(pageable.getSort()).isEqualTo(EmployeeEventService.CREATED_AT_WITH_ASC_SORT);
					}));
		}

	}

}