package de.stminko.employeeservice.department.entity;

import java.util.List;
import java.util.stream.IntStream;

import de.stminko.employeeservice.AbstractIntegrationTestSuite;
import de.stminko.employeeservice.department.control.DepartmentRepository;
import org.apache.commons.lang3.RandomUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;

class DepartmentIntegrationTest extends AbstractIntegrationTestSuite {

	@Autowired
	private DepartmentRepository departmentRepository;

	@Test
	@DisplayName("two departments with the same id are equals")
	void givenTwoDepartmentsWithSameId_whenCompare_thenEqual() {
		// Arrange
		Department persistedDepartment = departmentRepository.save(departmentTestFactory.builder().id(null).create());
		Department found = departmentRepository.findById(persistedDepartment.getId()).orElseThrow();

		// Act / Assert
		Assertions.assertThat(found).isEqualTo(persistedDepartment);
	}

	@Test
	@DisplayName("two different departments are not equals")
	void givenDifferentEmployees_whenCompare_thenNotEqual() {
		// Arrange
		Department first = departmentRepository.save(departmentTestFactory.builder().id(null).create());
		Department second = departmentRepository.save(departmentTestFactory.builder().id(null).create());

		// Act / Assert
		Assertions.assertThat(first).isNotEqualTo(second);
	}

	@Test
	@DisplayName("two employees with the same id and values are mapped to the same hash code")
	void givenTwoEmployeeWithSameValues_whenHash_thenSameHashCode() {
		// Arrange
		Department persistedDepartment = departmentRepository.save(departmentTestFactory.builder().id(null).create());
		Department found = departmentRepository.findById(persistedDepartment.getId()).orElseThrow();

		// Act / Assert
		Assertions.assertThat(found.hashCode()).isEqualTo(persistedDepartment.hashCode());
	}

	@Test
	@DisplayName("two employees with the same id and values are consistently mapped to the same hash value")
	void givenTwoEmployeeWithSameValues_whenHashMultipleTimes_thenSameValue() {
		// Arrange
		Department persistedDepartment = departmentRepository.save(departmentTestFactory.builder().id(null).create());
		Department found = departmentRepository.findById(persistedDepartment.getId()).orElseThrow();

		// Act / Assert
		IntStream.range(0, RandomUtils.nextInt(50, 60))
				.forEach(value -> Assertions.assertThat(found.hashCode()).isEqualTo(persistedDepartment.hashCode()));

	}

	@Test
	@DisplayName("different employees do not have the same hash-code")
	void givenDifferentEmployees_whenHash_thenDifferentValue() {
		// Arrange
		List<Department> departments = IntStream.range(0, RandomUtils.nextInt(50, 200))
				.mapToObj(value -> departmentTestFactory.builder().id(null).create()).map(departmentRepository::save)
				.toList();
		long distinctHashCodesCount = departments.stream().map(Department::hashCode).distinct().count();

		// Act / Assert
		Assertions.assertThat(distinctHashCodesCount).isEqualTo(departments.size());
	}

}