package de.stminko.employeeservice.employee.boundary;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import de.stminko.employeeservice.department.boundary.DepartmentService;
import de.stminko.employeeservice.department.entity.Department;
import de.stminko.employeeservice.employee.control.EmployeeEventPublisher;
import de.stminko.employeeservice.employee.control.EmployeeRepository;
import de.stminko.employeeservice.employee.entity.Employee;
import de.stminko.employeeservice.employee.entity.EmployeeRequest;
import de.stminko.employeeservice.runtime.errorhandling.boundary.BadRequestException;
import de.stminko.employeeservice.runtime.errorhandling.boundary.NotFoundException;
import de.stminko.employeeservice.runtime.rest.bondary.DataView;
import de.stminko.employeeservice.runtime.validation.constraints.boundary.MessageSourceHelper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.history.Revision;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service class for managing employee operations in the Employee Service application.
 * <p>
 * This class provides business logic for various operations related to employees, such as
 * creating, updating, finding, and deleting employee records. It interacts with
 * {@link EmployeeRepository} for data persistence and {@link DepartmentService} for
 * department-related operations. Additionally, it uses {@link EmployeeEventPublisher} for
 * publishing events related to employee actions.
 * </p>
 *
 * @author Stéphan Minko
 * @see EmployeeRepository for data persistence
 * @see DepartmentService for handling department-related operations
 * @see EmployeeEventPublisher for publishing employee-related events
 * @see Employee for the entity representing an employee
 * @see EmployeeRequest for the request object used in employee operations
 * @see Validator for validating request objects
 */
@Slf4j
@Transactional
@Service
public class EmployeeService {

	private final EmployeeRepository repository;

	private final DepartmentService departmentService;

	private final EmployeeEventPublisher messagePublisher;

	private final Validator validator;

	private final MessageSourceHelper messageSourceHelper;

	public EmployeeService(EmployeeRepository repository, @Lazy DepartmentService departmentService,
			EmployeeEventPublisher messagePublisher, Validator validator, MessageSourceHelper messageSourceHelper) {
		this.repository = repository;
		this.departmentService = departmentService;
		this.messagePublisher = messagePublisher;
		this.validator = validator;
		this.messageSourceHelper = messageSourceHelper;
	}

	/**
	 * Creates a new employee from the given request.
	 * <p>
	 * Validates the provided {@link EmployeeRequest}, checks for the uniqueness of the
	 * email address, and persists the new employee record. After successful creation, an
	 * event is published via {@link EmployeeEventPublisher}.
	 * </p>
	 * @param createRequest the request object containing details for the new employee
	 * @return the newly created {@link Employee}
	 */
	public Employee create(@NonNull EmployeeRequest createRequest) {
		log.info("create( createRequest= [{}] )", createRequest);
		validateRequest(createRequest, DataView.POST.class);

		String emailAddress = createRequest.emailAddress();
		validateUniquenessOfEmail(emailAddress);

		String departmentName = createRequest.departmentName();
		Department department = this.departmentService.findByDepartmentNameOrElseThrow(departmentName,
				BadRequestException.class);
		Employee newEmployee = new Employee();
		newEmployee.setEmailAddress(emailAddress);

		String firstName = createRequest.firstName();
		String lastName = createRequest.lastName();
		if (!StringUtils.isBlank(firstName) || StringUtils.isBlank(lastName)) {
			Employee.FullName fullName = new Employee.FullName();
			fullName.setFirstName(StringUtils.trim(firstName));

			fullName.setLastName(StringUtils.trim(lastName));
			newEmployee.setFullName(fullName);
		}

		newEmployee.setBirthday(createRequest.birthday());
		newEmployee.setDepartment(department);
		Employee savedEmployee = this.repository.save(newEmployee);
		this.messagePublisher.employeeCreated(savedEmployee);
		return savedEmployee;

	}

	/**
	 * Retrieves an employee by their unique identifier.
	 * <p>
	 * Searches for an employee using the provided ID. If the employee is not found, a
	 * {@link NotFoundException} is thrown.
	 * </p>
	 * @param id the unique identifier of the employee
	 * @return the found {@link Employee}
	 */
	@Transactional(propagation = Propagation.SUPPORTS)
	public Employee findById(@NonNull String id) {
		log.info("findById( id= [{}] )", id);
		return this.repository.findById(id)
			.orElseThrow(() -> new NotFoundException(
					this.messageSourceHelper.getMessage("errors.employee.id.not-found", id)));
	}

	/**
	 * Finds all employees.
	 * @param pageable the pageable object used for pagination
	 * @return a page of employees
	 */
	@Transactional(propagation = Propagation.SUPPORTS)
	public Page<Employee> findAll(Pageable pageable) {
		log.info("findAll()");
		return this.repository.findAll(pageable);
	}

	/**
	 * Fully updates an existing employee's data.
	 * <p>
	 * Updates the employee identified by the provided ID with the new data from the
	 * request object. Validates the request and applies the changes. An event is
	 * published if any change occurs.
	 * </p>
	 * @param id the unique identifier of the employee
	 * @param updateRequest the request object containing new details for the employee
	 * @return the updated {@link Employee}
	 */
	public Employee doFullUpdate(@NonNull String id, @NonNull EmployeeRequest updateRequest) {
		log.info("doFullUpdate ( [{}],[{}] ) ", id, updateRequest);
		return update(id, updateRequest, DataView.PUT.class);
	}

	/**
	 * Partially updates an existing employee's data.
	 * <p>
	 * Similar to {@link #doFullUpdate}, but only updates the fields provided in the
	 * update request. An event is published if any change occurs.
	 * </p>
	 * @param id the unique identifier of the employee
	 * @param updateRequest the request object containing fields to update
	 * @return the updated {@link Employee}
	 */
	public Employee doPartialUpdate(@NonNull String id, @NonNull EmployeeRequest updateRequest) {
		log.info("doFullUpdate ( [{}],[{}] ) ", id, updateRequest);
		return update(id, updateRequest, DataView.PATCH.class);
	}

	private Employee update(String id, EmployeeRequest updateRequest, Class<? extends DataView> validationGroup) {
		validateRequest(updateRequest, validationGroup);
		Employee employeeToUpdate = this.repository.findById(id)
			.orElseThrow(() -> new NotFoundException(
					this.messageSourceHelper.getMessage("errors.employee.id.not-found", id)));

		boolean hasChanged = hasEmailAddressChangedAfterUpdate(updateRequest, employeeToUpdate);
		hasChanged = hasFullNameChangedAfterUpdate(updateRequest, employeeToUpdate) || hasChanged;
		hasChanged = hasBirthDayChangedAfterUpdate(updateRequest, employeeToUpdate) || hasChanged;
		hasChanged = hasDepartmentChangedAfterUpdate(updateRequest, employeeToUpdate) || hasChanged;
		if (hasChanged) {
			Employee updatedEmployee = this.repository.save(employeeToUpdate);
			this.messagePublisher.employeeUpdated(updatedEmployee);
			return updatedEmployee;
		}
		return employeeToUpdate;
	}

	/**
	 * Deletes an employee by their unique identifier.
	 * <p>
	 * This method removes an employee record from the system using the provided ID. It
	 * first verifies that an employee with the given ID exists, throwing a
	 * {@link NotFoundException} if no such employee is found. After successful deletion,
	 * an event is published via {@link EmployeeEventPublisher} to indicate that an
	 * employee has been deleted.
	 * </p>
	 * @param id the unique identifier of the employee to be deleted
	 */
	public void deleteById(@NonNull String id) {
		log.info("deleteById( id= [{}] )", id);
		Employee employee = this.repository.findById(id)
			.orElseThrow(() -> new NotFoundException(
					this.messageSourceHelper.getMessage("errors.employee.id.not-found", id)));
		this.repository.deleteById(id);
		this.messagePublisher.employeeDeleted(employee);
	}

	public Page<Employee> findAllEmployeesByDepartmentId(@NonNull Long departmentId, @NonNull Pageable pageable) {
		log.info("findAllEmployeesByDepartmentId( departmentId= [{}] )", departmentId);
		return this.repository.findAllByDepartmentId(departmentId, pageable);
	}

	/**
	 * Find revisions of an employee by ID.
	 * @param id the ID of the employee.
	 * @param pageable the pagination information.
	 * @return a Page object containing the revisions of the employee.
	 */
	public Page<Revision<Long, Employee>> findRevisions(@NonNull String id, @NonNull Pageable pageable) {
		log.info("findRevisions( id= [{}] )", id);
		return this.repository.findRevisions(id, pageable);
	}

	/**
	 * Find the latest revision information for the given employee id.
	 * @param id the id of the entity the revision history should be fetched for
	 * @return a single {@link Revision} for the last data change on the Entity with given
	 * ID
	 * @throws NotFoundException if no revision information could be found (the employee
	 * for given ID does not exist)
	 */
	public Revision<Long, Employee> findLastChangeRevision(@NonNull String id) {
		log.info("findLastChangeRevision( id= [{}] )", id);
		return this.repository.findLastChangeRevision(id)
			.orElseThrow(() -> new NotFoundException(
					this.messageSourceHelper.getMessage("errors.employee.last-revision.not-found", id)));
	}

	private void validateUniquenessOfEmail(String emailAddress) {
		List<Employee> employeesWithSameEmail = StringUtils.isBlank(emailAddress) ? Collections.emptyList()
				: this.repository.findByEmailAddress(emailAddress);
		if (!employeesWithSameEmail.isEmpty()) {
			throw new BadRequestException(
					this.messageSourceHelper.getMessage("errors.employee.email.already-exists", emailAddress));
		}
	}

	private void validateRequest(EmployeeRequest employeeRequest, Class<? extends DataView> validationGroup) {
		Set<ConstraintViolation<EmployeeRequest>> cvs = this.validator.validate(employeeRequest, validationGroup);
		if (!cvs.isEmpty()) {
			throw new ConstraintViolationException(cvs);
		}
	}

	private boolean hasEmailAddressChangedAfterUpdate(EmployeeRequest employeeRequest, Employee employee) {
		boolean hasUpdated = false;
		String newEmailAddress = StringUtils.trim(employeeRequest.emailAddress());
		if (StringUtils.isNotBlank(newEmailAddress)
				&& !StringUtils.equals(employee.getEmailAddress(), newEmailAddress)) {
			employee.setEmailAddress(newEmailAddress);
			hasUpdated = true;
		}
		return hasUpdated;
	}

	private boolean hasFullNameChangedAfterUpdate(EmployeeRequest employeeRequest, Employee employee) {
		boolean hasUpdated = false;
		String newFirstName = StringUtils.trim(employeeRequest.firstName());
		String newLastName = StringUtils.trim(employeeRequest.lastName());
		Employee.FullName fullName = employee.getFullName();
		if (fullName != null) {
			if (StringUtils.isNotBlank(newFirstName) && !StringUtils.equals(fullName.getFirstName(), newFirstName)) {
				fullName.setFirstName(newFirstName);
				hasUpdated = true;
			}
			if (StringUtils.isNotBlank(newLastName) && !StringUtils.equals(fullName.getLastName(), newLastName)) {
				fullName.setLastName(newLastName);
				hasUpdated = true;
			}
		}
		else {
			if (StringUtils.isNotBlank(newFirstName) || StringUtils.isNotBlank(newLastName)) {
				fullName = new Employee.FullName();
				fullName.setFirstName(newFirstName);
				fullName.setLastName(newLastName);
				employee.setFullName(fullName);
				hasUpdated = true;
			}
		}

		return hasUpdated;
	}

	private boolean hasBirthDayChangedAfterUpdate(EmployeeRequest employeeRequest, Employee employee) {
		boolean hasUpdated = false;
		ZonedDateTime newBirthDay = employeeRequest.birthday();
		ZonedDateTime oldBirthDay = employee.getBirthday();
		if (Objects.nonNull(newBirthDay) && ObjectUtils.notEqual(oldBirthDay, newBirthDay)) {
			employee.setBirthday(newBirthDay);
			hasUpdated = true;
		}
		return hasUpdated;
	}

	private boolean hasDepartmentChangedAfterUpdate(EmployeeRequest updateParameter, Employee employee) {
		boolean hasUpdated = false;
		String newDepartmentName = StringUtils.trim(updateParameter.departmentName());
		if (StringUtils.isNotBlank(newDepartmentName)
				&& !StringUtils.equals(newDepartmentName, employee.getDepartment().getDepartmentName())) {
			Department department = this.departmentService.findByDepartmentNameOrElseThrow(newDepartmentName,
					BadRequestException.class);
			employee.setDepartment(department);
			hasUpdated = true;
		}
		return hasUpdated;
	}

}
