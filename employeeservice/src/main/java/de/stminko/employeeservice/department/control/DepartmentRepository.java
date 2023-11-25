package de.stminko.employeeservice.department.control;

import java.util.Optional;

import de.stminko.employeeservice.department.entity.Department;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.history.RevisionRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DepartmentRepository
		extends JpaRepository<Department, Long>, RevisionRepository<Department, Long, Long> {

	Optional<Department> findByDepartmentName(String departmentName);

}
