package de.stminko.employeeservice.employee.control;

import java.util.List;
import java.util.UUID;

import de.stminko.employeeservice.employee.entity.Employee;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.history.RevisionRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, String>, RevisionRepository<Employee, String, Long> {

	List<Employee> findByEmailAddress(String emailAddress);

}
