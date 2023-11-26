package de.stminko.employeeservice.department.boundary;

import java.util.List;

import de.stminko.employeeservice.AbstractIntegrationTestSuite;
import de.stminko.employeeservice.department.control.DepartmentRepository;
import de.stminko.employeeservice.department.entity.Department;
import de.stminko.employeeservice.department.entity.DepartmentRequest;
import de.stminko.employeeservice.runtime.errorhandling.boundary.BadRequestException;
import de.stminko.employeeservice.runtime.errorhandling.boundary.NotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;

@DisplayName("Integration tests for the department service")
class DepartmentServiceIntegrationTest extends AbstractIntegrationTestSuite {

	@Autowired
	private DepartmentRepository departmentRepository;

	@Autowired
	private DepartmentService departmentService;

	@Nested
	@DisplayName("when new")
	class WhenNew {

		@Test
		@DisplayName("Creating a department with a valid parameter succeeds")
		void givenValidRequestParams_whenCreate_thenStatusSucceed() {
			// Arrange
			DepartmentRequest departmentRequest = DepartmentServiceIntegrationTest.this.departmentRequestTestFactory
				.createDefault();

			// Act
			Department department = DepartmentServiceIntegrationTest.this.departmentService.create(departmentRequest);

			// Assert
			Assertions.assertThat(department).isNotNull();
			Assertions.assertThat(department.getId()).isGreaterThan(0L);
			Assertions.assertThat(department.getDepartmentName())
				.isNotBlank()
				.isEqualTo(departmentRequest.departmentName());
		}

		@Test
		@DisplayName("Creating a departments with a already existing name fails")
		void givenAlreadyExistingDepartmentName_whenCreate_thenThrowException() {
			// Arrange
			String departmentName = RandomStringUtils.randomAlphabetic(23);
			DepartmentRequest creationRequest = DepartmentServiceIntegrationTest.this.departmentRequestTestFactory
				.builder()
				.departmentName(departmentName)
				.create();
			DepartmentServiceIntegrationTest.this.departmentService.create(creationRequest);

			DepartmentRequest creationRequest_2 = DepartmentServiceIntegrationTest.this.departmentRequestTestFactory
				.builder()
				.departmentName(departmentName)
				.create();
			// Act / Assert
			Assertions.assertThatExceptionOfType(BadRequestException.class)
				.isThrownBy(() -> DepartmentServiceIntegrationTest.this.departmentService.create(creationRequest_2));
		}

		@Test
		@DisplayName("Creating a departments with a empty name fails")
		void givenBlankDepartmentName_whenCreate_thenThrowException() {
			// Arrange
			DepartmentRequest creationRequest = DepartmentServiceIntegrationTest.this.departmentRequestTestFactory
				.builder()
				.departmentName(" ")
				.create();
			// Act / Assert
			Assertions.assertThatExceptionOfType(ConstraintViolationException.class)
				.isThrownBy(() -> DepartmentServiceIntegrationTest.this.departmentService.create(creationRequest));
		}

		@Test
		@DisplayName("Creating a departments with a null name fails")
		void givenNullDepartmentName_whenCreate_thenThrowException() {
			// Arrange
			DepartmentRequest creationRequest = DepartmentServiceIntegrationTest.this.departmentRequestTestFactory
				.builder()
				.departmentName(null)
				.create();
			// Act / Assert
			Assertions.assertThatExceptionOfType(ConstraintViolationException.class)
				.isThrownBy(() -> DepartmentServiceIntegrationTest.this.departmentService.create(creationRequest));
		}

	}

	@Nested
	@DisplayName("when access")
	class WhenAccess {

		@Test
		@DisplayName("Finding all departments returns all existing departments")
		void givenDepartments_whenFindAll_thenReturnAll() {
			// Arrange
			DepartmentServiceIntegrationTest.this.departmentRepository.deleteAll();
			List<DepartmentRequest> creationRequests = DepartmentServiceIntegrationTest.this.departmentRequestTestFactory
				.createManyDefault(RandomUtils.nextInt(10, 50));
			for (DepartmentRequest creationRequest : creationRequests) {
				DepartmentServiceIntegrationTest.this.departmentService.create(creationRequest);
			}

			// Act
			List<Department> all = DepartmentServiceIntegrationTest.this.departmentService.findAll();

			// Assert
			Assertions.assertThat(all.size()).isEqualTo(creationRequests.size());
		}

		@Test
		@DisplayName("Finding a department with a correct department name returns the related department")
		void givenDepartments_whenFindByDepartmentName_thenReturnDepartment() {
			// Arrange
			List<DepartmentRequest> creationRequests = DepartmentServiceIntegrationTest.this.departmentRequestTestFactory
				.createManyDefault(RandomUtils.nextInt(10, 50));
			for (DepartmentRequest creationRequest : creationRequests) {
				DepartmentServiceIntegrationTest.this.departmentService.create(creationRequest);
			}

			DepartmentRequest creationRequest = DepartmentServiceIntegrationTest.this.departmentRequestTestFactory
				.createDefault();
			Department department = DepartmentServiceIntegrationTest.this.departmentService.create(creationRequest);

			// Act
			Department foundDepartment = DepartmentServiceIntegrationTest.this.departmentService
				.findByDepartmentName(creationRequest.departmentName());

			// Assert
			Assertions.assertThat(foundDepartment.getId()).isEqualTo(department.getId());
			Assertions.assertThat(foundDepartment.getDepartmentName()).isEqualTo(department.getDepartmentName());
		}

		@Test
		@DisplayName("Finding a department with a wrong department name throws NotFoundException")
		void givenNotExistingDepartmentName_whenFindByDepartmentName_thenThrowNotFoundException() {
			// Arrange
			List<DepartmentRequest> creationParameters = DepartmentServiceIntegrationTest.this.departmentRequestTestFactory
				.createManyDefault(RandomUtils.nextInt(10, 50));
			for (DepartmentRequest creationParameter : creationParameters) {
				DepartmentServiceIntegrationTest.this.departmentService.create(creationParameter);
			}
			String wrongDepartmentName = "unknownName";

			// Act / Assert
			Assertions.assertThatExceptionOfType(NotFoundException.class)
				.isThrownBy(() -> DepartmentServiceIntegrationTest.this.departmentService
					.findByDepartmentName(wrongDepartmentName));
		}

	}

}