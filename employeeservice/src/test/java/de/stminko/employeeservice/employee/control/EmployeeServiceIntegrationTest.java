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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

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
			DepartmentRequest DepartmentRequest = departmentRequestTestFactory.createDefault();
			departmentService.create(DepartmentRequest);
			EmployeeRequest EmployeeRequest = employeeRequestTestFactory.builder()
					.departmentName(DepartmentRequest.departmentName()).create();

			// Act
			Employee employee = employeeService.create(EmployeeRequest);

			// Assert
			assertThat(employee).isNotNull();
			Employee.FullName fullName = employee.getFullName();
			assertThat(fullName.getFirstName()).isNotBlank().isEqualTo(EmployeeRequest.firstName());
			assertThat(fullName.getLastName()).isNotBlank().isEqualTo(EmployeeRequest.lastName());
			assertThat(employee.getEmailAddress()).isNotBlank().isEqualTo(EmployeeRequest.emailAddress());
			Department department = employee.getDepartment();
			assertThat(department).isNotNull();
			assertThat(department.getDepartmentName()).isEqualTo(EmployeeRequest.departmentName());
			ZonedDateTime birthday = employee.getBirthday();
			assertThat(birthday).isNotNull().isEqualTo(EmployeeRequest.birthday());
			verify(employeeEventPublisher).employeeCreated(employee);
		}

		@Test
		@DisplayName("Creating an employee with a wrong department name fails")
		void givenWrongDepartmentName_whenCreate_thenThrowBadRequestException() {
			// Arrange
			EmployeeRequest EmployeeRequest = employeeRequestTestFactory.builder()
					.departmentName(RandomStringUtils.randomAlphabetic(4)).create();
			// Act/ Assert
			assertThatExceptionOfType(BadRequestException.class)
					.isThrownBy(() -> employeeService.create(EmployeeRequest));
		}

		@Test
		@DisplayName("Creating two employees with the same email fails")
		void givenEmailAddressToUseForTwoEmployees_whenCreate_thenThrowBadRequestException() {
			// Arrange
			DepartmentRequest DepartmentRequest = departmentRequestTestFactory.createDefault();
			Department department = departmentService.create(DepartmentRequest);
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
			DepartmentRequest DepartmentRequest = departmentRequestTestFactory.createDefault();
			departmentService.create(DepartmentRequest);
			List<EmployeeRequest> EmployeeRequests = new LinkedList<>();
			IntStream.range(0, RandomUtils.nextInt(20, 30))
					.forEach(value -> EmployeeRequests.add(employeeRequestTestFactory.builder()
							.departmentName(DepartmentRequest.departmentName()).create()));

			for (EmployeeRequest EmployeeRequest : EmployeeRequests) {
				employeeService.create(EmployeeRequest);
			}
			String unknownId = UUID.randomUUID().toString();

			// Act / Assert
			assertThatExceptionOfType(NotFoundException.class).isThrownBy(() -> employeeService.findById(unknownId));
		}

		@Test
		@DisplayName("Finding an employee with a correct employee uuid returns the related employee")
		void givenEmployee_whenFind_thenReturnEmployee() {
			// Arrange
			DepartmentRequest DepartmentRequest = departmentRequestTestFactory.createDefault();
			departmentService.create(DepartmentRequest);
			EmployeeRequest EmployeeRequest = employeeRequestTestFactory.builder()
					.departmentName(DepartmentRequest.departmentName()).create();
			Employee employee = employeeService.create(EmployeeRequest);
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

	}

	@Nested
	@DisplayName("when update")
	class WhenUpdate {

		@Test
		@DisplayName("(Full) Updating employee fields with valid parameters succeeds")
		void givenValidRequestParams_whenFullUpdate_thenSucceed() {
			// Arrange
			DepartmentRequest DepartmentRequest = departmentRequestTestFactory.createDefault();
			departmentService.create(DepartmentRequest);
			EmployeeRequest EmployeeRequest = employeeRequestTestFactory.builder()
					.departmentName(DepartmentRequest.departmentName()).create();
			Employee employee = employeeService.create(EmployeeRequest);

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
		@DisplayName("(Partial) Updating an employee birthday succeeds without affecting other values")
		void givenValidBirthday_whenPartialUpdate_thenUpdateOnlyBirthDay() {
			// Arrange
			DepartmentRequest DepartmentRequest = departmentRequestTestFactory.createDefault();
			departmentService.create(DepartmentRequest);
			EmployeeRequest EmployeeRequest = employeeRequestTestFactory.builder()
					.departmentName(DepartmentRequest.departmentName()).create();
			Employee employee = employeeService.create(EmployeeRequest);

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
			DepartmentRequest DepartmentRequest = departmentRequestTestFactory.createDefault();
			departmentService.create(DepartmentRequest);
			EmployeeRequest EmployeeRequest = employeeRequestTestFactory.builder()
					.departmentName(DepartmentRequest.departmentName()).create();
			Employee employee = employeeService.create(EmployeeRequest);

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
			DepartmentRequest DepartmentRequest = departmentRequestTestFactory.createDefault();
			departmentService.create(DepartmentRequest);
			EmployeeRequest EmployeeRequest = employeeRequestTestFactory.builder()
					.departmentName(DepartmentRequest.departmentName()).create();
			Employee employee = employeeService.create(EmployeeRequest);
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
			EmployeeRequest EmployeeRequest = employeeRequestTestFactory.createDefault();

			// Act / Assert
			assertThatExceptionOfType(NotFoundException.class)
					.isThrownBy(() -> employeeService.doPartialUpdate(UUID.randomUUID().toString(), EmployeeRequest));
		}

		@Test
		@DisplayName("Updating an employee with unknown department fails")
		void givenUnknownDepartment_whenUpdate_thenThrowBadRequestException() {
			// Arrange
			DepartmentRequest DepartmentRequest = departmentRequestTestFactory.createDefault();
			departmentService.create(DepartmentRequest);
			EmployeeRequest EmployeeRequest = employeeRequestTestFactory.builder()
					.departmentName(DepartmentRequest.departmentName()).create();
			Employee employee = employeeService.create(EmployeeRequest);
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
			IntStream.range(0, RandomUtils.nextInt(30, 50))
					.forEach(value -> employeeRequests.add(employeeRequestTestFactory.builder()
							.departmentName(departmentRequest.departmentName()).create()));

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
			DepartmentRequest DepartmentRequest = departmentRequestTestFactory.createDefault();
			departmentService.create(DepartmentRequest);
			EmployeeRequest EmployeeRequest = employeeRequestTestFactory.builder()
					.departmentName(DepartmentRequest.departmentName()).create();
			Employee employee = employeeService.create(EmployeeRequest);
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