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
import static info.solidsoft.mockito.java8.AssertionMatcher.assertArg;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.verify;

@DisplayName("Integration tests for the employee service")
class EmployeeServiceIntegrationTest extends AbstractIntegrationTestSuite {

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
			DepartmentRequest departmentRequest = departmentRequestTestFactory.createDefault();
			departmentService.create(departmentRequest);
			EmployeeRequest employeeRequest = employeeRequestTestFactory.builder()
					.departmentName(departmentRequest.departmentName()).create();

			// Act
			Employee employee = employeeService.create(employeeRequest);

			// Assert
			assertThat(employee).isNotNull();
			Employee.FullName fullName = employee.getFullName();
			assertThat(fullName.getFirstName()).isNotBlank().isEqualTo(employeeRequest.firstName());
			assertThat(fullName.getLastName()).isNotBlank().isEqualTo(employeeRequest.lastName());
			assertThat(employee.getEmailAddress()).isNotBlank().isEqualTo(employeeRequest.emailAddress());
			Department department = employee.getDepartment();
			assertThat(department).isNotNull();
			assertThat(department.getDepartmentName()).isEqualTo(employeeRequest.departmentName());
			ZonedDateTime birthday = employee.getBirthday();
			assertThat(birthday).isNotNull().isEqualTo(employeeRequest.birthday());
			verify(employeeEventPublisher).employeeCreated(employee);
		}

		@Test
		@DisplayName("Creating an employee with a wrong department name fails")
		void givenWrongDepartmentName_whenCreate_thenThrowBadRequestException() {
			// Arrange
			EmployeeRequest employeeRequest = employeeRequestTestFactory.builder()
					.departmentName(RandomStringUtils.randomAlphabetic(4)).create();
			// Act/ Assert
			assertThatExceptionOfType(BadRequestException.class)
					.isThrownBy(() -> employeeService.create(employeeRequest));
		}

		@Test
		@DisplayName("Creating two employees with the same email fails")
		void givenEmailAddressToUseForTwoEmployees_whenCreate_thenThrowBadRequestException() {
			// Arrange
			DepartmentRequest departmentRequest = departmentRequestTestFactory.createDefault();
			Department department = departmentService.create(departmentRequest);
			EmployeeRequest firstEmployeeRequest = employeeRequestTestFactory.builder()
					.departmentName(department.getDepartmentName()).create();
			employeeService.create(firstEmployeeRequest);

			EmployeeRequest secondEmployeeRequest = employeeRequestTestFactory.builder()
					.departmentName(department.getDepartmentName()).emailAddress(firstEmployeeRequest.emailAddress())
					.create();

			// Act / Assert
			assertThatExceptionOfType(BadRequestException.class)
					.isThrownBy(() -> employeeService.create(secondEmployeeRequest));
		}

	}

	@Nested
	@DisplayName("when access")
	class WhenAccess {

		@Test
		@DisplayName("Finding an employee with a wrong uuid fails")
		void givenUnknownUuid_whenFind_thenThrowResourceNotFoundException() {
			// Arrange
			DepartmentRequest departmentRequest = departmentRequestTestFactory.createDefault();
			departmentService.create(departmentRequest);
			List<EmployeeRequest> EmployeeRequests = new LinkedList<>();
			IntStream.range(0, RandomUtils.nextInt(20, 30)).forEach(value -> EmployeeRequests.add(
					employeeRequestTestFactory.builder().departmentName(departmentRequest.departmentName()).create()));

			for (EmployeeRequest employeeRequest : EmployeeRequests) {
				employeeService.create(employeeRequest);
			}
			String unknownId = UUID.randomUUID().toString();

			// Act / Assert
			assertThatExceptionOfType(NotFoundException.class).isThrownBy(() -> employeeService.findById(unknownId));
		}

		@Test
		@DisplayName("Finding an employee with a correct employee uuid returns the related employee")
		void givenEmployee_whenFind_thenReturnEmployee() {
			// Arrange
			DepartmentRequest departmentRequest = departmentRequestTestFactory.createDefault();
			departmentService.create(departmentRequest);
			EmployeeRequest employeeRequest = employeeRequestTestFactory.builder()
					.departmentName(departmentRequest.departmentName()).create();
			Employee employee = employeeService.create(employeeRequest);
			String id = employee.getId();
			assert id != null;

			// Act
			Employee foundEmployee = employeeService.findById(id);

			// Assert
			assertThat(foundEmployee.getId()).isEqualTo(id);
			assertThat(foundEmployee.getEmailAddress()).isEqualTo(employee.getEmailAddress());
			assertThat(foundEmployee.getFullName()).isEqualTo(employee.getFullName());
			assertThat(foundEmployee.getDepartment().getId()).isEqualTo(employee.getDepartment().getId());
		}

		@Test
		@DisplayName("Finding all employee returns all persisted employees")
		void givenEmployees_whenFindAll_thenReturnAllEmployees() {
			// Arrange
			DepartmentRequest departmentRequest = departmentRequestTestFactory.createDefault();
			Department department = departmentService.create(departmentRequest);

			List<Employee> employees = new LinkedList<>();
			IntStream.range(0, RandomUtils.nextInt(10, 20)).forEach(i -> {
				EmployeeRequest employeeRequest = employeeRequestTestFactory.builder()
						.departmentName(department.getDepartmentName()).create();
				employees.add(employeeService.create(employeeRequest));
			});

			// Act
			List<Employee> all = employeeService.findAll();

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
			DepartmentRequest departmentRequest = departmentRequestTestFactory.createDefault();
			departmentService.create(departmentRequest);
			EmployeeRequest employeeRequest = employeeRequestTestFactory.builder()
					.departmentName(departmentRequest.departmentName()).create();
			Employee employee = employeeService.create(employeeRequest);

			EmployeeRequest updateRequest = employeeRequestTestFactory.createDefault();
			DepartmentRequest newDepartmentRequest = departmentRequestTestFactory.builder()
					.departmentName(updateRequest.departmentName()).create();
			departmentService.create(newDepartmentRequest);
			String id = employee.getId();

			// Act
			employeeService.doFullUpdate(id, updateRequest);

			// Assert
			Employee updated = employeeService.findById(id);
			assertThat(updated.getEmailAddress()).isEqualTo(updateRequest.emailAddress());
			assertThat(updated.getFullName().getFirstName()).isEqualTo(updateRequest.firstName());
			assertThat(updated.getFullName().getLastName()).isEqualTo(updateRequest.lastName());
			assertThat(updated.getBirthday()).isEqualTo(updateRequest.birthday());
			assertThat(updated.getDepartment().getDepartmentName()).isEqualTo(updateRequest.departmentName());
			verify(employeeEventPublisher).employeeUpdated(
					assertArg(publishedEmployee -> assertThat(publishedEmployee.getId()).isEqualTo(updated.getId())));
		}

		@Test
		@DisplayName("(Partial) Updating employee full name with valid parameters succeeds")
		void givenEmployeeWithNoFullName_whenPartialUpdate_thenSucceed() {
			// Arrange
			DepartmentRequest departmentRequest = departmentRequestTestFactory.createDefault();
			departmentService.create(departmentRequest);
			EmployeeRequest employeeRequest = employeeRequestTestFactory.builder()
					.departmentName(departmentRequest.departmentName()).firstName(null).lastName(null).create();
			Employee employee = employeeService.create(employeeRequest);

			String newFirstName = RandomStringUtils.randomAlphabetic(23);
			String lastLastName = RandomStringUtils.randomAlphabetic(23);
			EmployeeRequest updateRequest = employeeRequestTestFactory.builder().emailAddress(null).departmentName(null)
					.firstName(newFirstName).lastName(lastLastName).birthday(null).create();
			String id = employee.getId();

			// Act
			employeeService.doPartialUpdate(id, updateRequest);

			// Assert
			Employee updated = employeeService.findById(id);
			assertThat(updated.getEmailAddress()).isEqualTo(employee.getEmailAddress());
			assertThat(updated.getFullName().getFirstName()).isEqualTo(updateRequest.firstName());
			assertThat(updated.getFullName().getLastName()).isEqualTo(updateRequest.lastName());
			assertThat(updated.getBirthday()).isEqualTo(employee.getBirthday());
			assertThat(updated.getDepartment().getDepartmentName())
					.isEqualTo(employee.getDepartment().getDepartmentName());
			verify(employeeEventPublisher).employeeUpdated(
					assertArg(publishedEmployee -> assertThat(publishedEmployee.getId()).isEqualTo(updated.getId())));
		}

		@Test
		@DisplayName("(Partial) Updating employee with no change does nothing")
		void givenNoChange_whenPartialUpdate_thenDoNothing() {
			// Arrange
			DepartmentRequest departmentRequest = departmentRequestTestFactory.createDefault();
			departmentService.create(departmentRequest);
			EmployeeRequest employeeRequest = employeeRequestTestFactory.builder()
					.departmentName(departmentRequest.departmentName()).create();
			Employee employee = employeeService.create(employeeRequest);

			EmployeeRequest updateRequest = employeeRequestTestFactory.builder().emailAddress(null).departmentName(null)
					.firstName(null).lastName(null).birthday(null).create();
			String id = employee.getId();

			// Act
			employeeService.doPartialUpdate(id, updateRequest);

			// Assert
			Employee updated = employeeService.findById(id);
			assertThat(updated.getEmailAddress()).isEqualTo(employee.getEmailAddress());
			assertThat(updated.getFullName().getFirstName()).isEqualTo(employee.getFullName().getFirstName());
			assertThat(updated.getFullName().getLastName()).isEqualTo(employee.getFullName().getLastName());
			assertThat(updated.getBirthday()).isEqualTo(employee.getBirthday());
			assertThat(updated.getDepartment().getDepartmentName())
					.isEqualTo(employee.getDepartment().getDepartmentName());
			Mockito.verify(employeeEventPublisher, Mockito.never()).employeeUpdated(ArgumentMatchers.any());
		}

		@Test
		@DisplayName("(Partial) Updating an employee birthday succeeds without affecting other values")
		void givenValidBirthday_whenPartialUpdate_thenUpdateOnlyBirthDay() {
			// Arrange
			DepartmentRequest departmentRequest = departmentRequestTestFactory.createDefault();
			departmentService.create(departmentRequest);
			EmployeeRequest employeeRequest = employeeRequestTestFactory.builder()
					.departmentName(departmentRequest.departmentName()).create();
			Employee employee = employeeService.create(employeeRequest);

			DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(UsableDateFormat.DEFAULT.getDateFormat());
			LocalDate localDate = LocalDate.parse("1979-12-03", dateFormatter);
			ZonedDateTime newBirthDay = localDate.atStartOfDay(ZoneOffset.UTC);
			EmployeeRequest updateParameters = employeeRequestTestFactory.builder().emailAddress(null)
					.departmentName(null).firstName(null).lastName(null).birthday(newBirthDay).create();
			String id = employee.getId();
			assert id != null;

			// Act
			employeeService.doPartialUpdate(id, updateParameters);

			// Assert
			Employee updated = employeeService.findById(id);
			assertThat(updated.getEmailAddress()).isEqualTo(employee.getEmailAddress());
			Employee.FullName fullName = updated.getFullName();
			assertThat(fullName.getFirstName()).isEqualTo(employee.getFullName().getFirstName());
			assertThat(fullName.getLastName()).isEqualTo(employee.getFullName().getLastName());
			assertThat(updated.getDepartment().getDepartmentName())
					.isEqualTo(employee.getDepartment().getDepartmentName());
			assertThat(updated.getBirthday()).isEqualTo(newBirthDay);
			verify(employeeEventPublisher).employeeUpdated(
					assertArg(publishedEmployee -> assertThat(publishedEmployee.getId()).isEqualTo(updated.getId())));
		}

		@Test
		@DisplayName("(Partial) Updating an employee full name succeeds without affecting other values")
		void givenValidFullName_whenPartialUpdate_thenUpdateOnlyFullName() {
			// Arrange
			DepartmentRequest departmentRequest = departmentRequestTestFactory.createDefault();
			departmentService.create(departmentRequest);
			EmployeeRequest employeeRequest = employeeRequestTestFactory.builder()
					.departmentName(departmentRequest.departmentName()).create();
			Employee employee = employeeService.create(employeeRequest);

			String expectedFirstName = RandomStringUtils.randomAlphabetic(23);
			String expectedLastName = RandomStringUtils.randomAlphabetic(23);

			EmployeeRequest updateParameters = employeeRequestTestFactory.builder().emailAddress(null)
					.departmentName(null).firstName(expectedFirstName).lastName(expectedLastName).birthday(null)
					.create();
			String id = employee.getId();

			// Act
			employeeService.doPartialUpdate(id, updateParameters);

			// Assert
			Employee updated = employeeService.findById(id);
			assertThat(updated.getEmailAddress()).isEqualTo(employee.getEmailAddress());
			assertThat(updated.getFullName().getFirstName()).isEqualTo(expectedFirstName);
			assertThat(updated.getFullName().getLastName()).isEqualTo(expectedLastName);
			assertThat(updated.getDepartment().getDepartmentName())
					.isEqualTo(employee.getDepartment().getDepartmentName());
			assertThat(updated.getBirthday()).isEqualTo(employee.getBirthday());
			verify(employeeEventPublisher).employeeUpdated(
					assertArg(publishedEmployee -> assertThat(publishedEmployee.getId()).isEqualTo(updated.getId())));
		}

		@Test
		@DisplayName("(Partial) Updating an employee email succeeds without affecting other values")
		void givenValidEmail_whenPartialUpdate_thenUpdateOnlyEmail() {
			// Arrange
			DepartmentRequest departmentRequest = departmentRequestTestFactory.createDefault();
			departmentService.create(departmentRequest);
			EmployeeRequest employeeRequest = employeeRequestTestFactory.builder()
					.departmentName(departmentRequest.departmentName()).create();
			Employee employee = employeeService.create(employeeRequest);
			String expectedEmail = employeeRequestTestFactory.builder().generateRandomEmail();

			EmployeeRequest updateRequest = employeeRequestTestFactory.builder().emailAddress(expectedEmail)
					.departmentName(null).firstName(null).lastName(null).birthday(null).create();
			String id = employee.getId();

			// Act
			employeeService.doPartialUpdate(id, updateRequest);

			// Assert
			Employee updated = employeeService.findById(id);
			assertThat(updated.getEmailAddress()).isEqualTo(expectedEmail);
			assertThat(updated.getFullName().getFirstName()).isEqualTo(employee.getFullName().getFirstName());
			assertThat(updated.getFullName().getLastName()).isEqualTo(employee.getFullName().getLastName());
			assertThat(updated.getDepartment().getDepartmentName())
					.isEqualTo(employee.getDepartment().getDepartmentName());
			assertThat(updated.getBirthday()).isEqualTo(employee.getBirthday());
			verify(employeeEventPublisher).employeeUpdated(
					assertArg(publishedEmployee -> assertThat(publishedEmployee.getId()).isEqualTo(updated.getId())));
		}

		@Test
		@DisplayName("(Partial) Updating an employee with wrong uuid fails")
		void givenUnknownUuid_whenPartialUpdate_thenThrowNotFoundException() {
			// Arrange
			EmployeeRequest employeeRequest = employeeRequestTestFactory.createDefault();

			// Act / Assert
			assertThatExceptionOfType(NotFoundException.class)
					.isThrownBy(() -> employeeService.doPartialUpdate(UUID.randomUUID().toString(), employeeRequest));
		}

		@Test
		@DisplayName("Updating an employee with unknown department fails")
		void givenUnknownDepartment_whenUpdate_thenThrowBadRequestException() {
			// Arrange
			DepartmentRequest departmentRequest = departmentRequestTestFactory.createDefault();
			departmentService.create(departmentRequest);
			EmployeeRequest employeeRequest = employeeRequestTestFactory.builder()
					.departmentName(departmentRequest.departmentName()).create();
			Employee employee = employeeService.create(employeeRequest);
			EmployeeRequest update = employeeRequestTestFactory.createDefault();

			// Act / Assert
			assertThatExceptionOfType(BadRequestException.class)
					.isThrownBy(() -> employeeService.doPartialUpdate(employee.getId(), update));
		}

	}

	@Nested
	@DisplayName("when delete")
	class WhenDelete {

		@Test
		@DisplayName("Deleting an employee with a wrong uuid fails")
		void givenUnknownUuid_whenFind_thenThrowNotFoundException() {
			// Arrange
			DepartmentRequest departmentRequest = departmentRequestTestFactory.createDefault();
			departmentService.create(departmentRequest);
			List<EmployeeRequest> employeeRequests = new LinkedList<>();
			IntStream.range(0, RandomUtils.nextInt(30, 50)).forEach(value -> employeeRequests.add(
					employeeRequestTestFactory.builder().departmentName(departmentRequest.departmentName()).create()));

			for (EmployeeRequest employeeRequest : employeeRequests) {
				employeeService.create(employeeRequest);
			}

			// Act / Assert
			assertThatExceptionOfType(NotFoundException.class)
					.isThrownBy(() -> employeeService.deleteById(UUID.randomUUID().toString()));
		}

		@Test
		@DisplayName("Deleting an employee with a correct employee uuid succeeds")
		void givenEmployee_whenDelete_thenSucceed() {
			// Arrange
			DepartmentRequest departmentRequest = departmentRequestTestFactory.createDefault();
			departmentService.create(departmentRequest);
			EmployeeRequest employeeRequest = employeeRequestTestFactory.builder()
					.departmentName(departmentRequest.departmentName()).create();
			Employee employee = employeeService.create(employeeRequest);
			String uuid = employee.getId();
			assert uuid != null;

			// Act
			employeeService.deleteById(uuid);

			// Assert
			assertThatExceptionOfType(NotFoundException.class).isThrownBy(() -> employeeService.findById(uuid));
			verify(employeeEventPublisher).employeeDeleted(AssertionMatcher
					.assertArg(publishedEmployee -> assertThat(publishedEmployee.getId()).isEqualTo(uuid)));
		}

	}

}