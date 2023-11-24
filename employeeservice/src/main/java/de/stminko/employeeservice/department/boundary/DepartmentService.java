package de.stminko.employeeservice.department.boundary;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Set;

import de.stminko.employeeservice.department.control.DepartmentRepository;
import de.stminko.employeeservice.department.entity.Department;
import de.stminko.employeeservice.department.entity.DepartmentRequest;
import de.stminko.employeeservice.runtime.errorhandling.boundary.BadRequestException;
import de.stminko.employeeservice.runtime.errorhandling.boundary.NotFoundException;
import de.stminko.employeeservice.runtime.rest.bondary.DataView;
import de.stminko.employeeservice.runtime.validation.constraints.boundary.MessageSourceHelper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service class for managing department-related operations.
 * <p>
 * This class provides services for various operations related to departments, such as
 * finding a department by its name, retrieving all departments, and creating a new department.
 * It uses {@link DepartmentRepository} for database interactions and employs
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

    private final Validator validator;

    private final MessageSourceHelper messageSourceHelper;


    /**
     * Retrieves a department by its name with a transactional context of SUPPORTS.
     * <p>
     * This method searches for a department based on the provided department name. It operates within
     * a transactional context defined by {@link org.springframework.transaction.annotation.Propagation#SUPPORTS}.
     * This means that the method will participate in a transaction if one already exists, but will not
     * start a new transaction if none exists.
     * </p>
     *
     * @param departmentName the name of the department to be retrieved. The name must not be {@code null}.
     * @return the {@link Department} object corresponding to the provided department name.
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
     * This method searches for a department based on the provided department name.
     * If the department is found, it is returned; otherwise, the method throws an exception of the
     * specified class. It operates within a transactional context defined by
     * {@link org.springframework.transaction.annotation.Propagation#SUPPORTS}.
     * </p>
     *
     * @param departmentName The name of the department to be retrieved. Must not be {@code null}.
     * @param exceptionClass The class of the exception to be thrown if the department is not found.
     *                       Must be a subclass of {@link RuntimeException} and not be {@code null}.
     * @return The {@link Department} object corresponding to the provided department name.
     * @throws RuntimeException Throws an exception of the specified class if the department is not found.
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
     * Retrieves all departments from the repository.
     * <p>
     * This method operates within a transactional context with the SUPPORTS propagation.
     * It means the method will participate in a transaction if one already exists,
     * but it does not require a transaction to be executed. If no transaction exists,
     * the method will still be executed, but without transactional support.
     * </p>
     *
     * @return A list of {@link Department} objects representing all departments in the repository.
     * The list may be empty if no departments are found.
     */
    @Transactional(propagation = Propagation.SUPPORTS)
    public List<Department> findAll() {
        log.info("findAll()");
        return this.repository.findAll();
    }

    /**
     * Creates a new department with the provided department request.
     * <p>
     * This method first validates the department request. If validation fails,
     * it throws a {@link ConstraintViolationException}. It then checks if a department
     * with the given name already exists. If it does, a {@link BadRequestException} is thrown.
     * Otherwise, a new department is created and saved in the repository.
     * </p>
     *
     * @param departmentRequest The request object containing the department details.
     * @return The created {@link Department} object.
     * @throws ConstraintViolationException if the validation of the department request fails.
     * @throws BadRequestException          if a department with the given name already exists.
     */
    public Department create(@NonNull DepartmentRequest departmentRequest) {
        log.info("create( [{}] )", departmentRequest);
        Set<ConstraintViolation<DepartmentRequest>> constraintViolations = this.validator.validate(departmentRequest,
                DataView.POST.class);
        if (!constraintViolations.isEmpty()) {
            throw new ConstraintViolationException(constraintViolations);
        }

        String departmentName = departmentRequest.departmentName();
        this.repository.findByDepartmentName(departmentName)
                .ifPresent(dept -> {
                    throw new BadRequestException(messageSourceHelper.getMessage("errors.department.name.already-exists", departmentName));
                });
        Department department = new Department();
        department.setDepartmentName(departmentName);
        return this.repository.save(department);
    }

    private Department findDepartmentOrThrow(String departmentName, Class<? extends RuntimeException> exceptionClass) {

        return this.repository.findByDepartmentName(departmentName)
                .orElseThrow(() -> createException(exceptionClass,
                        messageSourceHelper.getMessage("errors.department-not-found", departmentName)));
    }

    private <E extends RuntimeException> E createException(Class<E> exceptionClass, String errorMessage) {
        try {
            return exceptionClass.getDeclaredConstructor(String.class).newInstance(errorMessage);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException exception) {
            throw new RuntimeException("Error instantiating exception: " + exception.getMessage(), exception);
        }
    }

}
