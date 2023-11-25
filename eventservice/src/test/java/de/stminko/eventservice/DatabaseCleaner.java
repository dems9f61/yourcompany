package de.stminko.eventservice;

import de.stminko.eventservice.employee.control.EmployeeEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseCleaner {

	private final EmployeeEventRepository employeeEventRepository;

	public void cleanDatabases() {
		log.info("Cleaning up the test database");
		employeeEventRepository.deleteAll();
	}

}
