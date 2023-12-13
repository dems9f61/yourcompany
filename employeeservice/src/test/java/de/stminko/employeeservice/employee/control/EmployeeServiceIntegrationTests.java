package de.stminko.employeeservice.employee.control;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import de.stminko.employeeservice.AbstractIntegrationTestSuite;
import de.stminko.employeeservice.department.boundary.DepartmentService;
import de.stminko.employeeservice.department.entity.Department;
import de.stminko.employeeservice.department.entity.DepartmentRequest;
import de.stminko.employeeservice.employee.boundary.EmployeeService;
import de.stminko.employeeservice.employee.entity.Employee;
import de.stminko.employeeservice.employee.entity.EmployeeRequest;
import de.stminko.employeeservice.employee.entity.UsableDateFormat;
import de.stminko.employeeservice.runtime.errorhandling.boundary.BadRequestException;
import de.stminko.employeeservice.runtime.errorhandling.boundary.NotFoundException;
import info.solidsoft.mockito.java8.AssertionMatcher;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@DisplayName("Integration tests for the employee service")
class EmployeeServiceIntegrationTests extends AbstractIntegrationTestSuite {

	@Autowired
	private DepartmentService departmentService;

	@Autowired
	private EmployeeService employeeService;

	@Nested
	@DisplayName("when new")
	class WhenNew {

		@Test
		@DisplayName("Creating an employee with valid parameters succeeds")
		void givenValidRequestParams_whenCreate_thenSucceed() {
			// Arrange
			DepartmentRequest departmentRequest = EmployeeServiceIntegrationTests.this.departmentRequestTestFactory
				.createDefault();
			EmployeeServiceIntegrationTests.this.departmentService.create(departmentRequest);
			EmployeeRequest employeeRequest = EmployeeServiceIntegrationTests.this.employeeRequestTestFactory.builder()
				.departmentName(departmentRequest.departmentName())
				.create();

			// Act
			Employee employee = EmployeeServiceIntegrationTests.this.employeeService.create(employeeRequest);

			// Assert
			Assertions.assertThat(employee).isNotNull();
			Employee.FullName fullName = employee.getFullName();
			Assertions.assertThat(fullName.getFirstName()).isNotBlank().isEqualTo(employeeRequest.firstName());
			Assertions.assertThat(fullName.getLastName()).isNotBlank().isEqualTo(employeeRequest.lastName());
			Assertions.assertThat(employee.getEmailAddress()).isNotBlank().isEqualTo(employeeRequest.emailAddress());
			Department department = employee.getDepartment();
			Assertions.assertThat(department).isNotNull();
			Assertions.assertThat(department.getDepartmentName()).isEqualTo(employeeRequest.departmentName());
			ZonedDateTime birthday = employee.getBirthday();
			Assertions.assertThat(birthday).isNotNull().isEqualTo(employeeRequest.birthday());
			Mockito.verify(EmployeeServiceIntegrationTests.this.employeeEventPublisher).employeeCreated(employee);
		}

		@Test
		@DisplayName("Creating an employee with a wrong department name fails")
		void givenWrongDepartmentName_whenCreate_thenThrowBadRequestException() {
			// Arrange
			EmployeeRequest employeeRequest = EmployeeServiceIntegrationTests.this.employeeRequestTestFactory.builder()
				.departmentName(RandomStringUtils.randomAlphabetic(4))
				.create();
			// Act/ Assert
			Assertions.assertThatExceptionOfType(BadRequestException.class)
				.isThrownBy(() -> EmployeeServiceIntegrationTests.this.employeeService.create(employeeRequest));
		}

		@Test
		@DisplayName("Creating two employees with the same email fails")
		void givenEmailAddressToUseForTwoEmployees_whenCreate_thenThrowBadRequestException() {
			// Arrange
			DepartmentRequest departmentRequest = EmployeeServiceIntegrationTests.this.departmentRequestTestFactory
				.createDefault();
			Department department = EmployeeServiceIntegrationTests.this.departmentService.create(departmentRequest);
			EmployeeRequest firstEmployeeRequest = EmployeeServiceIntegrationTests.this.employeeRequestTestFactory
				.builder()
				.departmentName(department.getDepartmentName())
				.create();
			EmployeeServiceIntegrationTests.this.employeeService.create(firstEmployeeRequest);

			EmployeeRequest secondEmployeeRequest = EmployeeServiceIntegrationTests.this.employeeRequestTestFactory
				.builder()
				.departmentName(department.getDepartmentName())
				.emailAddress(firstEmployeeRequest.emailAddress())
				.create();

			// Act / Assert
			Assertions.assertThatExceptionOfType(BadRequestException.class)
				.isThrownBy(() -> EmployeeServiceIntegrationTests.this.employeeService.create(secondEmployeeRequest));
		}

	}

	@Nested
	@DisplayName("when access")
	class WhenAccess {

		@Test
		@DisplayName("Finding an employee with a wrong uuid fails")
		void givenUnknownUuid_whenFindById_thenThrowResourceNotFoundException() {
			// Arrange
			DepartmentRequest departmentRequest = EmployeeServiceIntegrationTests.this.departmentRequestTestFactory
				.createDefault();
			EmployeeServiceIntegrationTests.this.departmentService.create(departmentRequest);
			List<EmployeeRequest> employeeRequests = new LinkedList<>();
			IntStream.range(0, RandomUtils.nextInt(20, 30))
				.forEach((int value) -> employeeRequests
					.add(EmployeeServiceIntegrationTests.this.employeeRequestTestFactory.builder()
						.departmentName(departmentRequest.departmentName())
						.create()));

			for (EmployeeRequest employeeRequest : employeeRequests) {
				EmployeeServiceIntegrationTests.this.employeeService.create(employeeRequest);
			}
			String unknownId = UUID.randomUUID().toString();

			// Act / Assert
			Assertions.assertThatExceptionOfType(NotFoundException.class)
				.isThrownBy(() -> EmployeeServiceIntegrationTests.this.employeeService.findById(unknownId));
		}

		@Test
		@DisplayName("Finding an employee with a correct employee uuid returns the related employee")
		void givenEmployee_whenFindById_thenReturnEmployee() {
			// Arrange
			DepartmentRequest departmentRequest = EmployeeServiceIntegrationTests.this.departmentRequestTestFactory
				.createDefault();
			EmployeeServiceIntegrationTests.this.departmentService.create(departmentRequest);
			EmployeeRequest employeeRequest = EmployeeServiceIntegrationTests.this.employeeRequestTestFactory.builder()
				.departmentName(departmentRequest.departmentName())
				.create();
			Employee employee = EmployeeServiceIntegrationTests.this.employeeService.create(employeeRequest);
			String id = employee.getId();
			assert id != null;

			// Act
			Employee foundEmployee = EmployeeServiceIntegrationTests.this.employeeService.findById(id);

			// Assert
			Assertions.assertThat(foundEmployee.getId()).isEqualTo(id);
			Assertions.assertThat(foundEmployee.getEmailAddress()).isEqualTo(employee.getEmailAddress());
			Assertions.assertThat(foundEmployee.getFullName()).isEqualTo(employee.getFullName());
			Assertions.assertThat(foundEmployee.getDepartment().getId()).isEqualTo(employee.getDepartment().getId());
		}

		@Test
		@DisplayName("Finding all employee returns all persisted employees")
		void givenEmployees_whenFindAll_thenReturnAllEmployees() {
			// Arrange
			DepartmentRequest departmentRequest = EmployeeServiceIntegrationTests.this.departmentRequestTestFactory
				.createDefault();
			Department department = EmployeeServiceIntegrationTests.this.departmentService.create(departmentRequest);

			List<Employee> employees = new LinkedList<>();
			IntStream.range(0, RandomUtils.nextInt(10, 20)).forEach((int i) -> {
				EmployeeRequest employeeRequest = EmployeeServiceIntegrationTests.this.employeeRequestTestFactory
					.builder()
					.departmentName(department.getDepartmentName())
					.create();
				employees.add(EmployeeServiceIntegrationTests.this.employeeService.create(employeeRequest));
			});
			Pageable pageRequest = PageRequest.of(0, 200);

			// Act
			Page<Employee> all = EmployeeServiceIntegrationTests.this.employeeService.findAll(pageRequest);

			// Assert
			Assertions.assertThat(all).hasSameSizeAs(employees);
		}

	}

	@Nested
	@DisplayName("when update")
	class WhenUpdate {

		@Test
		@DisplayName("(Full) Updating employee fields with valid parameters succeeds")
		void givenValidRequestParams_whenFullUpdate_thenSucceed() {
			// Arrange
			DepartmentRequest departmentRequest = EmployeeServiceIntegrationTests.this.departmentRequestTestFactory
				.createDefault();
			EmployeeServiceIntegrationTests.this.departmentService.create(departmentRequest);
			EmployeeRequest employeeRequest = EmployeeServiceIntegrationTests.this.employeeRequestTestFactory.builder()
				.departmentName(departmentRequest.departmentName())
				.create();
			Employee employee = EmployeeServiceIntegrationTests.this.employeeService.create(employeeRequest);

			EmployeeRequest updateRequest = EmployeeServiceIntegrationTests.this.employeeRequestTestFactory
				.createDefault();
			DepartmentRequest newDepartmentRequest = EmployeeServiceIntegrationTests.this.departmentRequestTestFactory
				.builder()
				.departmentName(updateRequest.departmentName())
				.create();
			EmployeeServiceIntegrationTests.this.departmentService.create(newDepartmentRequest);
			String id = employee.getId();
			assert id != null;

			// Act
			EmployeeServiceIntegrationTests.this.employeeService.doFullUpdate(id, updateRequest);

			// Assert
			Employee updated = EmployeeServiceIntegrationTests.this.employeeService.findById(id);
			Assertions.assertThat(updated.getEmailAddress()).isEqualTo(updateRequest.emailAddress());
			Assertions.assertThat(updated.getFullName().getFirstName()).isEqualTo(updateRequest.firstName());
			Assertions.assertThat(updated.getFullName().getLastName()).isEqualTo(updateRequest.lastName());
			Assertions.assertThat(updated.getBirthday()).isEqualTo(updateRequest.birthday());
			Assertions.assertThat(updated.getDepartment().getDepartmentName())
				.isEqualTo(updateRequest.departmentName());
			Mockito.verify(EmployeeServiceIntegrationTests.this.employeeEventPublisher)
				.employeeUpdated(AssertionMatcher
					.assertArg((Employee publishedEmployee) -> Assertions.assertThat(publishedEmployee.getId())
						.isEqualTo(updated.getId())));
		}

		@Test
		@DisplayName("(Partial) Updating employee full name with valid parameters succeeds")
		void givenEmployeeWithNoFullName_whenPartialUpdate_thenSucceed() {
			// Arrange
			DepartmentRequest departmentRequest = EmployeeServiceIntegrationTests.this.departmentRequestTestFactory
				.createDefault();
			EmployeeServiceIntegrationTests.this.departmentService.create(departmentRequest);
			EmployeeRequest employeeRequest = EmployeeServiceIntegrationTests.this.employeeRequestTestFactory.builder()
				.departmentName(departmentRequest.departmentName())
				.firstName(null)
				.lastName(null)
				.create();
			Employee employee = EmployeeServiceIntegrationTests.this.employeeService.create(employeeRequest);

			String newFirstName = RandomStringUtils.randomAlphabetic(23);
			String lastLastName = RandomStringUtils.randomAlphabetic(23);
			EmployeeRequest updateRequest = EmployeeServiceIntegrationTests.this.employeeRequestTestFactory.builder()
				.emailAddress(null)
				.departmentName(null)
				.firstName(newFirstName)
				.lastName(lastLastName)
				.birthday(null)
				.create();
			String id = employee.getId();
			assert id != null;

			// Act
			EmployeeServiceIntegrationTests.this.employeeService.doPartialUpdate(id, updateRequest);

			// Assert
			Employee updated = EmployeeServiceIntegrationTests.this.employeeService.findById(id);
			Assertions.assertThat(updated.getEmailAddress()).isEqualTo(employee.getEmailAddress());
			Assertions.assertThat(updated.getFullName().getFirstName()).isEqualTo(updateRequest.firstName());
			Assertions.assertThat(updated.getFullName().getLastName()).isEqualTo(updateRequest.lastName());
			Assertions.assertThat(updated.getBirthday()).isEqualTo(employee.getBirthday());
			Assertions.assertThat(updated.getDepartment().getDepartmentName())
				.isEqualTo(employee.getDepartment().getDepartmentName());
			Mockito.verify(EmployeeServiceIntegrationTests.this.employeeEventPublisher)
				.employeeUpdated(AssertionMatcher
					.assertArg((Employee publishedEmployee) -> Assertions.assertThat(publishedEmployee.getId())
						.isEqualTo(updated.getId())));
		}

		@Test
		@DisplayName("(Partial) Updating employee with no change does nothing")
		void givenNoChange_whenPartialUpdate_thenDoNothing() {
			// Arrange
			DepartmentRequest departmentRequest = EmployeeServiceIntegrationTests.this.departmentRequestTestFactory
				.createDefault();
			EmployeeServiceIntegrationTests.this.departmentService.create(departmentRequest);
			EmployeeRequest employeeRequest = EmployeeServiceIntegrationTests.this.employeeRequestTestFactory.builder()
				.departmentName(departmentRequest.departmentName())
				.create();
			Employee employee = EmployeeServiceIntegrationTests.this.employeeService.create(employeeRequest);

			EmployeeRequest updateRequest = EmployeeServiceIntegrationTests.this.employeeRequestTestFactory.builder()
				.emailAddress(null)
				.departmentName(null)
				.firstName(null)
				.lastName(null)
				.birthday(null)
				.create();
			String id = employee.getId();
			assert id != null;

			// Act
			EmployeeServiceIntegrationTests.this.employeeService.doPartialUpdate(id, updateRequest);

			// Assert
			Employee updated = EmployeeServiceIntegrationTests.this.employeeService.findById(id);
			Assertions.assertThat(updated.getEmailAddress()).isEqualTo(employee.getEmailAddress());
			Assertions.assertThat(updated.getFullName().getFirstName())
				.isEqualTo(employee.getFullName().getFirstName());
			Assertions.assertThat(updated.getFullName().getLastName()).isEqualTo(employee.getFullName().getLastName());
			Assertions.assertThat(updated.getBirthday()).isEqualTo(employee.getBirthday());
			Assertions.assertThat(updated.getDepartment().getDepartmentName())
				.isEqualTo(employee.getDepartment().getDepartmentName());
			Mockito.verify(EmployeeServiceIntegrationTests.this.employeeEventPublisher, Mockito.never())
				.employeeUpdated(ArgumentMatchers.any());
		}

		@Test
		@DisplayName("(Partial) Updating an employee birthday succeeds without affecting other values")
		void givenValidBirthday_whenPartialUpdate_thenUpdateOnlyBirthDay() {
			// Arrange
			DepartmentRequest departmentRequest = EmployeeServiceIntegrationTests.this.departmentRequestTestFactory
				.createDefault();
			EmployeeServiceIntegrationTests.this.departmentService.create(departmentRequest);
			EmployeeRequest employeeRequest = EmployeeServiceIntegrationTests.this.employeeRequestTestFactory.builder()
				.departmentName(departmentRequest.departmentName())
				.create();
			Employee employee = EmployeeServiceIntegrationTests.this.employeeService.create(employeeRequest);

			DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(UsableDateFormat.DEFAULT.getDateFormat());
			LocalDate localDate = LocalDate.parse("1979-12-03", dateFormatter);
			ZonedDateTime newBirthDay = localDate.atStartOfDay(ZoneOffset.UTC);
			EmployeeRequest updateParameters = EmployeeServiceIntegrationTests.this.employeeRequestTestFactory.builder()
				.emailAddress(null)
				.departmentName(null)
				.firstName(null)
				.lastName(null)
				.birthday(newBirthDay)
				.create();
			String id = employee.getId();
			assert id != null;

			// Act
			EmployeeServiceIntegrationTests.this.employeeService.doPartialUpdate(id, updateParameters);

			// Assert
			Employee updated = EmployeeServiceIntegrationTests.this.employeeService.findById(id);
			Assertions.assertThat(updated.getEmailAddress()).isEqualTo(employee.getEmailAddress());
			Employee.FullName fullName = updated.getFullName();
			Assertions.assertThat(fullName.getFirstName()).isEqualTo(employee.getFullName().getFirstName());
			Assertions.assertThat(fullName.getLastName()).isEqualTo(employee.getFullName().getLastName());
			Assertions.assertThat(updated.getDepartment().getDepartmentName())
				.isEqualTo(employee.getDepartment().getDepartmentName());
			Assertions.assertThat(updated.getBirthday()).isEqualTo(newBirthDay);
			Mockito.verify(EmployeeServiceIntegrationTests.this.employeeEventPublisher)
				.employeeUpdated(AssertionMatcher
					.assertArg((Employee publishedEmployee) -> Assertions.assertThat(publishedEmployee.getId())
						.isEqualTo(updated.getId())));
		}

		@Test
		@DisplayName("(Partial) Updating an employee full name succeeds without affecting other values")
		void givenValidFullName_whenPartialUpdate_thenUpdateOnlyFullName() {
			// Arrange
			DepartmentRequest departmentRequest = EmployeeServiceIntegrationTests.this.departmentRequestTestFactory
				.createDefault();
			EmployeeServiceIntegrationTests.this.departmentService.create(departmentRequest);
			EmployeeRequest employeeRequest = EmployeeServiceIntegrationTests.this.employeeRequestTestFactory.builder()
				.departmentName(departmentRequest.departmentName())
				.create();
			Employee employee = EmployeeServiceIntegrationTests.this.employeeService.create(employeeRequest);

			String expectedFirstName = RandomStringUtils.randomAlphabetic(23);
			String expectedLastName = RandomStringUtils.randomAlphabetic(23);

			EmployeeRequest updateParameters = EmployeeServiceIntegrationTests.this.employeeRequestTestFactory.builder()
				.emailAddress(null)
				.departmentName(null)
				.firstName(expectedFirstName)
				.lastName(expectedLastName)
				.birthday(null)
				.create();
			String id = employee.getId();
			assert id != null;

			// Act
			EmployeeServiceIntegrationTests.this.employeeService.doPartialUpdate(id, updateParameters);

			// Assert
			Employee updated = EmployeeServiceIntegrationTests.this.employeeService.findById(id);
			Assertions.assertThat(updated.getEmailAddress()).isEqualTo(employee.getEmailAddress());
			Assertions.assertThat(updated.getFullName().getFirstName()).isEqualTo(expectedFirstName);
			Assertions.assertThat(updated.getFullName().getLastName()).isEqualTo(expectedLastName);
			Assertions.assertThat(updated.getDepartment().getDepartmentName())
				.isEqualTo(employee.getDepartment().getDepartmentName());
			Assertions.assertThat(updated.getBirthday()).isEqualTo(employee.getBirthday());
			Mockito.verify(EmployeeServiceIntegrationTests.this.employeeEventPublisher)
				.employeeUpdated(AssertionMatcher
					.assertArg((Employee publishedEmployee) -> Assertions.assertThat(publishedEmployee.getId())
						.isEqualTo(updated.getId())));
		}

		@Test
		@DisplayName("(Partial) Updating an employee email succeeds without affecting other values")
		void givenValidEmail_whenPartialUpdate_thenUpdateOnlyEmail() {
			// Arrange
			DepartmentRequest departmentRequest = EmployeeServiceIntegrationTests.this.departmentRequestTestFactory
				.createDefault();
			EmployeeServiceIntegrationTests.this.departmentService.create(departmentRequest);
			EmployeeRequest employeeRequest = EmployeeServiceIntegrationTests.this.employeeRequestTestFactory.builder()
				.departmentName(departmentRequest.departmentName())
				.create();
			Employee employee = EmployeeServiceIntegrationTests.this.employeeService.create(employeeRequest);
			String expectedEmail = EmployeeServiceIntegrationTests.this.employeeRequestTestFactory.builder()
				.generateRandomEmail();

			EmployeeRequest updateRequest = EmployeeServiceIntegrationTests.this.employeeRequestTestFactory.builder()
				.emailAddress(expectedEmail)
				.departmentName(null)
				.firstName(null)
				.lastName(null)
				.birthday(null)
				.create();
			String id = employee.getId();
			assert id != null;

			// Act
			EmployeeServiceIntegrationTests.this.employeeService.doPartialUpdate(id, updateRequest);

			// Assert
			Employee updated = EmployeeServiceIntegrationTests.this.employeeService.findById(id);
			Assertions.assertThat(updated.getEmailAddress()).isEqualTo(expectedEmail);
			Assertions.assertThat(updated.getFullName().getFirstName())
				.isEqualTo(employee.getFullName().getFirstName());
			Assertions.assertThat(updated.getFullName().getLastName()).isEqualTo(employee.getFullName().getLastName());
			Assertions.assertThat(updated.getDepartment().getDepartmentName())
				.isEqualTo(employee.getDepartment().getDepartmentName());
			Assertions.assertThat(updated.getBirthday()).isEqualTo(employee.getBirthday());
			Mockito.verify(EmployeeServiceIntegrationTests.this.employeeEventPublisher)
				.employeeUpdated(AssertionMatcher
					.assertArg((Employee publishedEmployee) -> Assertions.assertThat(publishedEmployee.getId())
						.isEqualTo(updated.getId())));
		}

		@Test
		@DisplayName("(Partial) Updating an employee with wrong uuid fails")
		void givenUnknownUuid_whenPartialUpdate_thenThrowNotFoundException() {
			// Arrange
			EmployeeRequest employeeRequest = EmployeeServiceIntegrationTests.this.employeeRequestTestFactory
				.createDefault();

			// Act / Assert
			Assertions.assertThatExceptionOfType(NotFoundException.class)
				.isThrownBy(() -> EmployeeServiceIntegrationTests.this.employeeService
					.doPartialUpdate(UUID.randomUUID().toString(), employeeRequest));
		}

		@Test
		@DisplayName("Updating an employee with unknown department fails")
		void givenUnknownDepartment_whenUpdate_thenThrowBadRequestException() {
			// Arrange
			DepartmentRequest departmentRequest = EmployeeServiceIntegrationTests.this.departmentRequestTestFactory
				.createDefault();
			EmployeeServiceIntegrationTests.this.departmentService.create(departmentRequest);
			EmployeeRequest employeeRequest = EmployeeServiceIntegrationTests.this.employeeRequestTestFactory.builder()
				.departmentName(departmentRequest.departmentName())
				.create();
			Employee employee = EmployeeServiceIntegrationTests.this.employeeService.create(employeeRequest);
			EmployeeRequest update = EmployeeServiceIntegrationTests.this.employeeRequestTestFactory.createDefault();

			// Act / Assert
			Assertions.assertThatExceptionOfType(BadRequestException.class).isThrownBy(() -> {
				assert employee.getId() != null;
				EmployeeServiceIntegrationTests.this.employeeService.doPartialUpdate(employee.getId(), update);
			});
		}

	}

	@Nested
	@DisplayName("when delete")
	class WhenDelete {

		@Test
		@DisplayName("Deleting an employee with a wrong uuid fails")
		void givenUnknownUuid_whenDelete_thenThrowNotFoundException() {
			// Arrange
			DepartmentRequest departmentRequest = EmployeeServiceIntegrationTests.this.departmentRequestTestFactory
				.createDefault();
			EmployeeServiceIntegrationTests.this.departmentService.create(departmentRequest);
			List<EmployeeRequest> employeeRequests = new LinkedList<>();
			IntStream.range(0, RandomUtils.nextInt(30, 50))
				.forEach((int value) -> employeeRequests
					.add(EmployeeServiceIntegrationTests.this.employeeRequestTestFactory.builder()
						.departmentName(departmentRequest.departmentName())
						.create()));

			for (EmployeeRequest employeeRequest : employeeRequests) {
				EmployeeServiceIntegrationTests.this.employeeService.create(employeeRequest);
			}

			// Act / Assert
			Assertions.assertThatExceptionOfType(NotFoundException.class)
				.isThrownBy(() -> EmployeeServiceIntegrationTests.this.employeeService
					.deleteById(UUID.randomUUID().toString()));
		}

		@Test
		@DisplayName("Deleting an employee with a correct employee uuid succeeds")
		void givenEmployee_whenDelete_thenSucceed() {
			// Arrange
			DepartmentRequest departmentRequest = EmployeeServiceIntegrationTests.this.departmentRequestTestFactory
				.createDefault();
			EmployeeServiceIntegrationTests.this.departmentService.create(departmentRequest);
			EmployeeRequest employeeRequest = EmployeeServiceIntegrationTests.this.employeeRequestTestFactory.builder()
				.departmentName(departmentRequest.departmentName())
				.create();
			Employee employee = EmployeeServiceIntegrationTests.this.employeeService.create(employeeRequest);
			String uuid = employee.getId();
			assert uuid != null;

			// Act
			EmployeeServiceIntegrationTests.this.employeeService.deleteById(uuid);

			// Assert
			Assertions.assertThatExceptionOfType(NotFoundException.class)
				.isThrownBy(() -> EmployeeServiceIntegrationTests.this.employeeService.findById(uuid));
			Mockito.verify(EmployeeServiceIntegrationTests.this.employeeEventPublisher)
				.employeeDeleted(AssertionMatcher
					.assertArg((Employee publishedEmployee) -> Assertions.assertThat(publishedEmployee.getId())
						.isEqualTo(uuid)));
		}

	}

}
