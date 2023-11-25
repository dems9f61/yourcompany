package de.stminko.eventservice.employee.control;

import java.util.Date;

import de.stminko.eventservice.employee.entity.Employee;
import de.stminko.eventservice.employee.entity.EmployeeEvent;
import de.stminko.eventservice.employee.entity.PersistentEmployeeEvent;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

/**
 * Service class for handling employee events.
 *
 * <p>
 * This class provides functionality to handle and process employee-related events, as
 * well as querying persisted employee event data.
 * </p>
 *
 * @author Stéphan Minko
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class EmployeeEventService {

	/**
	 * Sort specification for ordering by creation time in ascending order.
	 */
	static final Sort CREATED_AT_WITH_ASC_SORT = Sort.by(Sort.Direction.ASC, "createdAt");

	/**
	 * Maximum allowed page size for querying employee events.
	 */
	static final int MAX_PAGE_SIZE = 200;

	private final EmployeeEventRepository employeeEventRepository;

	/**
	 * Handles the given employee event by persisting it into the repository.
	 *
	 * <p>
	 * This method listens to {@link EmployeeEvent} and converts them to
	 * {@link PersistentEmployeeEvent}, then saves them using the
	 * {@link EmployeeEventRepository}. It logs the received event and handles the data
	 * transformation and persistence.
	 * </p>
	 * @param employeeEvent the employee event to handle
	 */
	@EventListener
	public void handleEmployeeEvent(@NonNull EmployeeEvent employeeEvent) {
		log.info("handleEmployeeEvent(employeeEvent= [{}])", employeeEvent);
		PersistentEmployeeEvent persistentEmployeeEvent = new PersistentEmployeeEvent();
		Employee employee = employeeEvent.getEmployee();
		persistentEmployeeEvent.setEventType(employeeEvent.getEventType());
		persistentEmployeeEvent.setBirthday(Date.from(employee.getBirthday().toInstant()));
		persistentEmployeeEvent.setEmailAddress(employee.getEmailAddress());
		Employee.FullName fullName = employee.getFullName();
		if (fullName != null) {
			persistentEmployeeEvent.setFirstName(fullName.getFirstName());
			persistentEmployeeEvent.setLastName(fullName.getLastName());
		}
		persistentEmployeeEvent.setEmployeeId(employee.getId());
		persistentEmployeeEvent.setDepartmentName(employee.getDepartment().getDepartmentName());
		this.employeeEventRepository.save(persistentEmployeeEvent);
	}

	/**
	 * Finds employee events by employee ID, ordered by creation time in ascending order.
	 *
	 * <p>
	 * This method retrieves a paginated list of {@link PersistentEmployeeEvent} for a
	 * given employee ID. The results are ordered by the event creation time in ascending
	 * order and are paginated according to the provided {@link Pageable} object. The page
	 * size is limited to a maximum of {@code MAX_PAGE_SIZE}.
	 * </p>
	 * @param employeeId the unique identifier of the employee whose events are to be
	 * retrieved
	 * @param pageable the pagination information
	 * @return a paginated list of {@link PersistentEmployeeEvent}
	 */
	public Page<PersistentEmployeeEvent> findByEmployeeIdOrderByCreatedAtAsc(@NonNull String employeeId,
			@NonNull Pageable pageable) {
		log.info("findByEmployeeIdOrderByCreatedAtAsc(employeeId= [{}], pageable= [{}])", employeeId, pageable);
		PageRequest createdAtPageRequest = PageRequest.of(pageable.getPageNumber(), MAX_PAGE_SIZE,
				CREATED_AT_WITH_ASC_SORT);
		return this.employeeEventRepository.findByEmployeeId(employeeId, createdAtPageRequest);
	}

}
