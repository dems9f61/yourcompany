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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

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
			DepartmentRequest departmentRequest = departmentRequestTestFactory.createDefault();

			// Act
			Department department = departmentService.create(departmentRequest);

			// Assert
			assertThat(department).isNotNull();
			assertThat(department.getId()).isGreaterThan(0L);
			assertThat(department.getDepartmentName()).isNotBlank().isEqualTo(departmentRequest.departmentName());
		}

		@Test
		@DisplayName("Creating a departments with a already existing name fails")
		void givenAlreadyExistingDepartmentName_whenCreate_thenThrowException() {
			// Arrange
			String departmentName = RandomStringUtils.randomAlphabetic(23);
			DepartmentRequest creationRequest = departmentRequestTestFactory.builder().departmentName(departmentName)
					.create();
			departmentService.create(creationRequest);

			DepartmentRequest creationRequest_2 = departmentRequestTestFactory.builder().departmentName(departmentName)
					.create();
			// Act / Assert
			assertThatExceptionOfType(BadRequestException.class)
					.isThrownBy(() -> departmentService.create(creationRequest_2));
		}

		@Test
		@DisplayName("Creating a departments with a empty name fails")
		void givenBlankDepartmentName_whenCreate_thenThrowException() {
			// Arrange
			DepartmentRequest creationRequest = departmentRequestTestFactory.builder().departmentName(" ").create();
			// Act / Assert
			assertThatExceptionOfType(ConstraintViolationException.class)
					.isThrownBy(() -> departmentService.create(creationRequest));
		}

		@Test
		@DisplayName("Creating a departments with a null name fails")
		void givenNullDepartmentName_whenCreate_thenThrowException() {
			// Arrange
			DepartmentRequest creationRequest = departmentRequestTestFactory.builder().departmentName(null).create();
			// Act / Assert
			assertThatExceptionOfType(ConstraintViolationException.class)
					.isThrownBy(() -> departmentService.create(creationRequest));
		}

	}

	@Nested
	@DisplayName("when access")
	class WhenAccess {

		@Test
		@DisplayName("Finding all departments returns all existing departments")
		void givenDepartments_whenFindAll_thenReturnAll() {
			// Arrange
			departmentRepository.deleteAll();
			List<DepartmentRequest> creationRequests = departmentRequestTestFactory
					.createManyDefault(RandomUtils.nextInt(10, 50));
			for (DepartmentRequest creationRequest : creationRequests) {
				departmentService.create(creationRequest);
			}

			// Act
			List<Department> all = departmentService.findAll();

			// Assert
			assertThat(all.size()).isEqualTo(creationRequests.size());
		}

		@Test
		@DisplayName("Finding a department with a correct department name returns the related department")
		void givenDepartments_whenFindByDepartmentName_thenReturnDepartment() {
			// Arrange
			List<DepartmentRequest> creationRequests = departmentRequestTestFactory
					.createManyDefault(RandomUtils.nextInt(10, 50));
			for (DepartmentRequest creationRequest : creationRequests) {
				departmentService.create(creationRequest);
			}

			DepartmentRequest creationRequest = departmentRequestTestFactory.createDefault();
			Department department = departmentService.create(creationRequest);

			// Act
			Department foundDepartment = departmentService.findByDepartmentName(creationRequest.departmentName());

			// Assert
			assertThat(foundDepartment.getId()).isEqualTo(department.getId());
			assertThat(foundDepartment.getDepartmentName()).isEqualTo(department.getDepartmentName());
		}

		@Test
		@DisplayName("Finding a department with a wrong department name returns nothing")
		void givenNotExistingDepartmentName_whenFindByDepartmentName_thenReturnNothing() {
			// Arrange
			List<DepartmentRequest> creationParameters = departmentRequestTestFactory
					.createManyDefault(RandomUtils.nextInt(10, 50));
			for (DepartmentRequest creationParameter : creationParameters) {
				departmentService.create(creationParameter);
			}
			String wrongDepartmentName = "unknownName";

			// Act / Assert
			assertThatExceptionOfType(NotFoundException.class)
					.isThrownBy(() -> departmentService.findByDepartmentName(wrongDepartmentName));
		}

	}

}