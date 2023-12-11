package de.stminko.employeeservice.department.control;

import java.util.Optional;

import de.stminko.employeeservice.department.entity.Department;
import lombok.NonNull;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.history.RevisionRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DepartmentRepository
		extends JpaRepository<Department, Long>, RevisionRepository<Department, Long, Long> {

	Optional<Department> findByDepartmentName(String departmentName);

	@Query("SELECT d FROM Department d LEFT JOIN FETCH d.employees WHERE d.id = :id")
	Optional<Department> findDepartmentWithEmployees(@Param("id") @NonNull Long id);

	boolean existsById(@Param("id") @NonNull Long id);

}
