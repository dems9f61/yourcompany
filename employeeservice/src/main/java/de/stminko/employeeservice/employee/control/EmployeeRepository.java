package de.stminko.employeeservice.employee.control;

import java.util.List;

import de.stminko.employeeservice.employee.entity.Employee;
import lombok.NonNull;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.history.RevisionRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeRepository
		extends JpaRepository<Employee, String>, RevisionRepository<Employee, String, Long> {

	List<Employee> findByEmailAddress(@NonNull String emailAddress);

	Page<Employee> findAllByDepartmentId(@NonNull @Param("departmentId") Long departmentId, @NonNull Pageable pageable);

}
