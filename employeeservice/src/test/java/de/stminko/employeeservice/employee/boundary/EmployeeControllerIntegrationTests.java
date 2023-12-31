package de.stminko.employeeservice.employee.boundary;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import de.stminko.employeeservice.AbstractIntegrationTestSuite;
import de.stminko.employeeservice.department.boundary.DepartmentController;
import de.stminko.employeeservice.department.boundary.dto.DepartmentRequest;
import de.stminko.employeeservice.department.boundary.dto.DepartmentResponse;
import de.stminko.employeeservice.employee.boundary.dto.EmployeeRequest;
import de.stminko.employeeservice.employee.boundary.dto.EmployeeResponse;
import de.stminko.employeeservice.employee.boundary.dto.UsableDateFormat;
import de.stminko.employeeservice.employee.control.EmployeeRepository;
import de.stminko.employeeservice.employee.entity.Employee;
import de.stminko.employeeservice.runtime.rest.bondary.DataView;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.assertj.core.api.Assertions;
import org.hamcrest.Matchers;
import org.json.JSONObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@AutoConfigureMockMvc
class EmployeeControllerIntegrationTests extends AbstractIntegrationTestSuite {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private EmployeeRepository employeeRepository;

	private final DateTimeFormatter dateFormatter = DateTimeFormatter
		.ofPattern(UsableDateFormat.DEFAULT.getDateFormat());

	private String generateRandomEmail() {
		return RandomStringUtils.randomAlphanumeric(RandomUtils.nextInt(10, 24)) + "@"
				+ (RandomStringUtils.randomAlphanumeric(10) + ".com");
	}

	private List<EmployeeResponse> saveRandomEmployees(int count) throws Exception {

		DepartmentResponse departmentResponse = saveRandomDepartment();

		int normalizedCount = (count <= 0) ? 1 : count;
		List<EmployeeResponse> result = new ArrayList<>(count);
		String uri = EmployeeController.BASE_URI;
		for (int i = 0; i < normalizedCount; i++) {
			EmployeeRequest employeeRequest = this.employeeRequestTestFactory.builder()
				.departmentName(departmentResponse.departmentName())
				.create();

			String requestAsJson = transformRequestToJSONByView(employeeRequest, DataView.POST.class);
			MvcResult mvcResult = this.mockMvc
				.perform(
						MockMvcRequestBuilders.post(uri).contentType(MediaType.APPLICATION_JSON).content(requestAsJson))
				.andReturn();
			String contentAsString = mvcResult.getResponse().getContentAsString();
			result.add(this.objectMapper.readValue(contentAsString, EmployeeResponse.class));
		}
		return result;
	}

	private DepartmentResponse saveRandomDepartment() throws Exception {
		DepartmentRequest departmentRequest = this.departmentRequestTestFactory.createDefault();
		String departmentRequestAsJson = transformRequestToJSONByView(departmentRequest, DataView.POST.class);
		MvcResult departmentMvcResult = this.mockMvc
			.perform(MockMvcRequestBuilders.post(DepartmentController.BASE_URI)
				.contentType(MediaType.APPLICATION_JSON)
				.content(departmentRequestAsJson))
			.andReturn();
		return this.objectMapper.readValue(departmentMvcResult.getResponse().getContentAsString(),
				DepartmentResponse.class);
	}

	@Nested
	@DisplayName("when create")
	class WhenCreate {

		@Test
		@DisplayName("POST: 'hhtps://.../employees' returns CREATED for valid request")
		void givenValidRequest_whenCreateEmployee_thenStatus201() throws Exception {
			// Arrange
			DepartmentResponse departmentResponse = saveRandomDepartment();
			EmployeeRequest toPersist = EmployeeControllerIntegrationTests.this.employeeRequestTestFactory.builder()
				.departmentName(departmentResponse.departmentName())
				.create();
			String requestAsJson = transformRequestToJSONByView(toPersist, DataView.POST.class);
			String uri = "%s".formatted(EmployeeController.BASE_URI);
			String formattedBirthday = EmployeeControllerIntegrationTests.this.dateFormatter
				.format(toPersist.birthday());

			// Act / Assert
			MvcResult mvcResult = EmployeeControllerIntegrationTests.this.mockMvc
				.perform(
						MockMvcRequestBuilders.post(uri).contentType(MediaType.APPLICATION_JSON).content(requestAsJson))
				.andExpect(MockMvcResultMatchers.status().isCreated())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.notNullValue()))
				.andExpect(MockMvcResultMatchers.header()
					.string(HttpHeaders.LOCATION, Matchers.containsString(EmployeeController.BASE_URI)))
				.andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.notNullValue()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.employeeId", Matchers.notNullValue()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.firstName", Matchers.is(toPersist.firstName())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.lastName", Matchers.is(toPersist.lastName())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.birthday", Matchers.is(formattedBirthday)))
				.andExpect(MockMvcResultMatchers.jsonPath("$.emailAddress", Matchers.is(toPersist.emailAddress())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.departmentName", Matchers.is(toPersist.departmentName())))
				.andReturn();
			String contentAsString = mvcResult.getResponse().getContentAsString();
			EmployeeResponse employeeResponse = EmployeeControllerIntegrationTests.this.objectMapper
				.readValue(contentAsString, EmployeeResponse.class);
			Optional<Employee> optionalEmployee = EmployeeControllerIntegrationTests.this.employeeRepository
				.findById(employeeResponse.employeeId());
			Assertions.assertThat(optionalEmployee)
				.hasValueSatisfying((Employee value) -> Assertions.assertThat(value).isNotNull());

		}

		@Test
		@DisplayName("POST: 'hhtps://.../employees' returns CREATED even if only the department name is specified")
		void givenOnlyDepartment_whenCreateEmployee_thenStatus201() throws Exception {
			// Arrange
			DepartmentResponse departmentResponse = saveRandomDepartment();
			EmployeeRequest toPersist = EmployeeControllerIntegrationTests.this.employeeRequestTestFactory.builder()
				.birthday(null)
				.emailAddress(null)
				.firstName(null)
				.lastName(null)
				.departmentName(departmentResponse.departmentName())
				.create();

			String requestAsJson = transformRequestToJSONByView(toPersist, DataView.POST.class);
			String uri = "%s".formatted(EmployeeController.BASE_URI);

			// Act / Assert
			MvcResult mvcResult = EmployeeControllerIntegrationTests.this.mockMvc
				.perform(
						MockMvcRequestBuilders.post(uri).contentType(MediaType.APPLICATION_JSON).content(requestAsJson))
				.andExpect(MockMvcResultMatchers.status().isCreated())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.notNullValue()))
				.andExpect(MockMvcResultMatchers.header()
					.string(HttpHeaders.LOCATION, Matchers.containsString(EmployeeController.BASE_URI)))
				.andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.notNullValue()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.employeeId", Matchers.notNullValue()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.firstName", Matchers.nullValue()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.lastName", Matchers.nullValue()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.birthday", Matchers.nullValue()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.emailAddress", Matchers.nullValue()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.departmentName", Matchers.is(toPersist.departmentName())))
				.andReturn();
			String contentAsString = mvcResult.getResponse().getContentAsString();
			EmployeeResponse employeeResponse = EmployeeControllerIntegrationTests.this.objectMapper
				.readValue(contentAsString, EmployeeResponse.class);
			Optional<Employee> optionalEmployee = EmployeeControllerIntegrationTests.this.employeeRepository
				.findById(employeeResponse.employeeId());
			Assertions.assertThat(optionalEmployee)
				.hasValueSatisfying((Employee value) -> Assertions.assertThat(value).isNotNull());
		}

		@Test
		@DisplayName("POST: 'hhtps://.../employees' returns BAD REQUEST if the specified department name doesn't exist ")
		void givenUnknownDepartment_whenCreateEmployee_thenStatus400() throws Exception {
			// Arrange
			EmployeeRequest toPersist = EmployeeControllerIntegrationTests.this.employeeRequestTestFactory
				.createDefault();
			String requestAsJson = transformRequestToJSONByView(toPersist, DataView.POST.class);
			String uri = "%s".formatted(EmployeeController.BASE_URI);

			// Act / Assert
			EmployeeControllerIntegrationTests.this.mockMvc
				.perform(
						MockMvcRequestBuilders.post(uri).contentType(MediaType.APPLICATION_JSON).content(requestAsJson))
				.andExpect(MockMvcResultMatchers.status().isBadRequest())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.notNullValue()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.url", Matchers.notNullValue()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.httpMethod", Matchers.notNullValue()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.httpStatus", Matchers.is(HttpStatus.BAD_REQUEST.name())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.errorDateTime", Matchers.notNullValue()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.errorMessage", Matchers.notNullValue()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.errorMessage",
						Matchers.containsString("The department with the name [%s] could not be found!"
							.formatted(toPersist.departmentName()))));
		}

		@Test
		@DisplayName("POST: 'hhtps://.../employees' returns BAD REQUEST if the specified email is not valid ")
		void givenInvalidEmail_whenCreateEmployee_thenStatus400() throws Exception {
			// Arrange
			EmployeeRequest toPersist = EmployeeControllerIntegrationTests.this.employeeRequestTestFactory.builder()
				.emailAddress(RandomStringUtils.randomAlphabetic(10))
				.create();
			String requestAsJson = transformRequestToJSONByView(toPersist, DataView.POST.class);
			String uri = "%s".formatted(EmployeeController.BASE_URI);

			// Act / Assert
			EmployeeControllerIntegrationTests.this.mockMvc
				.perform(
						MockMvcRequestBuilders.post(uri).contentType(MediaType.APPLICATION_JSON).content(requestAsJson))
				.andExpect(MockMvcResultMatchers.status().isBadRequest())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.notNullValue()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.url", Matchers.notNullValue()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.httpMethod", Matchers.notNullValue()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.httpStatus", Matchers.is(HttpStatus.BAD_REQUEST.name())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.errorDateTime", Matchers.notNullValue()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.errorMessage", Matchers.notNullValue()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.errorMessage",
						Matchers.containsString("must match \"" + EmployeeRequest.EMAIL_REGEX + "\"")));
		}

		@Test
		@DisplayName("POST: 'hhtps://.../employees' returns BAD REQUEST if the specified birthday is not valid ")
		void givenInvalidBirthday_whenCreateEmployee_thenStatus400() throws Exception {
			// Arrange
			EmployeeRequest toPersist = EmployeeControllerIntegrationTests.this.employeeRequestTestFactory
				.createDefault();
			String requestAsJson = transformRequestToJSONByView(toPersist, DataView.POST.class);
			JSONObject jsonObject = new JSONObject(requestAsJson);
			String originalBirthday = jsonObject.getString("birthday");
			LocalDate date = LocalDate.parse(originalBirthday);
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMM dd, yyyy");
			String modifiedBirthday = date.format(formatter);
			jsonObject.put("birthday", modifiedBirthday);
			requestAsJson = jsonObject.toString();
			String uri = "%s".formatted(EmployeeController.BASE_URI);

			// Act / Assert
			EmployeeControllerIntegrationTests.this.mockMvc
				.perform(
						MockMvcRequestBuilders.post(uri).contentType(MediaType.APPLICATION_JSON).content(requestAsJson))
				.andExpect(MockMvcResultMatchers.status().isBadRequest())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.notNullValue()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.url", Matchers.notNullValue()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.httpMethod", Matchers.notNullValue()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.httpStatus", Matchers.is(HttpStatus.BAD_REQUEST.name())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.errorDateTime", Matchers.notNullValue()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.errorMessage", Matchers.notNullValue()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.errorMessage",
						Matchers.containsString("Not parseable date: [%s]. Expected format: [%s]!"
							.formatted(modifiedBirthday, UsableDateFormat.DEFAULT.getDateFormat()))));
		}

		@Test
		@DisplayName("POST: 'hhtps://.../employees' returns BAD REQUEST if the specified email already exists ")
		void givenAlreadyUsedEmail_whenCreateEmployee_thenStatus400() throws Exception {
			// Arrange
			DepartmentResponse departmentResponse = saveRandomDepartment();
			EmployeeRequest firstEmployeeRequest = EmployeeControllerIntegrationTests.this.employeeRequestTestFactory
				.builder()
				.departmentName(departmentResponse.departmentName())
				.create();

			String requestAsJson = transformRequestToJSONByView(firstEmployeeRequest, DataView.POST.class);
			String uri = "%s".formatted(EmployeeController.BASE_URI);
			EmployeeControllerIntegrationTests.this.mockMvc.perform(
					MockMvcRequestBuilders.post(uri).contentType(MediaType.APPLICATION_JSON).content(requestAsJson));

			EmployeeRequest secondEmployeeRequest = EmployeeControllerIntegrationTests.this.employeeRequestTestFactory
				.builder()
				.departmentName(departmentResponse.departmentName())
				.emailAddress(firstEmployeeRequest.emailAddress())
				.create();

			// Act / Assert
			EmployeeControllerIntegrationTests.this.mockMvc
				.perform(
						MockMvcRequestBuilders.post(uri).contentType(MediaType.APPLICATION_JSON).content(requestAsJson))
				.andExpect(MockMvcResultMatchers.status().isBadRequest())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.notNullValue()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.url", Matchers.notNullValue()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.httpMethod", Matchers.notNullValue()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.httpStatus", Matchers.is(HttpStatus.BAD_REQUEST.name())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.errorDateTime", Matchers.notNullValue()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.errorMessage", Matchers.notNullValue()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.errorMessage", Matchers.containsString(
						"The email address [%s] already exists!".formatted(secondEmployeeRequest.emailAddress()))));
		}

		@Test
		@DisplayName("POST: 'hhtps://.../employees' returns BAD REQUEST if no field is set")
		void givenEmptyRequest_whenCreateEmployee_thenStatus400() throws Exception {
			// Arrange
			EmployeeRequest toPersist = EmployeeControllerIntegrationTests.this.employeeRequestTestFactory.builder()
				.birthday(null)
				.emailAddress(null)
				.firstName(null)
				.lastName(null)
				.departmentName(null)
				.create();

			String requestAsJson = transformRequestToJSONByView(toPersist, DataView.POST.class);
			String uri = "%s".formatted(EmployeeController.BASE_URI);

			// Act / Assert
			EmployeeControllerIntegrationTests.this.mockMvc
				.perform(
						MockMvcRequestBuilders.post(uri).contentType(MediaType.APPLICATION_JSON).content(requestAsJson))
				.andExpect(MockMvcResultMatchers.status().isBadRequest())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.notNullValue()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.url", Matchers.notNullValue()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.httpMethod", Matchers.notNullValue()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.httpStatus", Matchers.is(HttpStatus.BAD_REQUEST.name())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.errorDateTime", Matchers.notNullValue()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.errorMessage", Matchers.notNullValue()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.errorMessage",
						Matchers.containsString("The employee department name must not be blank!")));
		}

	}

	@Nested
	@DisplayName("when access")
	class WhenAccess {

		@Test
		@DisplayName("GET: 'https://.../employees/{departmentId}' returns NOT FOUND for unknown departmentId ")
		void givenUnknownId_whenFindById_thenStatus404() throws Exception {
			// Arrange
			String unknownId = UUID.randomUUID().toString();
			String uri = "%s/{departmentId}".formatted(EmployeeController.BASE_URI);

			// Act / Assert
			EmployeeControllerIntegrationTests.this.mockMvc
				.perform(MockMvcRequestBuilders.get(uri, unknownId).contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isNotFound())
				.andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.notNullValue()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.errorMessage", Matchers
					.containsString("The employee with the ID [%s] could not be found!".formatted(unknownId))));
		}

		@Test
		@DisplayName("GET: 'https://.../employees/{departmentId}' returns OK and valid employee response")
		void givenEmployee_whenFindById_thenStatus200AndReturnValidEmployeeResponse() throws Exception {
			// Arrange
			EmployeeResponse persisted = saveRandomEmployees(1).get(0);
			String uri = "%s/{departmentId}".formatted(EmployeeController.BASE_URI);
			String formattedBirthday = EmployeeControllerIntegrationTests.this.dateFormatter
				.format(persisted.birthday());

			// Act / Assert
			MvcResult mvcResult = EmployeeControllerIntegrationTests.this.mockMvc
				.perform(
						MockMvcRequestBuilders.get(uri, persisted.employeeId()).contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.notNullValue()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.departmentName", Matchers.is(persisted.departmentName())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.employeeId", Matchers.is(persisted.employeeId())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.birthday", Matchers.is(formattedBirthday)))
				.andExpect(MockMvcResultMatchers.jsonPath("$.firstName", Matchers.is(persisted.firstName())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.lastName", Matchers.is(persisted.lastName())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.departmentName", Matchers.is(persisted.departmentName())))
				.andReturn();

			String contentAsString = mvcResult.getResponse().getContentAsString();
			EmployeeResponse employeeResponse = EmployeeControllerIntegrationTests.this.objectMapper
				.readValue(contentAsString, EmployeeResponse.class);
			Assertions.assertThat(employeeResponse).isNotNull();
			Assertions.assertThat(employeeResponse.employeeId()).isEqualTo(persisted.employeeId());
			Assertions.assertThat(employeeResponse.birthday()).isEqualTo(persisted.birthday());
			Assertions.assertThat(employeeResponse.firstName()).isEqualTo(persisted.firstName());
			Assertions.assertThat(employeeResponse.lastName()).isEqualTo(persisted.lastName());
			Assertions.assertThat(employeeResponse.departmentName()).isEqualTo(persisted.departmentName());
		}

		@Test
		@DisplayName("GET: 'https://.../employees/{departmentId}' returns OK and valid employee response on multiple employees")
		void givenEmployees_whenFindById_thenStatus200AndReturnValidEmployeeResponse() throws Exception {
			// Arrange
			int count = 10;
			EmployeeResponse persisted = saveRandomEmployees(count).get(RandomUtils.nextInt(0, count - 1));
			String uri = "%s/{departmentId}".formatted(EmployeeController.BASE_URI);

			// Act / Assert
			EmployeeControllerIntegrationTests.this.mockMvc
				.perform(
						MockMvcRequestBuilders.get(uri, persisted.employeeId()).contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.notNullValue()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.departmentName", Matchers.is(persisted.departmentName())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.employeeId", Matchers.is(persisted.employeeId())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.birthday",
						Matchers
							.is(EmployeeControllerIntegrationTests.this.dateFormatter.format(persisted.birthday()))))
				.andExpect(MockMvcResultMatchers.jsonPath("$.firstName", Matchers.is(persisted.firstName())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.lastName", Matchers.is(persisted.lastName())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.departmentName", Matchers.is(persisted.departmentName())));
		}

		@Test
		@DisplayName("GET: 'https://.../employees' returns OK and all employees")
		void givenEmployees_whenFindAll_thenStatus200AndReturnAll() throws Exception {
			// Arrange
			List<EmployeeResponse> employeeResponses = saveRandomEmployees(RandomUtils.nextInt(10, 20));
			String uri = "%s".formatted(EmployeeController.BASE_URI);

			// Act / Assert
			EmployeeControllerIntegrationTests.this.mockMvc
				.perform(MockMvcRequestBuilders.get(uri).contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.notNullValue()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.content", Matchers.hasSize(employeeResponses.size())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.content[*].emailAddress").exists())
				.andExpect(MockMvcResultMatchers.jsonPath("$.content[*].firstName").exists())
				.andExpect(MockMvcResultMatchers.jsonPath("$.content[*].lastName").exists())
				.andExpect(MockMvcResultMatchers.jsonPath("$.content[*].birthday").exists())
				.andExpect(MockMvcResultMatchers.jsonPath("$.content[*].departmentName").exists())
				.andExpect(MockMvcResultMatchers.jsonPath("$.content[*].employeeId").exists());
		}

		@Test
		@DisplayName("GET: 'https://.../employees/{departmentId}/revisions succeeds on existing employee")
		void givenExistingEmployee_whenFindRevisions_thenStatusOkAndReturnPageOfRevisions() throws Exception {
			// Arrange
			DepartmentResponse departmentResponse = saveRandomDepartment();

			EmployeeRequest createEmployeeRequest = EmployeeControllerIntegrationTests.this.employeeRequestTestFactory
				.builder()
				.departmentName(departmentResponse.departmentName())
				.create();
			String requestAsJson = transformRequestToJSONByView(createEmployeeRequest, DataView.POST.class);
			String createUri = "%s".formatted(EmployeeController.BASE_URI);
			MvcResult mvcResult = EmployeeControllerIntegrationTests.this.mockMvc
				.perform(MockMvcRequestBuilders.post(createUri)
					.contentType(MediaType.APPLICATION_JSON)
					.content(requestAsJson))
				.andReturn();
			EmployeeResponse persistedEmployeeResponse = EmployeeControllerIntegrationTests.this.objectMapper
				.readValue(mvcResult.getResponse().getContentAsString(), EmployeeResponse.class);

			String newFirstName = RandomStringUtils.randomAlphabetic(23);
			EmployeeRequest updateEmployeeRequest = EmployeeControllerIntegrationTests.this.employeeRequestTestFactory
				.builder()
				.departmentName(null)
				.emailAddress(null)
				.firstName(newFirstName)
				.lastName(null)
				.birthday(null)
				.create();
			String updateRequestAsJson = transformRequestToJSONByView(updateEmployeeRequest, DataView.PATCH.class);
			String patchUri = "%s/{departmentId}".formatted(EmployeeController.BASE_URI);
			EmployeeControllerIntegrationTests.this.mockMvc
				.perform(MockMvcRequestBuilders.patch(patchUri, persistedEmployeeResponse.employeeId())
					.contentType(MediaType.APPLICATION_JSON)
					.content(updateRequestAsJson));

			String lastName = RandomStringUtils.randomAlphabetic(23);
			updateEmployeeRequest = EmployeeControllerIntegrationTests.this.employeeRequestTestFactory.builder()
				.departmentName(null)
				.emailAddress(null)
				.firstName(null)
				.lastName(lastName)
				.birthday(null)
				.create();
			updateRequestAsJson = transformRequestToJSONByView(updateEmployeeRequest, DataView.PATCH.class);
			EmployeeControllerIntegrationTests.this.mockMvc
				.perform(MockMvcRequestBuilders.patch(patchUri, persistedEmployeeResponse.employeeId())
					.contentType(MediaType.APPLICATION_JSON)
					.content(updateRequestAsJson));

			String deleteUri = "%s/{departmentId}".formatted(EmployeeController.BASE_URI);
			EmployeeControllerIntegrationTests.this.mockMvc
				.perform(MockMvcRequestBuilders.delete(deleteUri, persistedEmployeeResponse.employeeId())
					.contentType(MediaType.APPLICATION_JSON));

			String revisionUri = "%s/{departmentId}/revisions".formatted(EmployeeController.BASE_URI);

			// Act / Assert
			EmployeeControllerIntegrationTests.this.mockMvc
				.perform(MockMvcRequestBuilders.get(revisionUri, persistedEmployeeResponse.employeeId())
					.contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.notNullValue()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.content", Matchers.hasSize(4)))
				.andExpect(MockMvcResultMatchers.jsonPath("$.content[0].metadata.revisionType", Matchers.is("INSERT")))
				.andExpect(MockMvcResultMatchers.jsonPath("$.content[1].metadata.revisionType", Matchers.is("UPDATE")))
				.andExpect(MockMvcResultMatchers.jsonPath("$.content[2].metadata.revisionType", Matchers.is("UPDATE")))
				.andExpect(MockMvcResultMatchers.jsonPath("$.content[3].metadata.revisionType", Matchers.is("DELETE")));

		}

		@Test
		@DisplayName("GET: 'https://.../employees/{departmentId}/revisions/latest returns 404 for unknown departmentId")
		void givenUnknownId_whenFindLatestRevision_thenStatus404AndErrorMessage() throws Exception {
			// Arrange
			String unknownId = UUID.randomUUID().toString();
			String revisionUri = "%s/{departmentId}/revisions/latest".formatted(EmployeeController.BASE_URI);

			// Act / Assert
			EmployeeControllerIntegrationTests.this.mockMvc
				.perform(MockMvcRequestBuilders.get(revisionUri, unknownId).contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isNotFound())
				.andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.notNullValue()))
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.notNullValue()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.url", Matchers.notNullValue()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.httpMethod", Matchers.notNullValue()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.httpStatus", Matchers.is(HttpStatus.NOT_FOUND.name())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.errorDateTime", Matchers.notNullValue()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.errorMessage", Matchers.notNullValue()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.errorMessage", Matchers.containsString(
						"The latest revision for the employee with ID [%s] could not be found!".formatted(unknownId))));

		}

		@Test
		@DisplayName("GET: 'https://.../employees/{departmentId}/revisions/latest succeeds on existing employee")
		void givenExistingEmployee_whenFindLatestRevision_thenStatusOkAndReturnLatestRevision() throws Exception {
			// Arrange
			DepartmentResponse departmentResponse = saveRandomDepartment();

			EmployeeRequest createEmployeeRequest = EmployeeControllerIntegrationTests.this.employeeRequestTestFactory
				.builder()
				.departmentName(departmentResponse.departmentName())
				.create();
			String requestAsJson = transformRequestToJSONByView(createEmployeeRequest, DataView.POST.class);
			String createUri = "%s".formatted(EmployeeController.BASE_URI);
			MvcResult mvcResult = EmployeeControllerIntegrationTests.this.mockMvc
				.perform(MockMvcRequestBuilders.post(createUri)
					.contentType(MediaType.APPLICATION_JSON)
					.content(requestAsJson))
				.andReturn();
			EmployeeResponse persistedEmployeeResponse = EmployeeControllerIntegrationTests.this.objectMapper
				.readValue(mvcResult.getResponse().getContentAsString(), EmployeeResponse.class);

			String newFirstName = RandomStringUtils.randomAlphabetic(23);
			EmployeeRequest updateEmployeeRequest = EmployeeControllerIntegrationTests.this.employeeRequestTestFactory
				.builder()
				.departmentName(null)
				.emailAddress(null)
				.firstName(newFirstName)
				.lastName(null)
				.birthday(null)
				.create();
			String updateRequestAsJson = transformRequestToJSONByView(updateEmployeeRequest, DataView.PATCH.class);
			String patchUri = "%s/{departmentId}".formatted(EmployeeController.BASE_URI);
			EmployeeControllerIntegrationTests.this.mockMvc
				.perform(MockMvcRequestBuilders.patch(patchUri, persistedEmployeeResponse.employeeId())
					.contentType(MediaType.APPLICATION_JSON)
					.content(updateRequestAsJson));

			String revisionUri = "%s/{departmentId}/revisions/latest".formatted(EmployeeController.BASE_URI);

			// Act / Assert
			EmployeeControllerIntegrationTests.this.mockMvc
				.perform(MockMvcRequestBuilders.get(revisionUri, persistedEmployeeResponse.employeeId())
					.contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.notNullValue()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.metadata.revisionType", Matchers.is("UPDATE")))
				.andExpect(MockMvcResultMatchers.jsonPath("$.metadata.revisionNumber").isNotEmpty());
		}

	}

	@Nested
	@DisplayName("when partial update")
	class WhenPartialUpdate {

		@Test
		@DisplayName("PATCH: 'hhtps://.../employees/{departmentId}' returns NOT FOUND if the specified employee doesn't exist ")
		void givenUnknownEmployeeId_whenPartialUpdateEmployee_thenStatus404() throws Exception {
			// Arrange
			EmployeeRequest updateRequest = EmployeeControllerIntegrationTests.this.employeeRequestTestFactory
				.createDefault();
			String uri = "%s/{departmentId}".formatted(EmployeeController.BASE_URI);
			String requestAsJson = transformRequestToJSONByView(updateRequest, DataView.PATCH.class);
			String wrongId = UUID.randomUUID().toString();

			// Act / Assert
			EmployeeControllerIntegrationTests.this.mockMvc
				.perform(MockMvcRequestBuilders.patch(uri, wrongId)
					.contentType(MediaType.APPLICATION_JSON)
					.content(requestAsJson))
				.andExpect(MockMvcResultMatchers.status().isNotFound())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.notNullValue()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.url", Matchers.notNullValue()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.httpMethod", Matchers.notNullValue()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.httpStatus", Matchers.is(HttpStatus.NOT_FOUND.name())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.errorDateTime", Matchers.notNullValue()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.errorMessage", Matchers.notNullValue()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.errorMessage", Matchers
					.containsString("The employee with the ID [%s] could not be found!".formatted(wrongId))));
		}

		@Test
		@DisplayName("PATCH: 'hhtps://.../employees/{departmentId}' returns BAD REQUEST if the specified department doesn't exist ")
		void givenUnknownDepartment_whenPartialUpdateEmployee_thenStatus400() throws Exception {
			// Arrange
			DepartmentResponse departmentResponse = saveRandomDepartment();
			EmployeeRequest initialEmployeeRequest = EmployeeControllerIntegrationTests.this.employeeRequestTestFactory
				.builder()
				.departmentName(departmentResponse.departmentName())
				.create();
			String requestAsJson = transformRequestToJSONByView(initialEmployeeRequest, DataView.POST.class);
			String createUri = "%s".formatted(EmployeeController.BASE_URI);
			MvcResult mvcResult = EmployeeControllerIntegrationTests.this.mockMvc
				.perform(MockMvcRequestBuilders.post(createUri)
					.contentType(MediaType.APPLICATION_JSON)
					.content(requestAsJson))
				.andReturn();
			EmployeeResponse employeeResponse = EmployeeControllerIntegrationTests.this.objectMapper
				.readValue(mvcResult.getResponse().getContentAsString(), EmployeeResponse.class);

			String newDepartmentName = RandomStringUtils.randomAlphabetic(10);
			EmployeeRequest updateEmployeeRequest = EmployeeControllerIntegrationTests.this.employeeRequestTestFactory
				.builder()
				.departmentName(newDepartmentName)
				.create();
			String updateRequestAsJson = transformRequestToJSONByView(updateEmployeeRequest, DataView.PATCH.class);
			String patchUri = "%s/{departmentId}".formatted(EmployeeController.BASE_URI);

			// Act / Assert
			EmployeeControllerIntegrationTests.this.mockMvc
				.perform(MockMvcRequestBuilders.patch(patchUri, employeeResponse.employeeId())
					.contentType(MediaType.APPLICATION_JSON)
					.content(updateRequestAsJson))
				.andExpect(MockMvcResultMatchers.status().isBadRequest())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.notNullValue()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.url", Matchers.notNullValue()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.httpMethod", Matchers.notNullValue()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.httpStatus", Matchers.is(HttpStatus.BAD_REQUEST.name())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.errorDateTime", Matchers.notNullValue()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.errorMessage", Matchers.notNullValue()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.errorMessage", Matchers.containsString(
						"The department with the name [%s] could not be found!".formatted(newDepartmentName))));
		}

		@Test
		@DisplayName("PATCH: 'hhtps://.../employees/{departmentId} returns NO CONTENT if the specified request (all fields set) is valid ")
		void givenValidRequestWithAllFieldsSet_whenPartialUpdateEmployee_thenStatus204() throws Exception {
			// Arrange
			DepartmentResponse departmentResponse = saveRandomDepartment();
			EmployeeRequest initialEmployeeRequest = EmployeeControllerIntegrationTests.this.employeeRequestTestFactory
				.builder()
				.departmentName(departmentResponse.departmentName())
				.create();
			String requestAsJson = transformRequestToJSONByView(initialEmployeeRequest, DataView.POST.class);
			String createUri = "%s".formatted(EmployeeController.BASE_URI);
			MvcResult mvcResult = EmployeeControllerIntegrationTests.this.mockMvc
				.perform(MockMvcRequestBuilders.post(createUri)
					.contentType(MediaType.APPLICATION_JSON)
					.content(requestAsJson))
				.andReturn();
			EmployeeResponse employeeResponse = EmployeeControllerIntegrationTests.this.objectMapper
				.readValue(mvcResult.getResponse().getContentAsString(), EmployeeResponse.class);

			DepartmentResponse otherDepartmentResponse = saveRandomDepartment();
			EmployeeRequest updateEmployeeRequest = EmployeeControllerIntegrationTests.this.employeeRequestTestFactory
				.builder()
				.departmentName(otherDepartmentResponse.departmentName())
				.create();
			String updateRequestAsJson = transformRequestToJSONByView(updateEmployeeRequest, DataView.PATCH.class);
			String patchUri = "%s/{departmentId}".formatted(EmployeeController.BASE_URI);

			// Act / Assert
			EmployeeControllerIntegrationTests.this.mockMvc
				.perform(MockMvcRequestBuilders.patch(patchUri, employeeResponse.employeeId())
					.contentType(MediaType.APPLICATION_JSON)
					.content(updateRequestAsJson))
				.andExpect(MockMvcResultMatchers.status().isNoContent());

			Employee updateEmployee = EmployeeControllerIntegrationTests.this.employeeRepository
				.findById(employeeResponse.employeeId())
				.orElseThrow();
			Assertions
				.assertThat(EmployeeControllerIntegrationTests.this.dateFormatter.format(updateEmployee.getBirthday()))
				.isEqualTo(
						EmployeeControllerIntegrationTests.this.dateFormatter.format(updateEmployeeRequest.birthday()));
			Assertions.assertThat(updateEmployee.getFullName().getFirstName())
				.isEqualTo(updateEmployeeRequest.firstName());
			Assertions.assertThat(updateEmployee.getFullName().getLastName())
				.isEqualTo(updateEmployeeRequest.lastName());
			Assertions.assertThat(updateEmployee.getEmailAddress()).isEqualTo(updateEmployeeRequest.emailAddress());
			Assertions.assertThat(updateEmployee.getDepartment().getDepartmentName())
				.isEqualTo(updateEmployeeRequest.departmentName());
		}

		@Test
		@DisplayName("PATCH: 'hhtps://.../employees/{departmentId} returns NO CONTENT on only updating birthday")
		void givenNewBirthDay_whenPartialUpdateEmployee_thenStatus204andUpdateOnlyBirthDay() throws Exception {
			// Arrange
			DepartmentResponse departmentResponse = saveRandomDepartment();
			EmployeeRequest initialEmployeeRequest = EmployeeControllerIntegrationTests.this.employeeRequestTestFactory
				.builder()
				.departmentName(departmentResponse.departmentName())
				.create();
			String requestAsJson = transformRequestToJSONByView(initialEmployeeRequest, DataView.POST.class);
			String createUri = "%s".formatted(EmployeeController.BASE_URI);
			MvcResult mvcResult = EmployeeControllerIntegrationTests.this.mockMvc
				.perform(MockMvcRequestBuilders.post(createUri)
					.contentType(MediaType.APPLICATION_JSON)
					.content(requestAsJson))
				.andReturn();
			EmployeeResponse employeeResponse = EmployeeControllerIntegrationTests.this.objectMapper
				.readValue(mvcResult.getResponse().getContentAsString(), EmployeeResponse.class);

			LocalDate localDate = LocalDate.parse("1979-12-03", EmployeeControllerIntegrationTests.this.dateFormatter);
			ZonedDateTime newBirthDay = localDate.atStartOfDay(ZoneId.systemDefault());
			EmployeeRequest updateEmployeeRequest = EmployeeControllerIntegrationTests.this.employeeRequestTestFactory
				.builder()
				.departmentName(null)
				.emailAddress(null)
				.firstName(null)
				.lastName(null)
				.birthday(newBirthDay)
				.create();
			String updateRequestAsJson = transformRequestToJSONByView(updateEmployeeRequest, DataView.PATCH.class);
			String patchUri = "%s/{departmentId}".formatted(EmployeeController.BASE_URI);

			// Act / Assert
			EmployeeControllerIntegrationTests.this.mockMvc
				.perform(MockMvcRequestBuilders.patch(patchUri, employeeResponse.employeeId())
					.contentType(MediaType.APPLICATION_JSON)
					.content(updateRequestAsJson))
				.andExpect(MockMvcResultMatchers.status().isNoContent());

			Employee updateEmployee = EmployeeControllerIntegrationTests.this.employeeRepository
				.findById(employeeResponse.employeeId())
				.orElseThrow();
			Assertions
				.assertThat(EmployeeControllerIntegrationTests.this.dateFormatter.format(updateEmployee.getBirthday()))
				.isEqualTo(
						EmployeeControllerIntegrationTests.this.dateFormatter.format(updateEmployeeRequest.birthday()));
			Assertions.assertThat(updateEmployee.getFullName().getFirstName()).isEqualTo(employeeResponse.firstName());
			Assertions.assertThat(updateEmployee.getFullName().getLastName()).isEqualTo(employeeResponse.lastName());
			Assertions.assertThat(updateEmployee.getEmailAddress()).isEqualTo(employeeResponse.emailAddress());
			Assertions.assertThat(updateEmployee.getDepartment().getDepartmentName())
				.isEqualTo(employeeResponse.departmentName());

		}

		@Test
		@DisplayName("PATCH: 'hhtps://.../employees/{departmentId} returns NO CONTENT on only updating first name")
		void givenNewFirstName_whenPartialUpdateEmployee_thenStatus204andUpdateOnlyFirstName() throws Exception {
			// Arrange
			DepartmentResponse departmentResponse = saveRandomDepartment();
			EmployeeRequest initialEmployeeRequest = EmployeeControllerIntegrationTests.this.employeeRequestTestFactory
				.builder()
				.departmentName(departmentResponse.departmentName())
				.create();
			String requestAsJson = transformRequestToJSONByView(initialEmployeeRequest, DataView.POST.class);
			String createUri = "%s".formatted(EmployeeController.BASE_URI);
			MvcResult mvcResult = EmployeeControllerIntegrationTests.this.mockMvc
				.perform(MockMvcRequestBuilders.post(createUri)
					.contentType(MediaType.APPLICATION_JSON)
					.content(requestAsJson))
				.andReturn();
			EmployeeResponse employeeResponse = EmployeeControllerIntegrationTests.this.objectMapper
				.readValue(mvcResult.getResponse().getContentAsString(), EmployeeResponse.class);
			String newFirstName = RandomStringUtils.randomAlphabetic(23);
			EmployeeRequest updateEmployeeRequest = EmployeeControllerIntegrationTests.this.employeeRequestTestFactory
				.builder()
				.departmentName(null)
				.emailAddress(null)
				.firstName(newFirstName)
				.lastName(null)
				.birthday(null)
				.create();
			String updateRequestAsJson = transformRequestToJSONByView(updateEmployeeRequest, DataView.PATCH.class);
			String patchUri = "%s/{departmentId}".formatted(EmployeeController.BASE_URI);

			// Act / Assert
			EmployeeControllerIntegrationTests.this.mockMvc
				.perform(MockMvcRequestBuilders.patch(patchUri, employeeResponse.employeeId())
					.contentType(MediaType.APPLICATION_JSON)
					.content(updateRequestAsJson))
				.andExpect(MockMvcResultMatchers.status().isNoContent());

			Employee updateEmployee = EmployeeControllerIntegrationTests.this.employeeRepository
				.findById(employeeResponse.employeeId())
				.orElseThrow();
			Assertions
				.assertThat(EmployeeControllerIntegrationTests.this.dateFormatter.format(updateEmployee.getBirthday()))
				.isEqualTo(EmployeeControllerIntegrationTests.this.dateFormatter.format(employeeResponse.birthday()));
			Assertions.assertThat(updateEmployee.getFullName().getFirstName())
				.isEqualTo(updateEmployeeRequest.firstName());
			Assertions.assertThat(updateEmployee.getFullName().getLastName()).isEqualTo(employeeResponse.lastName());
			Assertions.assertThat(updateEmployee.getEmailAddress()).isEqualTo(employeeResponse.emailAddress());
			Assertions.assertThat(updateEmployee.getDepartment().getDepartmentName())
				.isEqualTo(employeeResponse.departmentName());

		}

		@Test
		@DisplayName("PATCH: 'hhtps://.../employees/{departmentId} returns BAD REQUEST on only updating first name")
		void givenBlankFirstName_whenPartialUpdateEmployee_thenStatus400() throws Exception {
			// Arrange
			DepartmentResponse departmentResponse = saveRandomDepartment();
			EmployeeRequest initialEmployeeRequest = EmployeeControllerIntegrationTests.this.employeeRequestTestFactory
				.builder()
				.departmentName(departmentResponse.departmentName())
				.create();
			String requestAsJson = transformRequestToJSONByView(initialEmployeeRequest, DataView.POST.class);
			String createUri = "%s".formatted(EmployeeController.BASE_URI);
			MvcResult mvcResult = EmployeeControllerIntegrationTests.this.mockMvc
				.perform(MockMvcRequestBuilders.post(createUri)
					.contentType(MediaType.APPLICATION_JSON)
					.content(requestAsJson))
				.andReturn();
			EmployeeResponse employeeResponse = EmployeeControllerIntegrationTests.this.objectMapper
				.readValue(mvcResult.getResponse().getContentAsString(), EmployeeResponse.class);
			EmployeeRequest updateEmployeeRequest = EmployeeControllerIntegrationTests.this.employeeRequestTestFactory
				.builder()
				.departmentName(null)
				.emailAddress(null)
				.firstName("   ")
				.lastName(null)
				.birthday(null)
				.create();
			String updateRequestAsJson = transformRequestToJSONByView(updateEmployeeRequest, DataView.PATCH.class);
			String patchUri = "%s/{departmentId}".formatted(EmployeeController.BASE_URI);

			// Act / Assert
			EmployeeControllerIntegrationTests.this.mockMvc
				.perform(MockMvcRequestBuilders.patch(patchUri, employeeResponse.employeeId())
					.contentType(MediaType.APPLICATION_JSON)
					.content(updateRequestAsJson))
				.andExpect(MockMvcResultMatchers.status().isBadRequest())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.notNullValue()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.url", Matchers.notNullValue()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.httpMethod", Matchers.notNullValue()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.httpStatus", Matchers.is(HttpStatus.BAD_REQUEST.name())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.errorDateTime", Matchers.notNullValue()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.errorMessage", Matchers.notNullValue()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.errorMessage", Matchers.containsString(
						"firstName: The value: [   ] must either be 'null' or contain a non empty trimmed value")));
		}

		@Test
		@DisplayName("PATCH: 'hhtps://.../employees/{departmentId} returns NO CONTENT on only updating last name")
		void givenNewLastName_whenPartialUpdateEmployee_thenStatus204andUpdateOnlyLastName() throws Exception {
			// Arrange
			DepartmentResponse departmentResponse = saveRandomDepartment();
			EmployeeRequest initialEmployeeRequest = EmployeeControllerIntegrationTests.this.employeeRequestTestFactory
				.builder()
				.departmentName(departmentResponse.departmentName())
				.create();
			String requestAsJson = transformRequestToJSONByView(initialEmployeeRequest, DataView.POST.class);
			String createUri = "%s".formatted(EmployeeController.BASE_URI);
			MvcResult mvcResult = EmployeeControllerIntegrationTests.this.mockMvc
				.perform(MockMvcRequestBuilders.post(createUri)
					.contentType(MediaType.APPLICATION_JSON)
					.content(requestAsJson))
				.andReturn();
			EmployeeResponse employeeResponse = EmployeeControllerIntegrationTests.this.objectMapper
				.readValue(mvcResult.getResponse().getContentAsString(), EmployeeResponse.class);
			String newLastName = RandomStringUtils.randomAlphabetic(23);
			EmployeeRequest updateEmployeeRequest = EmployeeControllerIntegrationTests.this.employeeRequestTestFactory
				.builder()
				.departmentName(null)
				.emailAddress(null)
				.firstName(null)
				.lastName(newLastName)
				.birthday(null)
				.create();
			String updateRequestAsJson = transformRequestToJSONByView(updateEmployeeRequest, DataView.PATCH.class);
			String patchUri = "%s/{departmentId}".formatted(EmployeeController.BASE_URI);

			// Act / Assert
			EmployeeControllerIntegrationTests.this.mockMvc
				.perform(MockMvcRequestBuilders.patch(patchUri, employeeResponse.employeeId())
					.contentType(MediaType.APPLICATION_JSON)
					.content(updateRequestAsJson))
				.andExpect(MockMvcResultMatchers.status().isNoContent());

			Employee updateEmployee = EmployeeControllerIntegrationTests.this.employeeRepository
				.findById(employeeResponse.employeeId())
				.orElseThrow();
			Assertions
				.assertThat(EmployeeControllerIntegrationTests.this.dateFormatter.format(updateEmployee.getBirthday()))
				.isEqualTo(EmployeeControllerIntegrationTests.this.dateFormatter.format(employeeResponse.birthday()));
			Assertions.assertThat(updateEmployee.getFullName().getFirstName()).isEqualTo(employeeResponse.firstName());
			Assertions.assertThat(updateEmployee.getFullName().getLastName())
				.isEqualTo(updateEmployeeRequest.lastName());
			Assertions.assertThat(updateEmployee.getEmailAddress()).isEqualTo(employeeResponse.emailAddress());
			Assertions.assertThat(updateEmployee.getDepartment().getDepartmentName())
				.isEqualTo(employeeResponse.departmentName());

		}

		@Test
		@DisplayName("PATCH: 'hhtps://.../employees/{departmentId} returns NO CONTENT on only updating email")
		void givenNewEmail_whenPartialUpdateEmployee_thenStatus204andUpdateOnlyEmail() throws Exception {
			// Arrange
			DepartmentResponse departmentResponse = saveRandomDepartment();
			EmployeeRequest initialEmployeeRequest = EmployeeControllerIntegrationTests.this.employeeRequestTestFactory
				.builder()
				.departmentName(departmentResponse.departmentName())
				.create();
			String requestAsJson = transformRequestToJSONByView(initialEmployeeRequest, DataView.POST.class);
			String createUri = "%s".formatted(EmployeeController.BASE_URI);
			MvcResult mvcResult = EmployeeControllerIntegrationTests.this.mockMvc
				.perform(MockMvcRequestBuilders.post(createUri)
					.contentType(MediaType.APPLICATION_JSON)
					.content(requestAsJson))
				.andReturn();
			EmployeeResponse employeeResponse = EmployeeControllerIntegrationTests.this.objectMapper
				.readValue(mvcResult.getResponse().getContentAsString(), EmployeeResponse.class);
			String newEmail = generateRandomEmail();
			EmployeeRequest updateEmployeeRequest = EmployeeControllerIntegrationTests.this.employeeRequestTestFactory
				.builder()
				.departmentName(null)
				.emailAddress(newEmail)
				.firstName(null)
				.lastName(null)
				.birthday(null)
				.create();
			String updateRequestAsJson = transformRequestToJSONByView(updateEmployeeRequest, DataView.PATCH.class);
			String patchUri = "%s/{departmentId}".formatted(EmployeeController.BASE_URI);

			// Act / Assert
			EmployeeControllerIntegrationTests.this.mockMvc
				.perform(MockMvcRequestBuilders.patch(patchUri, employeeResponse.employeeId())
					.contentType(MediaType.APPLICATION_JSON)
					.content(updateRequestAsJson))
				.andExpect(MockMvcResultMatchers.status().isNoContent());

			Employee updateEmployee = EmployeeControllerIntegrationTests.this.employeeRepository
				.findById(employeeResponse.employeeId())
				.orElseThrow();
			Assertions
				.assertThat(EmployeeControllerIntegrationTests.this.dateFormatter.format(updateEmployee.getBirthday()))
				.isEqualTo(EmployeeControllerIntegrationTests.this.dateFormatter.format(employeeResponse.birthday()));
			Assertions.assertThat(updateEmployee.getFullName().getFirstName()).isEqualTo(employeeResponse.firstName());
			Assertions.assertThat(updateEmployee.getFullName().getLastName()).isEqualTo(employeeResponse.lastName());
			Assertions.assertThat(updateEmployee.getEmailAddress()).isEqualTo(updateEmployeeRequest.emailAddress());
			Assertions.assertThat(updateEmployee.getDepartment().getDepartmentName())
				.isEqualTo(employeeResponse.departmentName());

		}

		@Test
		@DisplayName("PATCH: 'hhtps://.../employees/{departmentId} returns BAD REQUEST on invalid email")
		void givenNewInvalidEmail_whenPartialUpdateEmployee_thenStatus400() throws Exception {
			// Arrange
			DepartmentResponse departmentResponse = saveRandomDepartment();
			EmployeeRequest initialEmployeeRequest = EmployeeControllerIntegrationTests.this.employeeRequestTestFactory
				.builder()
				.departmentName(departmentResponse.departmentName())
				.create();
			String requestAsJson = transformRequestToJSONByView(initialEmployeeRequest, DataView.POST.class);
			String createUri = "%s".formatted(EmployeeController.BASE_URI);
			MvcResult mvcResult = EmployeeControllerIntegrationTests.this.mockMvc
				.perform(MockMvcRequestBuilders.post(createUri)
					.contentType(MediaType.APPLICATION_JSON)
					.content(requestAsJson))
				.andReturn();
			EmployeeResponse employeeResponse = EmployeeControllerIntegrationTests.this.objectMapper
				.readValue(mvcResult.getResponse().getContentAsString(), EmployeeResponse.class);
			String newEmail = RandomStringUtils.randomAlphabetic(23);
			EmployeeRequest updateEmployeeRequest = EmployeeControllerIntegrationTests.this.employeeRequestTestFactory
				.builder()
				.departmentName(null)
				.emailAddress(newEmail)
				.firstName(null)
				.lastName(null)
				.birthday(null)
				.create();
			String updateRequestAsJson = transformRequestToJSONByView(updateEmployeeRequest, DataView.PATCH.class);
			String patchUri = "%s/{departmentId}".formatted(EmployeeController.BASE_URI);

			// Act / Assert
			EmployeeControllerIntegrationTests.this.mockMvc
				.perform(MockMvcRequestBuilders.patch(patchUri, employeeResponse.employeeId())
					.contentType(MediaType.APPLICATION_JSON)
					.content(updateRequestAsJson))
				.andExpect(MockMvcResultMatchers.status().isBadRequest())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.notNullValue()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.url", Matchers.notNullValue()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.httpMethod", Matchers.notNullValue()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.httpStatus", Matchers.is(HttpStatus.BAD_REQUEST.name())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.errorDateTime", Matchers.notNullValue()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.errorMessage", Matchers.notNullValue()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.errorMessage",
						Matchers.containsString("must match \"" + EmployeeRequest.EMAIL_REGEX + "\"")));

		}

		@Test
		@DisplayName("PATCH: 'hhtps://.../employees/{departmentId} returns NO CONTENT on only updating department")
		void givenNewDepartment_whenPartialUpdateEmployee_thenStatus204andUpdateOnlyDeparmtent() throws Exception {
			// Arrange
			DepartmentResponse departmentResponse = saveRandomDepartment();
			EmployeeRequest initialEmployeeRequest = EmployeeControllerIntegrationTests.this.employeeRequestTestFactory
				.builder()
				.departmentName(departmentResponse.departmentName())
				.create();
			String requestAsJson = transformRequestToJSONByView(initialEmployeeRequest, DataView.POST.class);
			String createUri = "%s".formatted(EmployeeController.BASE_URI);
			MvcResult mvcResult = EmployeeControllerIntegrationTests.this.mockMvc
				.perform(MockMvcRequestBuilders.post(createUri)
					.contentType(MediaType.APPLICATION_JSON)
					.content(requestAsJson))
				.andReturn();
			EmployeeResponse employeeResponse = EmployeeControllerIntegrationTests.this.objectMapper
				.readValue(mvcResult.getResponse().getContentAsString(), EmployeeResponse.class);

			DepartmentResponse newDepartmentResponse = saveRandomDepartment();
			EmployeeRequest updateEmployeeRequest = EmployeeControllerIntegrationTests.this.employeeRequestTestFactory
				.builder()
				.departmentName(newDepartmentResponse.departmentName())
				.emailAddress(null)
				.firstName(null)
				.lastName(null)
				.birthday(null)
				.create();
			String updateRequestAsJson = transformRequestToJSONByView(updateEmployeeRequest, DataView.PATCH.class);
			String patchUri = "%s/{departmentId}".formatted(EmployeeController.BASE_URI);

			// Act / Assert
			EmployeeControllerIntegrationTests.this.mockMvc
				.perform(MockMvcRequestBuilders.patch(patchUri, employeeResponse.employeeId())
					.contentType(MediaType.APPLICATION_JSON)
					.content(updateRequestAsJson))
				.andExpect(MockMvcResultMatchers.status().isNoContent());

			Employee updateEmployee = EmployeeControllerIntegrationTests.this.employeeRepository
				.findById(employeeResponse.employeeId())
				.orElseThrow();
			Assertions
				.assertThat(EmployeeControllerIntegrationTests.this.dateFormatter.format(updateEmployee.getBirthday()))
				.isEqualTo(EmployeeControllerIntegrationTests.this.dateFormatter.format(employeeResponse.birthday()));
			Assertions.assertThat(updateEmployee.getFullName().getFirstName()).isEqualTo(employeeResponse.firstName());
			Assertions.assertThat(updateEmployee.getFullName().getLastName()).isEqualTo(employeeResponse.lastName());
			Assertions.assertThat(updateEmployee.getEmailAddress()).isEqualTo(employeeResponse.emailAddress());
			Assertions.assertThat(updateEmployee.getDepartment().getDepartmentName())
				.isEqualTo(updateEmployeeRequest.departmentName());

		}

	}

	@Nested
	@DisplayName("when full update")
	class WhenFullUpdate {

		@Test
		@DisplayName("PUT: 'hhtps://.../employees/{departmentId} returns NO CONTENT if the specified request (all fields set) is valid ")
		void givenValidFullRequest_whenFullUpdateEmployee_thenStatus204() throws Exception {
			// Arrange
			DepartmentResponse departmentResponse = saveRandomDepartment();
			EmployeeRequest initialEmployeeRequest = EmployeeControllerIntegrationTests.this.employeeRequestTestFactory
				.builder()
				.departmentName(departmentResponse.departmentName())
				.create();
			String requestAsJson = transformRequestToJSONByView(initialEmployeeRequest, DataView.POST.class);
			String createUri = "%s".formatted(EmployeeController.BASE_URI);
			MvcResult mvcResult = EmployeeControllerIntegrationTests.this.mockMvc
				.perform(MockMvcRequestBuilders.post(createUri)
					.contentType(MediaType.APPLICATION_JSON)
					.content(requestAsJson))
				.andReturn();
			EmployeeResponse employeeResponse = EmployeeControllerIntegrationTests.this.objectMapper
				.readValue(mvcResult.getResponse().getContentAsString(), EmployeeResponse.class);

			DepartmentResponse otherDepartmentResponse = saveRandomDepartment();
			EmployeeRequest updateEmployeeRequest = EmployeeControllerIntegrationTests.this.employeeRequestTestFactory
				.builder()
				.departmentName(otherDepartmentResponse.departmentName())
				.create();
			String updateRequestAsJson = transformRequestToJSONByView(updateEmployeeRequest, DataView.PUT.class);
			String putUri = "%s/{departmentId}".formatted(EmployeeController.BASE_URI);

			// Act / Assert
			EmployeeControllerIntegrationTests.this.mockMvc
				.perform(MockMvcRequestBuilders.put(putUri, employeeResponse.employeeId())
					.contentType(MediaType.APPLICATION_JSON)
					.content(updateRequestAsJson))
				.andExpect(MockMvcResultMatchers.status().isNoContent());

			Employee updateEmployee = EmployeeControllerIntegrationTests.this.employeeRepository
				.findById(employeeResponse.employeeId())
				.orElseThrow();
			Assertions
				.assertThat(EmployeeControllerIntegrationTests.this.dateFormatter.format(updateEmployee.getBirthday()))
				.isEqualTo(
						EmployeeControllerIntegrationTests.this.dateFormatter.format(updateEmployeeRequest.birthday()));
			Assertions.assertThat(updateEmployee.getFullName().getFirstName())
				.isEqualTo(updateEmployeeRequest.firstName());
			Assertions.assertThat(updateEmployee.getFullName().getLastName())
				.isEqualTo(updateEmployeeRequest.lastName());
			Assertions.assertThat(updateEmployee.getEmailAddress()).isEqualTo(updateEmployeeRequest.emailAddress());
			Assertions.assertThat(updateEmployee.getDepartment().getDepartmentName())
				.isEqualTo(updateEmployeeRequest.departmentName());
		}

		@Test
		@DisplayName("PUT: 'hhtps://.../employees/{departmentId} returns BAD REQUEST on null birthday")
		void givenNullBirthDay_whenFullUpdateEmployee_thenStatus400() throws Exception {
			// Arrange
			DepartmentResponse departmentResponse = saveRandomDepartment();
			EmployeeRequest initialEmployeeRequest = EmployeeControllerIntegrationTests.this.employeeRequestTestFactory
				.builder()
				.departmentName(departmentResponse.departmentName())
				.create();
			String requestAsJson = transformRequestToJSONByView(initialEmployeeRequest, DataView.POST.class);
			String createUri = "%s".formatted(EmployeeController.BASE_URI);
			MvcResult mvcResult = EmployeeControllerIntegrationTests.this.mockMvc
				.perform(MockMvcRequestBuilders.post(createUri)
					.contentType(MediaType.APPLICATION_JSON)
					.content(requestAsJson))
				.andReturn();
			EmployeeResponse employeeResponse = EmployeeControllerIntegrationTests.this.objectMapper
				.readValue(mvcResult.getResponse().getContentAsString(), EmployeeResponse.class);

			EmployeeRequest updateEmployeeRequest = EmployeeControllerIntegrationTests.this.employeeRequestTestFactory
				.builder()
				.departmentName(departmentResponse.departmentName())
				.birthday(null)
				.create();
			String updateRequestAsJson = transformRequestToJSONByView(updateEmployeeRequest, DataView.PUT.class);
			String putUri = "%s/{departmentId}".formatted(EmployeeController.BASE_URI);

			// Act / Assert
			EmployeeControllerIntegrationTests.this.mockMvc
				.perform(MockMvcRequestBuilders.put(putUri, employeeResponse.employeeId())
					.contentType(MediaType.APPLICATION_JSON)
					.content(updateRequestAsJson))
				.andExpect(MockMvcResultMatchers.status().isBadRequest())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.notNullValue()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.url", Matchers.notNullValue()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.httpMethod", Matchers.notNullValue()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.httpStatus", Matchers.is(HttpStatus.BAD_REQUEST.name())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.errorDateTime", Matchers.notNullValue()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.errorMessage", Matchers.notNullValue()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.errorMessage",
						Matchers.containsString("The employee's birthday must not be null!")));
		}

		@Test
		@DisplayName("PUT: 'hhtps://.../employees/{departmentId} returns BAD REQUEST on invalid email")
		void givenInvalidEmail_whenFullUpdateEmployee_thenStatus400() throws Exception {
			// Arrange
			DepartmentResponse departmentResponse = saveRandomDepartment();
			EmployeeRequest initialEmployeeRequest = EmployeeControllerIntegrationTests.this.employeeRequestTestFactory
				.builder()
				.departmentName(departmentResponse.departmentName())
				.create();
			String requestAsJson = transformRequestToJSONByView(initialEmployeeRequest, DataView.POST.class);
			String createUri = "%s".formatted(EmployeeController.BASE_URI);
			MvcResult mvcResult = EmployeeControllerIntegrationTests.this.mockMvc
				.perform(MockMvcRequestBuilders.post(createUri)
					.contentType(MediaType.APPLICATION_JSON)
					.content(requestAsJson))
				.andReturn();
			EmployeeResponse employeeResponse = EmployeeControllerIntegrationTests.this.objectMapper
				.readValue(mvcResult.getResponse().getContentAsString(), EmployeeResponse.class);

			EmployeeRequest updateEmployeeRequest = EmployeeControllerIntegrationTests.this.employeeRequestTestFactory
				.builder()
				.departmentName(departmentResponse.departmentName())
				.emailAddress(RandomStringUtils.randomAlphabetic(10))
				.create();
			String updateRequestAsJson = transformRequestToJSONByView(updateEmployeeRequest, DataView.PUT.class);
			String putUri = "%s/{departmentId}".formatted(EmployeeController.BASE_URI);

			// Act / Assert
			EmployeeControllerIntegrationTests.this.mockMvc
				.perform(MockMvcRequestBuilders.put(putUri, employeeResponse.employeeId())
					.contentType(MediaType.APPLICATION_JSON)
					.content(updateRequestAsJson))
				.andExpect(MockMvcResultMatchers.status().isBadRequest())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.notNullValue()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.url", Matchers.notNullValue()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.httpMethod", Matchers.notNullValue()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.httpStatus", Matchers.is(HttpStatus.BAD_REQUEST.name())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.errorDateTime", Matchers.notNullValue()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.errorMessage", Matchers.notNullValue()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.errorMessage",
						Matchers.containsString("must match \"" + EmployeeRequest.EMAIL_REGEX + "\"")));
		}

		@Test
		@DisplayName("PUT: 'hhtps://.../employees/{departmentId} returns BAD REQUEST on null email")
		void givenNullEmail_whenFullUpdateEmployee_thenStatus400() throws Exception {
			// Arrange
			DepartmentResponse departmentResponse = saveRandomDepartment();
			EmployeeRequest initialEmployeeRequest = EmployeeControllerIntegrationTests.this.employeeRequestTestFactory
				.builder()
				.departmentName(departmentResponse.departmentName())
				.create();
			String requestAsJson = transformRequestToJSONByView(initialEmployeeRequest, DataView.POST.class);
			String createUri = "%s".formatted(EmployeeController.BASE_URI);
			MvcResult mvcResult = EmployeeControllerIntegrationTests.this.mockMvc
				.perform(MockMvcRequestBuilders.post(createUri)
					.contentType(MediaType.APPLICATION_JSON)
					.content(requestAsJson))
				.andReturn();
			EmployeeResponse employeeResponse = EmployeeControllerIntegrationTests.this.objectMapper
				.readValue(mvcResult.getResponse().getContentAsString(), EmployeeResponse.class);

			EmployeeRequest updateEmployeeRequest = EmployeeControllerIntegrationTests.this.employeeRequestTestFactory
				.builder()
				.departmentName(departmentResponse.departmentName())
				.emailAddress(null)
				.create();
			String updateRequestAsJson = transformRequestToJSONByView(updateEmployeeRequest, DataView.PATCH.class);
			String putUri = "%s/{departmentId}".formatted(EmployeeController.BASE_URI);

			// Act / Assert
			EmployeeControllerIntegrationTests.this.mockMvc
				.perform(MockMvcRequestBuilders.put(putUri, employeeResponse.employeeId())
					.contentType(MediaType.APPLICATION_JSON)
					.content(updateRequestAsJson))
				.andExpect(MockMvcResultMatchers.status().isBadRequest())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.notNullValue()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.url", Matchers.notNullValue()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.httpMethod", Matchers.notNullValue()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.httpStatus", Matchers.is(HttpStatus.BAD_REQUEST.name())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.errorDateTime", Matchers.notNullValue()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.errorMessage", Matchers.notNullValue()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.errorMessage",
						Matchers.containsString("The employee's email address must not be empty!")));
		}

		@Test
		@DisplayName("PUT: 'hhtps://.../employees/{departmentId} returns BAD REQUEST on null first name")
		void givenNullFirstName_whenFullUpdateEmployee_thenStatus400() throws Exception {
			// Arrange
			DepartmentResponse departmentResponse = saveRandomDepartment();
			EmployeeRequest initialEmployeeRequest = EmployeeControllerIntegrationTests.this.employeeRequestTestFactory
				.builder()
				.departmentName(departmentResponse.departmentName())
				.create();
			String requestAsJson = transformRequestToJSONByView(initialEmployeeRequest, DataView.POST.class);
			String createUri = "%s".formatted(EmployeeController.BASE_URI);
			MvcResult mvcResult = EmployeeControllerIntegrationTests.this.mockMvc
				.perform(MockMvcRequestBuilders.post(createUri)
					.contentType(MediaType.APPLICATION_JSON)
					.content(requestAsJson))
				.andReturn();
			EmployeeResponse employeeResponse = EmployeeControllerIntegrationTests.this.objectMapper
				.readValue(mvcResult.getResponse().getContentAsString(), EmployeeResponse.class);

			EmployeeRequest updateEmployeeRequest = EmployeeControllerIntegrationTests.this.employeeRequestTestFactory
				.builder()
				.departmentName(departmentResponse.departmentName())
				.firstName(null)
				.create();
			String updateRequestAsJson = transformRequestToJSONByView(updateEmployeeRequest, DataView.PATCH.class);
			String putUri = "%s/{departmentId}".formatted(EmployeeController.BASE_URI);

			// Act / Assert
			EmployeeControllerIntegrationTests.this.mockMvc
				.perform(MockMvcRequestBuilders.put(putUri, employeeResponse.employeeId())
					.contentType(MediaType.APPLICATION_JSON)
					.content(updateRequestAsJson))
				.andExpect(MockMvcResultMatchers.status().isBadRequest())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.notNullValue()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.url", Matchers.notNullValue()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.httpMethod", Matchers.notNullValue()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.httpStatus", Matchers.is(HttpStatus.BAD_REQUEST.name())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.errorDateTime", Matchers.notNullValue()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.errorMessage", Matchers.notNullValue()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.errorMessage",
						Matchers.containsString("The employee's first name must not be empty!")));
		}

		@Test
		@DisplayName("PUT: 'hhtps://.../employees/{departmentId} returns BAD REQUEST on null last name")
		void givenNullLastName_whenFullUpdateEmployee_thenStatus400() throws Exception {
			// Arrange
			DepartmentResponse departmentResponse = saveRandomDepartment();
			EmployeeRequest initialEmployeeRequest = EmployeeControllerIntegrationTests.this.employeeRequestTestFactory
				.builder()
				.departmentName(departmentResponse.departmentName())
				.create();
			String requestAsJson = transformRequestToJSONByView(initialEmployeeRequest, DataView.POST.class);
			String createUri = "%s".formatted(EmployeeController.BASE_URI);
			MvcResult mvcResult = EmployeeControllerIntegrationTests.this.mockMvc
				.perform(MockMvcRequestBuilders.post(createUri)
					.contentType(MediaType.APPLICATION_JSON)
					.content(requestAsJson))
				.andReturn();
			EmployeeResponse employeeResponse = EmployeeControllerIntegrationTests.this.objectMapper
				.readValue(mvcResult.getResponse().getContentAsString(), EmployeeResponse.class);

			EmployeeRequest updateEmployeeRequest = EmployeeControllerIntegrationTests.this.employeeRequestTestFactory
				.builder()
				.departmentName(departmentResponse.departmentName())
				.lastName(null)
				.create();
			String updateRequestAsJson = transformRequestToJSONByView(updateEmployeeRequest, DataView.PATCH.class);
			String putUri = "%s/{departmentId}".formatted(EmployeeController.BASE_URI);

			// Act / Assert
			EmployeeControllerIntegrationTests.this.mockMvc
				.perform(MockMvcRequestBuilders.put(putUri, employeeResponse.employeeId())
					.contentType(MediaType.APPLICATION_JSON)
					.content(updateRequestAsJson))
				.andExpect(MockMvcResultMatchers.status().isBadRequest())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.notNullValue()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.url", Matchers.notNullValue()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.httpMethod", Matchers.notNullValue()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.httpStatus", Matchers.is(HttpStatus.BAD_REQUEST.name())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.errorDateTime", Matchers.notNullValue()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.errorMessage", Matchers.notNullValue()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.errorMessage",
						Matchers.containsString("The employee's last name must not be empty!")));
		}

		@Test
		@DisplayName("PUT: 'hhtps://.../employees/{departmentId} returns BAD REQUEST on null department name")
		void givenNullDepartmentName_whenFullUpdateEmployee_thenStatus400() throws Exception {
			// Arrange
			DepartmentResponse departmentResponse = saveRandomDepartment();
			EmployeeRequest initialEmployeeRequest = EmployeeControllerIntegrationTests.this.employeeRequestTestFactory
				.builder()
				.departmentName(departmentResponse.departmentName())
				.create();
			String requestAsJson = transformRequestToJSONByView(initialEmployeeRequest, DataView.POST.class);
			String createUri = "%s".formatted(EmployeeController.BASE_URI);
			MvcResult mvcResult = EmployeeControllerIntegrationTests.this.mockMvc
				.perform(MockMvcRequestBuilders.post(createUri)
					.contentType(MediaType.APPLICATION_JSON)
					.content(requestAsJson))
				.andReturn();
			EmployeeResponse employeeResponse = EmployeeControllerIntegrationTests.this.objectMapper
				.readValue(mvcResult.getResponse().getContentAsString(), EmployeeResponse.class);

			EmployeeRequest updateEmployeeRequest = EmployeeControllerIntegrationTests.this.employeeRequestTestFactory
				.builder()
				.departmentName(null)
				.create();
			String updateRequestAsJson = transformRequestToJSONByView(updateEmployeeRequest, DataView.PATCH.class);
			String putUri = "%s/{departmentId}".formatted(EmployeeController.BASE_URI);

			// Act / Assert
			EmployeeControllerIntegrationTests.this.mockMvc
				.perform(MockMvcRequestBuilders.put(putUri, employeeResponse.employeeId())
					.contentType(MediaType.APPLICATION_JSON)
					.content(updateRequestAsJson))
				.andExpect(MockMvcResultMatchers.status().isBadRequest())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.notNullValue()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.url", Matchers.notNullValue()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.httpMethod", Matchers.notNullValue()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.httpStatus", Matchers.is(HttpStatus.BAD_REQUEST.name())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.errorDateTime", Matchers.notNullValue()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.errorMessage", Matchers.notNullValue()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.errorMessage",
						Matchers.containsString("The employee department name must not be blank!")));
		}

	}

	@Nested
	@DisplayName("when delete")
	class WhenDelete {

		@Test
		@DisplayName("DELETE: 'hhtps://.../employees/{departmentId}' returns NOT FOUND if the specified uuid doesn't exist")
		void givenUnknownId_whenDeleteEmployeeById_thenStatus404() throws Exception {
			// Arrange
			String unknownId = UUID.randomUUID().toString();
			String uri = "%s/{departmentId}".formatted(EmployeeController.BASE_URI);

			// Act / Assert
			EmployeeControllerIntegrationTests.this.mockMvc
				.perform(MockMvcRequestBuilders.delete(uri, unknownId).contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isNotFound())
				.andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.notNullValue()))
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.notNullValue()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.url", Matchers.notNullValue()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.httpMethod", Matchers.notNullValue()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.httpStatus", Matchers.is(HttpStatus.NOT_FOUND.name())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.errorDateTime", Matchers.notNullValue()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.errorMessage", Matchers.notNullValue()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.errorMessage", Matchers
					.containsString("The employee with the ID [%s] could not be found!".formatted(unknownId))));
		}

		@Test
		@DisplayName("DELETE: 'hhtps://.../employees/{departmentId}' returns NO CONTENT if the specified uuid exists")
		void givenEmployee_whenDeleteEmployeeById_thenStatus204() throws Exception {
			// Arrange
			DepartmentResponse departmentResponse = saveRandomDepartment();
			EmployeeRequest initialEmployeeRequest = EmployeeControllerIntegrationTests.this.employeeRequestTestFactory
				.builder()
				.departmentName(departmentResponse.departmentName())
				.create();
			String requestAsJson = transformRequestToJSONByView(initialEmployeeRequest, DataView.POST.class);
			String createUri = "%s".formatted(EmployeeController.BASE_URI);
			MvcResult mvcResult = EmployeeControllerIntegrationTests.this.mockMvc
				.perform(MockMvcRequestBuilders.post(createUri)
					.contentType(MediaType.APPLICATION_JSON)
					.content(requestAsJson))
				.andReturn();
			EmployeeResponse employeeResponse = EmployeeControllerIntegrationTests.this.objectMapper
				.readValue(mvcResult.getResponse().getContentAsString(), EmployeeResponse.class);
			String uri = "%s/{departmentId}".formatted(EmployeeController.BASE_URI);

			// Act / Assert
			EmployeeControllerIntegrationTests.this.mockMvc
				.perform(MockMvcRequestBuilders.delete(uri, employeeResponse.employeeId())
					.contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isNoContent());

			Optional<Employee> optionalEmployee = EmployeeControllerIntegrationTests.this.employeeRepository
				.findById(employeeResponse.employeeId());
			Assertions.assertThat(optionalEmployee).isEmpty();
		}

	}

}
