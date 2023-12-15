package de.stminko.employeeservice.department.control;

import java.lang.reflect.InvocationTargetException;
import java.util.Objects;
import java.util.Set;

import de.stminko.employeeservice.department.boundary.dto.DepartmentRequest;
import de.stminko.employeeservice.department.entity.Department;
import de.stminko.employeeservice.employee.control.EmployeeService;
import de.stminko.employeeservice.employee.entity.Employee;
import de.stminko.employeeservice.runtime.errorhandling.boundary.BadRequestException;
import de.stminko.employeeservice.runtime.errorhandling.boundary.DepartmentNotEmptyException;
import de.stminko.employeeservice.runtime.errorhandling.boundary.NotFoundException;
import de.stminko.employeeservice.runtime.persistence.boundary.BeanTool;
import de.stminko.employeeservice.runtime.rest.bondary.DataView;
import de.stminko.employeeservice.runtime.validation.constraints.boundary.MessageSourceHelper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.history.Revision;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service class for managing department-related operations.
 * <p>
 * This class provides services for various operations related to departments, such as
 * finding a department by its name, retrieving all departments, and creating a new
 * department. It uses {@link DepartmentRepository} for database interactions and employs
 * Spring's transactional management to ensure data consistency and integrity.
 * </p>
 *
 * @author Stéphan Minko
 * @see Department
 * @see DepartmentRepository
 */
@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class DepartmentService {

	private final DepartmentRepository repository;

	private final EmployeeService employeeService;

	private final Validator validator;

	private final MessageSourceHelper messageSourceHelper;

	/**
	 * Retrieves an department by their unique identifier.
	 * <p>
	 * Searches for an department using the provided ID. If the department is not found, a
	 * {@link NotFoundException} is thrown.
	 * </p>
	 * @param departmentId the unique identifier of the employee
	 * @return the found {@link Department}
	 */
	@Transactional(propagation = Propagation.SUPPORTS)
	public Department findById(@NonNull Long departmentId) {
		log.info("findById( departmentId= [{}] )", departmentId);
		return this.repository.findById(departmentId)
			.orElseThrow(() -> new NotFoundException(
					this.messageSourceHelper.getMessage("errors.department.id.not-found", departmentId.toString())));
	}

	/**
	 * Retrieves a department by its name with a transactional context of SUPPORTS.
	 * <p>
	 * This method searches for a department based on the provided department name. It
	 * operates within a transactional context defined by
	 * {@link org.springframework.transaction.annotation.Propagation#SUPPORTS}. This means
	 * that the method will participate in a transaction if one already exists, but will
	 * not start a new transaction if none exists.
	 * </p>
	 * @param departmentName the name of the department to be retrieved. The name must not
	 * be {@code null}.
	 * @return the {@link Department} object corresponding to the provided department
	 * name.
	 * @throws NotFoundException if no department with the given name is found.
	 * @see #findDepartmentOrThrow
	 */
	@Transactional(propagation = Propagation.SUPPORTS)
	public Department findByDepartmentName(@NonNull String departmentName) {
		log.info("findByDepartmentName( departmentName=[{}] )", departmentName);
		return findDepartmentOrThrow(departmentName, NotFoundException.class);
	}

	/**
	 * Retrieves a department by its name or throws a specified exception if not found.
	 * <p>
	 * This method searches for a department based on the provided department name. If the
	 * department is found, it is returned; otherwise, the method throws an exception of
	 * the specified class. It operates within a transactional context defined by
	 * {@link org.springframework.transaction.annotation.Propagation#SUPPORTS}.
	 * </p>
	 * @param departmentName the name of the department to be retrieved. Must not be
	 * {@code null}.
	 * @param exceptionClass the class of the exception to be thrown if the department is
	 * not found. Must be a subclass of {@link RuntimeException} and not be {@code null}.
	 * @return the {@link Department} object corresponding to the provided department
	 * name.
	 * @throws RuntimeException throws an exception of the specified class if the
	 * department is not found.
	 * @see #findDepartmentOrThrow
	 */
	@Transactional(propagation = Propagation.SUPPORTS)
	public Department findByDepartmentNameOrElseThrow(@NonNull String departmentName,
			@NonNull Class<? extends RuntimeException> exceptionClass) {
		log.info("findByDepartmentNameOrElseThrow( departmentName= [{}], exceptionClass= [{}] )", departmentName,
				exceptionClass.getSimpleName());
		return findDepartmentOrThrow(departmentName, exceptionClass);
	}

	/**
	 * Retrieves a paginated list of all departments.
	 * <p>
	 * This method returns a {@link Page} of {@link Department} objects, each representing
	 * a department. The result is paginated according to the provided {@link Pageable}
	 * object, which specifies the page number and size, along with sorting parameters.
	 * @param pageable a {@link Pageable} object to specify the pagination and sorting
	 * information.
	 * @return a {@link Page} of {@link Department} objects containing the paginated
	 * department data.
	 */
	@Transactional(propagation = Propagation.SUPPORTS)
	public Page<Department> findAll(@NonNull Pageable pageable) {
		log.info("findAll()");
		return this.repository.findAll(pageable);
	}

	/**
	 * Finds the revision information of a department with the specified departmentId.
	 * @param departmentId the departmentId of the department to find revisions for (must
	 * not be null)
	 * @param pageable the pagination information for the result (must not be null)
	 * @return a Page object containing the revisions of the department
	 */
	public Page<Revision<Long, Department>> findRevisions(@NonNull Long departmentId, @NonNull Pageable pageable) {
		log.info("findRevisions( departmentId= [{}] )", departmentId);
		return this.repository.findRevisions(departmentId, pageable);
	}

	/**
	 * Find the latest revision information for the given department departmentId.
	 * @param departmentId the departmentId of the entity the revision history should be
	 * fetched for
	 * @return a single {@link Revision} for the last data change on the Entity with given
	 * Id
	 * @throws NotFoundException if no revision information could be found (the department
	 * for given Id does not exist)
	 */
	public Revision<Long, Department> findLastChangeRevision(@NonNull Long departmentId) {
		log.info("findLastChangeRevision( departmentId= [{}] )", departmentId);
		return this.repository.findLastChangeRevision(departmentId)
			.orElseThrow(() -> new NotFoundException(this.messageSourceHelper
				.getMessage("errors.department.last-revision.not-found", departmentId.toString())));
	}

	/**
	 * Creates a new department with the provided department request.
	 * <p>
	 * This method first validates the department request. If validation fails, it throws
	 * a {@link ConstraintViolationException}. It then checks if a department with the
	 * given name already exists. If it does, a {@link BadRequestException} is thrown.
	 * Otherwise, a new department is created and saved in the repository.
	 * </p>
	 * @param departmentRequest the request object containing the department details.
	 * @return the created {@link Department} object.
	 * @throws ConstraintViolationException if the validation of the department request
	 * fails.
	 * @throws BadRequestException if a department with the given name already exists.
	 */
	public Department create(@NonNull DepartmentRequest departmentRequest) {
		log.info("create( departmentRequest= [{}] )", departmentRequest);
		validateRequest(departmentRequest, DataView.POST.class);

		String departmentName = departmentRequest.departmentName();
		if (this.repository.existsByDepartmentName(departmentName)) {
			throw new BadRequestException(
					this.messageSourceHelper.getMessage("errors.department.name.already-exists", departmentName));
		}

		Department department = new Department();
		department.setDepartmentName(departmentName);
		return this.repository.save(department);
	}

	/**
	 * Fully updates an existing department's data.
	 * <p>
	 * Updates the department identified by the provided ID with the new data from the
	 * request object. Validates the request and applies the changes.
	 * </p>
	 * @param departmentId the unique identifier of the department
	 * @param departmentRequest the request object containing new details for the
	 * department
	 * @return the updated {@link Department}
	 */
	public Department doFullUpdate(Long departmentId, DepartmentRequest departmentRequest) {
		log.info("doFullUpdate ( departmentId= [{}], departmentRequest= [{}] ) ", departmentId, departmentRequest);
		return update(departmentId, departmentRequest, DataView.PUT.class);
	}

	/**
	 * Partially updates an existing department's data.
	 * <p>
	 * Updates the department identified by the provided ID with the new data from the
	 * request object. Validates the request and applies the changes.
	 * </p>
	 * @param departmentId the unique identifier of the department
	 * @param departmentRequest the request object containing new details for the
	 * department
	 * @return the updated {@link Department}
	 */
	public Department doPartialUpdate(Long departmentId, DepartmentRequest departmentRequest) {
		log.info("doFullUpdate ( departmentId= [{}], departmentRequest= [{}] ) ", departmentId, departmentRequest);
		return update(departmentId, departmentRequest, DataView.PATCH.class);
	}

	/**
	 * deletes a department by its unique identifier.
	 * <p>
	 * Retrieves the department with the provided ID from the repository. If the
	 * department does not exist, a {@link NotFoundException} is thrown.
	 * </p>
	 * <p>
	 * Checks if the department has any employees associated with it. If there are any
	 * employees, a {@link DepartmentNotEmptyException} is thrown, indicating that the
	 * department cannot be deleted until all employees are removed.
	 * </p>
	 * <p>
	 * Finally, deletes the department from the repository.
	 * </p>
	 * @param departmentId the unique identifier of the department to be deleted
	 * @throws NotFoundException if the department with the provided ID does not exist
	 * @throws DepartmentNotEmptyException if the department has employees associated with
	 * it
	 */
	public void deleteById(@NonNull Long departmentId) {
		log.info("deleteById( departmentId= [{}] )", departmentId);
		Department department = this.repository.findDepartmentWithEmployees(departmentId)
			.orElseThrow(() -> new NotFoundException(
					this.messageSourceHelper.getMessage("errors.department.id.not-found", departmentId.toString())));
		Set<Employee> employees = department.getEmployees();
		if (CollectionUtils.isNotEmpty(employees)) {
			throw new DepartmentNotEmptyException(this.messageSourceHelper
				.getMessage("errors.department.not-deletable-on-employee", departmentId.toString()));
		}
		this.repository.deleteById(departmentId);
	}

	/**
	 * retrieves all employees associated with a department identified by the provided ID.
	 * @param departmentId the unique identifier of the department
	 * @param pageable a {@link Pageable} object to specify pagination information.
	 * @return a set of employees associated with the department
	 * @throws NotFoundException if the department with the provided ID does not exist
	 */
	public Page<Employee> findAllEmployeesById(@NonNull Long departmentId, @NonNull Pageable pageable) {
		log.info("findAllEmployeesById( departmentId= [{}] )", departmentId);
		if (!this.repository.existsById(departmentId)) {
			throw new NotFoundException(
					this.messageSourceHelper.getMessage("errors.department.id.not-found", departmentId.toString()));
		}
		return this.employeeService.findAllEmployeesByDepartmentId(departmentId, pageable);
	}

	private Department update(Long departmentId, DepartmentRequest departmentRequest,
			Class<? extends DataView> validationGroup) {
		validateRequest(departmentRequest, validationGroup);
		Department departmentToUpdate = this.repository.findById(departmentId)
			.orElseThrow(() -> new NotFoundException(
					this.messageSourceHelper.getMessage("errors.department.id.not-found", departmentId.toString())));

		String departmentName = departmentRequest.departmentName();
		this.repository.findByDepartmentName(departmentName).ifPresent((Department department) -> {
			if (!Objects.equals(departmentId, department.getId())) {
				throw new BadRequestException(
						this.messageSourceHelper.getMessage("errors.department.name.already-exists", departmentName));
			}
		});
		BeanTool.copyNonNullProperties(departmentRequest, departmentToUpdate);
		return this.repository.save(departmentToUpdate);
	}

	private void validateRequest(DepartmentRequest departmentRequest, Class<? extends DataView> validationGroup) {
		Set<ConstraintViolation<DepartmentRequest>> constraintViolations = this.validator.validate(departmentRequest,
				validationGroup);
		if (!constraintViolations.isEmpty()) {
			throw new ConstraintViolationException(constraintViolations);
		}
	}

	private Department findDepartmentOrThrow(String departmentName, Class<? extends RuntimeException> exceptionClass) {

		return this.repository.findByDepartmentName(departmentName)
			.orElseThrow(() -> createException(exceptionClass,
					this.messageSourceHelper.getMessage("errors.department.name.not-found", departmentName)));
	}

	private <E extends RuntimeException> E createException(Class<E> exceptionClass, String errorMessage) {
		try {
			return exceptionClass.getDeclaredConstructor(String.class).newInstance(errorMessage);
		}
		catch (InstantiationException | IllegalAccessException | InvocationTargetException
				| NoSuchMethodException exception) {
			throw new RuntimeException("Error instantiating exception: " + exception.getMessage(), exception);
		}
	}

}
