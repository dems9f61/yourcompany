package de.stminko.employeeservice;

import de.stminko.employeeservice.department.control.DepartmentRepository;
import de.stminko.employeeservice.employee.control.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseCleaner {

	private final DepartmentRepository departmentRepository;

	private final EmployeeRepository employeeRepository;

	public void cleanDatabases() {
		log.info("Cleaning up the test database");
		this.employeeRepository.deleteAll();
		this.departmentRepository.deleteAll();
	}

}
