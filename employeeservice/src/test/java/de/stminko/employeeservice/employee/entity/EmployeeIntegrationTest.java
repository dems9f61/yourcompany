package de.stminko.employeeservice.employee.entity;

import java.util.List;
import java.util.stream.IntStream;

import de.stminko.employeeservice.AbstractIntegrationTestSuite;
import de.stminko.employeeservice.department.control.DepartmentRepository;
import de.stminko.employeeservice.department.entity.Department;
import de.stminko.employeeservice.employee.control.EmployeeRepository;
import org.apache.commons.lang3.RandomUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;

class EmployeeIntegrationTest extends AbstractIntegrationTestSuite {

	@Autowired
	private EmployeeRepository employeeRepository;

	@Autowired
	private DepartmentRepository departmentRepository;

	@Test
	@DisplayName("two employees with the same id and values are equal")
	void givenTwoEmployeesWithSameIdAndValues_whenCompare_thenEqual() {
		// Arrange
		Department toPersistDepartment = this.departmentTestFactory.builder().id(null).create();
		Department persistedDepartment = this.departmentRepository.save(toPersistDepartment);

		Employee toPersistEmployee = this.employeeTestFactory.builder().department(persistedDepartment).create();
		Employee persistedEmployee = this.employeeRepository.save(toPersistEmployee);
		Employee found = this.employeeRepository.findById(persistedEmployee.getId()).orElseThrow();

		// Act / Assert
		Assertions.assertThat(found).isEqualTo(persistedEmployee);
	}

	@Test
	@DisplayName("two different employees are not equal")
	void givenDifferentEmployees_whenCompare_thenNotEqual() {
		// Arrange
		Department toPersistDepartment = this.departmentTestFactory.builder().id(null).create();
		Department persistedDepartment = this.departmentRepository.save(toPersistDepartment);

		Employee persistedEmployee = this.employeeRepository
				.save(this.employeeTestFactory.builder().department(persistedDepartment).create());
		Employee otherPersistedEmployee = this.employeeRepository
				.save(this.employeeTestFactory.builder().department(persistedDepartment).create());
		Employee found = this.employeeRepository.findById(persistedEmployee.getId()).orElseThrow();
		Employee otherFound = this.employeeRepository.findById(otherPersistedEmployee.getId()).orElseThrow();

		// Act / Assert
		Assertions.assertThat(found).isNotEqualTo(otherFound);
	}

	@Test
	@DisplayName("two employees with the same id and values are mapped to the same hash code")
	void givenTwoEmployeesWithSameIdAndValues_whenHash_thenSameHashCode() {
		// Arrange
		Department toPersistDepartment = this.departmentTestFactory.builder().id(null).create();
		Department persistedDepartment = this.departmentRepository.save(toPersistDepartment);

		Employee toPersistEmployee = this.employeeTestFactory.builder().department(persistedDepartment).create();
		Employee persistedEmployee = this.employeeRepository.save(toPersistEmployee);
		Employee found = this.employeeRepository.findById(persistedEmployee.getId()).orElseThrow();

		// Act / Assert
		Assertions.assertThat(found.hashCode()).isEqualTo(persistedEmployee.hashCode());
	}

	@Test
	@DisplayName("two employees with the same id and values are consistently mapped to the same hash code")
	void givenTwoEmployeesWithSameValues_whenHashMultipleTimes_thenSameHashCode() {
		// Arrange
		Department toPersistDepartment = this.departmentTestFactory.builder().id(null).create();
		Department persistedDepartment = this.departmentRepository.save(toPersistDepartment);

		Employee toPersistEmployee = this.employeeTestFactory.builder().department(persistedDepartment).create();
		Employee persistedEmployee = this.employeeRepository.save(toPersistEmployee);
		Employee found = this.employeeRepository.findById(persistedEmployee.getId()).orElseThrow();

		// Act / Assert
		IntStream.range(0, RandomUtils.nextInt(50, 60)).forEach(
				(int value) -> Assertions.assertThat(found.hashCode()).isEqualTo(persistedEmployee.hashCode()));

	}

	@Test
	@DisplayName("different employees do not have the same hash-code")
	void givenDifferentEmployees_whenHash_thenDifferentHashCode() {
		// Arrange
		Department toPersistDepartment = this.departmentTestFactory.builder().id(null).create();
		Department persistedDepartment = this.departmentRepository.save(toPersistDepartment);

		List<Employee> employees = IntStream.range(0, RandomUtils.nextInt(50, 200))
				.mapToObj((int value) -> this.employeeTestFactory.builder().department(persistedDepartment).create())
				.map(this.employeeRepository::save).toList();
		long distinctHashCodesCount = employees.stream().map(Employee::hashCode).distinct().count();

		// Act / Assert
		Assertions.assertThat(distinctHashCodesCount).isEqualTo(employees.size());
	}

}
