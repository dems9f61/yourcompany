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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import static org.assertj.core.api.Assertions.assertThat;


@DisplayName("Integration tests for employee event service")
class EmployeeEventServiceIntegrationTest extends AbstractIntegrationTestSuite {
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
        Page<PersistentEmployeeEvent> allDescOrderedByCreatedAt
                = employeeEventService.findByEmployeeIdOrderByCreatedAtAsc(employeeId, pageRequest);

        // Assert
        List<PersistentEmployeeEvent> sortedPersistentEmployeeEvents = allDescOrderedByCreatedAt.stream().toList();
        IntStream.range(0, sortedPersistentEmployeeEvents.size() - 1)
                .forEach(i -> {
                    Instant current = sortedPersistentEmployeeEvents.get(i).getCreatedAt();
                    Instant next = sortedPersistentEmployeeEvents.get(i + 1).getCreatedAt();
                    assertThat(current).isBefore(next);
                });
    }

    @DisplayName("Finding published employee events returns an empty collection For unknown id ")
    @Test
    void givenUnknownUuid_whenFindAll_thenReturnEmptyList() {
        // Arrange
        int eventCount = RandomUtils.nextInt(10, 20);
        receiveRandomMessageFor(eventCount);
        String unknownEmployeeId = UUID.randomUUID().toString();
        PageRequest pageRequest = PageRequest.of(0, eventCount);

        // Act
        Page<PersistentEmployeeEvent> events = employeeEventService.findByEmployeeIdOrderByCreatedAtAsc(unknownEmployeeId, pageRequest);

        // Assert
        assertThat(events).isNotNull().isEmpty();
    }

    @DisplayName("Handling an employee events makes it persistent")
    @Test
    void givenEmployeeEvent_whenHandle_thenPersistEvent() {
        // Arrange
        employeeEventRepository.deleteAll();
        Employee employee = employeeTestFactory.createDefault();
        EmployeeEvent employeeEvent = employeeEventTestFactory.builder().employee(employee).create();

        // Act
        employeeEventService.handleEmployeeEvent(employeeEvent);

        // Assert
        List<PersistentEmployeeEvent> allEvents = employeeEventRepository.findAll();
        assertThat(allEvents).isNotEmpty().hasSize(1);
        PersistentEmployeeEvent persistentEmployeeEvent = allEvents.get(0);
        assertThat(persistentEmployeeEvent.getId()).isNotNull();
        assertThat(persistentEmployeeEvent.getCreatedAt()).isNotNull().isBefore(Instant.now());
        assertThat(persistentEmployeeEvent.getBirthday()).isEqualTo(Date.from(employee.getBirthday().toInstant()));
        assertThat(persistentEmployeeEvent.getDepartmentName()).isEqualTo(employee.getDepartment().getDepartmentName());
        assertThat(persistentEmployeeEvent.getEmailAddress()).isEqualTo(employee.getEmailAddress());
        assertThat(persistentEmployeeEvent.getEventType()).isNotNull().isEqualTo(employeeEvent.getEventType());
        assertThat(persistentEmployeeEvent.getEmployeeId()).isEqualTo(employee.getId());
        assertThat(persistentEmployeeEvent.getFirstName()).isEqualTo(employee.getFullName().getFirstName());
        assertThat(persistentEmployeeEvent.getLastName()).isEqualTo(employee.getFullName().getLastName());
    }
}
