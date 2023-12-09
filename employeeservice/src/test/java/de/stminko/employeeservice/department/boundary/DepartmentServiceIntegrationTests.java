package de.stminko.employeeservice.department.boundary;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import de.stminko.employeeservice.AbstractIntegrationTestSuite;
import de.stminko.employeeservice.department.control.DepartmentRepository;
import de.stminko.employeeservice.department.entity.Department;
import de.stminko.employeeservice.department.entity.DepartmentRequest;
import de.stminko.employeeservice.employee.control.EmployeeService;
import de.stminko.employeeservice.employee.entity.Employee;
import de.stminko.employeeservice.employee.entity.EmployeeRequest;
import de.stminko.employeeservice.runtime.errorhandling.boundary.BadRequestException;
import de.stminko.employeeservice.runtime.errorhandling.boundary.DepartmentNotEmptyException;
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
class DepartmentServiceIntegrationTests extends AbstractIntegrationTestSuite {

	@Autowired
	private DepartmentRepository departmentRepository;

	@Autowired
	private DepartmentService departmentService;

	@Autowired
	private EmployeeService employeeService;

	@Nested
	@DisplayName("when new")
	class WhenNew {

		@Test
		@DisplayName("Creating a department with a valid parameter succeeds")
		void givenValidRequestParams_whenCreate_thenStatusSucceed() {
			// Arrange
			DepartmentRequest departmentRequest = DepartmentServiceIntegrationTests.this.departmentRequestTestFactory
				.createDefault();

			// Act
			Department department = DepartmentServiceIntegrationTests.this.departmentService.create(departmentRequest);

			// Assert
			Assertions.assertThat(department).isNotNull();
			Assertions.assertThat(department.getId()).isGreaterThan(0L);
			Assertions.assertThat(department.getCreatedAt()).isNotNull();
			Assertions.assertThat(department.getModifiedAt()).isNotNull();
			Assertions.assertThat(department.getDepartmentName())
				.isNotBlank()
				.isEqualTo(departmentRequest.departmentName());
		}

		@Test
		@DisplayName("Creating a departments with a already existing name fails")
		void givenAlreadyExistingDepartmentName_whenCreate_thenThrowException() {
			// Arrange
			String departmentName = RandomStringUtils.randomAlphabetic(23);
			DepartmentRequest creationRequest = DepartmentServiceIntegrationTests.this.departmentRequestTestFactory
				.builder()
				.departmentName(departmentName)
				.create();
			DepartmentServiceIntegrationTests.this.departmentService.create(creationRequest);

			DepartmentRequest creationRequest_2 = DepartmentServiceIntegrationTests.this.departmentRequestTestFactory
				.builder()
				.departmentName(departmentName)
				.create();
			// Act / Assert
			Assertions.assertThatExceptionOfType(BadRequestException.class)
				.isThrownBy(() -> DepartmentServiceIntegrationTests.this.departmentService.create(creationRequest_2));
		}

		@Test
		@DisplayName("Creating a departments with a empty name fails")
		void givenBlankDepartmentName_whenCreate_thenThrowException() {
			// Arrange
			DepartmentRequest creationRequest = DepartmentServiceIntegrationTests.this.departmentRequestTestFactory
				.builder()
				.departmentName(" ")
				.create();
			// Act / Assert
			Assertions.assertThatExceptionOfType(ConstraintViolationException.class)
				.isThrownBy(() -> DepartmentServiceIntegrationTests.this.departmentService.create(creationRequest));
		}

		@Test
		@DisplayName("Creating a departments with a null name fails")
		void givenNullDepartmentName_whenCreate_thenThrowException() {
			// Arrange
			DepartmentRequest creationRequest = DepartmentServiceIntegrationTests.this.departmentRequestTestFactory
				.builder()
				.departmentName(null)
				.create();
			// Act / Assert
			Assertions.assertThatExceptionOfType(ConstraintViolationException.class)
				.isThrownBy(() -> DepartmentServiceIntegrationTests.this.departmentService.create(creationRequest));
		}

	}

	@Nested
	@DisplayName("when access")
	class WhenAccess {

		@Test
		@DisplayName("Finding all departments returns all existing departments")
		void givenDepartments_whenFindAll_thenReturnAll() {
			// Arrange
			DepartmentServiceIntegrationTests.this.departmentRepository.deleteAll();
			List<DepartmentRequest> creationRequests = DepartmentServiceIntegrationTests.this.departmentRequestTestFactory
				.createManyDefault(RandomUtils.nextInt(10, 50));
			for (DepartmentRequest creationRequest : creationRequests) {
				DepartmentServiceIntegrationTests.this.departmentService.create(creationRequest);
			}

			// Act
			List<Department> all = DepartmentServiceIntegrationTests.this.departmentService.findAll();

			// Assert
			Assertions.assertThat(all.size()).isEqualTo(creationRequests.size());
		}

		@Test
		@DisplayName("Finding a department with a correct department name returns the related department")
		void givenDepartments_whenFindByDepartmentName_thenReturnDepartment() {
			// Arrange
			List<DepartmentRequest> creationRequests = DepartmentServiceIntegrationTests.this.departmentRequestTestFactory
				.createManyDefault(RandomUtils.nextInt(10, 50));
			for (DepartmentRequest creationRequest : creationRequests) {
				DepartmentServiceIntegrationTests.this.departmentService.create(creationRequest);
			}

			DepartmentRequest creationRequest = DepartmentServiceIntegrationTests.this.departmentRequestTestFactory
				.createDefault();
			Department department = DepartmentServiceIntegrationTests.this.departmentService.create(creationRequest);

			// Act
			Department foundDepartment = DepartmentServiceIntegrationTests.this.departmentService
				.findByDepartmentName(creationRequest.departmentName());

			// Assert
			Assertions.assertThat(foundDepartment.getId()).isEqualTo(department.getId());
			Assertions.assertThat(foundDepartment.getDepartmentName()).isEqualTo(department.getDepartmentName());
		}

		@Test
		@DisplayName("Finding a department with a wrong department name throws NotFoundException")
		void givenNotExistingDepartmentName_whenFindByDepartmentName_thenThrowNotFoundException() {
			// Arrange
			List<DepartmentRequest> creationParameters = DepartmentServiceIntegrationTests.this.departmentRequestTestFactory
				.createManyDefault(RandomUtils.nextInt(10, 50));
			for (DepartmentRequest creationParameter : creationParameters) {
				DepartmentServiceIntegrationTests.this.departmentService.create(creationParameter);
			}
			String wrongDepartmentName = "unknownName";

			// Act / Assert
			Assertions.assertThatExceptionOfType(NotFoundException.class)
				.isThrownBy(() -> DepartmentServiceIntegrationTests.this.departmentService
					.findByDepartmentName(wrongDepartmentName));
		}

		@Test
		@DisplayName("Finding an department with a wrong id throws NotFoundException")
		void givenUnknownId_whenFindById_thenThrowNotFoundException() {
			// Arrange
			List<DepartmentRequest> departmentRequests = new LinkedList<>();
			IntStream.range(0, RandomUtils.nextInt(20, 30))
				.forEach((int value) -> departmentRequests
					.add(DepartmentServiceIntegrationTests.this.departmentRequestTestFactory.builder()
						.departmentName(RandomStringUtils.randomAlphabetic(40))
						.create()));

			for (DepartmentRequest currentRequest : departmentRequests) {
				DepartmentServiceIntegrationTests.this.departmentService.create(currentRequest);
			}
			Long unknownId = Long.MAX_VALUE;

			// Act / Assert
			Assertions.assertThatExceptionOfType(NotFoundException.class)
				.isThrownBy(() -> DepartmentServiceIntegrationTests.this.departmentService.findById(unknownId));
		}

		@Test
		@DisplayName("Finding an department with a correct department id returns the related department")
		void givenDepartment_whenFindById_thenReturnDepartment() {
			// Arrange
			DepartmentRequest departmentRequest = DepartmentServiceIntegrationTests.this.departmentRequestTestFactory
				.createDefault();
			Department createdDepartment = DepartmentServiceIntegrationTests.this.departmentService
				.create(departmentRequest);
			Long id = createdDepartment.getId();
			assert id != null;

			// Act
			Department foundDepartment = DepartmentServiceIntegrationTests.this.departmentService.findById(id);

			// Assert
			Assertions.assertThat(foundDepartment.getId()).isEqualTo(id);
			Assertions.assertThat(foundDepartment.getCreatedAt())
				.isEqualToIgnoringNanos(createdDepartment.getCreatedAt());
			Assertions.assertThat(foundDepartment.getModifiedAt())
				.isEqualToIgnoringNanos(createdDepartment.getModifiedAt());
		}

		@Test
		@DisplayName("Finding all employees associated to a department returns not empty set")
		void givenMultipleEmployeesAssociatedToDepartment_whenFindAllAssociatedEmployeesByDepId_thenReturnAllAssociatedEmployees() {
			// Arrange
			DepartmentRequest departmentRequest = DepartmentServiceIntegrationTests.this.departmentRequestTestFactory
				.createDefault();
			Department department = DepartmentServiceIntegrationTests.this.departmentService.create(departmentRequest);

			int count = RandomUtils.nextInt(20, 30);
			IntStream.range(0, count)
				.mapToObj((int value) -> DepartmentServiceIntegrationTests.this.employeeRequestTestFactory.builder()
					.departmentName(departmentRequest.departmentName())
					.create())
				.forEach((EmployeeRequest employeeRequest) -> DepartmentServiceIntegrationTests.this.employeeService
					.create(employeeRequest));
			Long id = department.getId();
			assert id != null;

			// Act
			Set<Employee> associatedEmployees = DepartmentServiceIntegrationTests.this.departmentService
				.findAllEmployeesById(id);

			// Assert
			Assertions.assertThat(associatedEmployees).isNotEmpty().hasSize(count);
		}

		@Test
		@DisplayName("Finding all employees associated to a department returns  empty set")
		void givenNoEmployeeAssociatedToDepartment_whenFindAllAssociatedEmployeesByDepId_thenReturnEmptySet() {
			// Arrange
			DepartmentRequest departmentRequest = DepartmentServiceIntegrationTests.this.departmentRequestTestFactory
				.createDefault();
			Department department = DepartmentServiceIntegrationTests.this.departmentService.create(departmentRequest);
			Long id = department.getId();
			assert id != null;

			// Act
			Set<Employee> associatedEmployees = DepartmentServiceIntegrationTests.this.departmentService
				.findAllEmployeesById(id);

			// Assert
			Assertions.assertThat(associatedEmployees).isEmpty();
		}

	}

	@Nested
	@DisplayName("when update")
	class WhenUpdate {

		@Test
		@DisplayName("(Full) Updating department fields with valid parameters succeeds")
		void givenValidRequestParams_whenFullUpdate_thenSucceed() {
			// Arrange
			Department departmentToUpdate = DepartmentServiceIntegrationTests.this.departmentService
				.create(DepartmentServiceIntegrationTests.this.departmentRequestTestFactory.createDefault());

			DepartmentRequest departmentRequest = DepartmentServiceIntegrationTests.this.departmentRequestTestFactory
				.createDefault();
			// When
			Department updatedDepartment = DepartmentServiceIntegrationTests.this.departmentService
				.doFullUpdate(departmentToUpdate.getId(), departmentRequest);

			// Assert
			Assertions.assertThat(updatedDepartment.getDepartmentName()).isEqualTo(departmentRequest.departmentName());
		}

		@Test
		@DisplayName("(Full) Updating department fields with no department name fails")
		void givenRequestWithoutDepartmentName_whenFullUpdate_thenThrowConstraintViolationException() {
			// Arrange
			Department departmentToUpdate = DepartmentServiceIntegrationTests.this.departmentService
				.create(DepartmentServiceIntegrationTests.this.departmentRequestTestFactory.createDefault());

			DepartmentRequest departmentRequest = DepartmentServiceIntegrationTests.this.departmentRequestTestFactory
				.builder()
				.departmentName(null)
				.create();

			// Act / Assert
			Assertions.assertThatExceptionOfType(ConstraintViolationException.class)
				.isThrownBy(() -> DepartmentServiceIntegrationTests.this.departmentService
					.doFullUpdate(departmentToUpdate.getId(), departmentRequest));

		}

		@Test
		@DisplayName("(Partial) Updating department fields with valid parameters succeeds")
		void givenValidRequestParams_whenPartialUpdate_thenSucceed() {
			// Arrange
			Department departmentToUpdate = DepartmentServiceIntegrationTests.this.departmentService
				.create(DepartmentServiceIntegrationTests.this.departmentRequestTestFactory.createDefault());

			DepartmentRequest departmentRequest = DepartmentServiceIntegrationTests.this.departmentRequestTestFactory
				.createDefault();
			// Act
			Department updatedDepartment = DepartmentServiceIntegrationTests.this.departmentService
				.doPartialUpdate(departmentToUpdate.getId(), departmentRequest);

			// Assert
			Assertions.assertThat(updatedDepartment.getDepartmentName()).isEqualTo(departmentRequest.departmentName());
		}

		@Test
		@DisplayName("(Partial) Updating department fields with no department name succeeds but does nothing")
		void givenValidRequestWithoutDepartmentName_whenPartialUpdate_thenSucceedAndDoesNothing() {
			// Arrange
			Department departmentToUpdate = DepartmentServiceIntegrationTests.this.departmentService
				.create(DepartmentServiceIntegrationTests.this.departmentRequestTestFactory.createDefault());

			DepartmentRequest departmentRequest = DepartmentServiceIntegrationTests.this.departmentRequestTestFactory
				.builder()
				.departmentName(null)
				.create();
			// Act
			Department updatedDepartment = DepartmentServiceIntegrationTests.this.departmentService
				.doPartialUpdate(departmentToUpdate.getId(), departmentRequest);

			// Assert
			Assertions.assertThat(updatedDepartment.getDepartmentName())
				.isNotEqualTo(departmentRequest.departmentName())
				.isEqualTo(departmentToUpdate.getDepartmentName());
		}

	}

	@Nested
	@DisplayName("when delete")
	class WhenDelete {

		@Test
		@DisplayName("Deleting a department with a wrong id fails")
		void givenUnknownId_whenDeleteById_thenThrowNotFoundException() {
			// Arrange
			List<DepartmentRequest> departmentRequests = new LinkedList<>();
			IntStream.range(0, RandomUtils.nextInt(30, 50))
				.forEach((int value) -> departmentRequests
					.add(DepartmentServiceIntegrationTests.this.departmentRequestTestFactory.builder()
						.departmentName(RandomStringUtils.randomAlphabetic(40))
						.create()));

			for (DepartmentRequest currentRequest : departmentRequests) {
				DepartmentServiceIntegrationTests.this.departmentService.create(currentRequest);
			}

			// Act / Assert
			Assertions.assertThatExceptionOfType(NotFoundException.class)
				.isThrownBy(() -> DepartmentServiceIntegrationTests.this.departmentService.deleteById(Long.MAX_VALUE));
		}

		@Test
		@DisplayName("Deleting a department as it still contains some employee throws DepartmentNotEmptyException")
		void givenDepartmentStillContainingEmployee_whenDeleteById_thenThrowDepartmentNotEmptyException() {
			// Arrange
			DepartmentRequest departmentRequest = DepartmentServiceIntegrationTests.this.departmentRequestTestFactory
				.createDefault();
			Department department = DepartmentServiceIntegrationTests.this.departmentService.create(departmentRequest);

			EmployeeRequest employeeRequest = DepartmentServiceIntegrationTests.this.employeeRequestTestFactory
				.builder()
				.departmentName(departmentRequest.departmentName())
				.create();
			DepartmentServiceIntegrationTests.this.employeeService.create(employeeRequest);
			assert department.getId() != null;

			// Act / Assert
			Assertions.assertThatExceptionOfType(DepartmentNotEmptyException.class)
				.isThrownBy(
						() -> DepartmentServiceIntegrationTests.this.departmentService.deleteById(department.getId()));

		}

		@Test
		@DisplayName("Deleting a department with a correct department id succeeds")
		void givenDepartment_whenDelete_thenSucceed() {
			// Arrange
			DepartmentRequest departmentRequest = DepartmentServiceIntegrationTests.this.departmentRequestTestFactory
				.createDefault();
			Department persistdDepartment = DepartmentServiceIntegrationTests.this.departmentService
				.create(departmentRequest);
			Long id = persistdDepartment.getId();
			assert id != null;

			// Act
			DepartmentServiceIntegrationTests.this.departmentService.deleteById(id);

			// Assert
			Assertions.assertThatExceptionOfType(NotFoundException.class)
				.isThrownBy(() -> DepartmentServiceIntegrationTests.this.departmentService.findById(id));
		}

	}

}
