package de.stminko.employeeservice.department.boundary;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import de.stminko.employeeservice.AbstractIntegrationTestSuite;
import de.stminko.employeeservice.department.control.DepartmentRepository;
import de.stminko.employeeservice.department.entity.Department;
import de.stminko.employeeservice.department.entity.DepartmentRequest;
import de.stminko.employeeservice.department.entity.DepartmentResponse;
import de.stminko.employeeservice.employee.boundary.EmployeeController;
import de.stminko.employeeservice.employee.entity.EmployeeRequest;
import de.stminko.employeeservice.employee.entity.EmployeeResponse;
import de.stminko.employeeservice.runtime.rest.bondary.DataView;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.assertj.core.api.Assertions;
import org.hamcrest.Matchers;
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
class DepartmentControllerIntegrationTests extends AbstractIntegrationTestSuite {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private DepartmentRepository departmentRepository;

	private DepartmentResponse saveRandomDepartment() {
		try {
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
		catch (Exception caught) {
			throw new RuntimeException(caught);
		}

	}

	private EmployeeResponse saveRandomEmployee(String departmentName) {
		try {
			EmployeeRequest employeeRequest = this.employeeRequestTestFactory.builder()
				.departmentName(departmentName)
				.create();
			String employeeRequestAsJson = transformRequestToJSONByView(employeeRequest, DataView.POST.class);
			MvcResult employeeMvcResult = this.mockMvc
				.perform(MockMvcRequestBuilders.post(EmployeeController.BASE_URI)
					.contentType(MediaType.APPLICATION_JSON)
					.content(employeeRequestAsJson))
				.andReturn();
			return this.objectMapper.readValue(employeeMvcResult.getResponse().getContentAsString(),
					EmployeeResponse.class);
		}
		catch (Exception caught) {
			throw new RuntimeException(caught);
		}
	}

	@Nested
	@DisplayName("when new")
	class WhenCreate {

		@Test
		@DisplayName("POST: 'http://.../departments' returns BAD_REQUEST on empty department name")
		void givenEmptyDepartmentName_whenCreate_thenStatus400() throws Exception {
			// Arrange
			DepartmentRequest departmentRequest = DepartmentControllerIntegrationTests.this.departmentRequestTestFactory
				.builder()
				.departmentName("")
				.create();

			// Act / Assert
			DepartmentControllerIntegrationTests.this.mockMvc
				.perform(MockMvcRequestBuilders.post(DepartmentController.BASE_URI)
					.contentType(MediaType.APPLICATION_JSON_VALUE)
					.content(DepartmentControllerIntegrationTests.this.objectMapper
						.writeValueAsString(departmentRequest)))
				.andExpect(MockMvcResultMatchers.status().isBadRequest());
		}

		@Test
		@DisplayName("POST: 'http://.../departments' returns BAD_REQUEST on blank department name")
		void givenBlankDepartmentName_whenCreate_thenStatus400() throws Exception {
			// Arrange
			DepartmentRequest departmentRequest = DepartmentControllerIntegrationTests.this.departmentRequestTestFactory
				.builder()
				.departmentName("  ")
				.create();

			// Act / Assert
			DepartmentControllerIntegrationTests.this.mockMvc
				.perform(MockMvcRequestBuilders.post(DepartmentController.BASE_URI)
					.contentType(MediaType.APPLICATION_JSON_VALUE)
					.content(DepartmentControllerIntegrationTests.this.objectMapper
						.writeValueAsString(departmentRequest)))
				.andExpect(MockMvcResultMatchers.status().isBadRequest());
		}

		@Test
		@DisplayName("POST: 'http://.../departments' returns BAD_REQUEST on null department name")
		void givenNullDepartmentName_whenCreate_thenStatus400() throws Exception {
			// Arrange
			DepartmentRequest departmentRequest = DepartmentControllerIntegrationTests.this.departmentRequestTestFactory
				.builder()
				.departmentName("")
				.create();

			// Act / Assert
			DepartmentControllerIntegrationTests.this.mockMvc
				.perform(MockMvcRequestBuilders.post(DepartmentController.BASE_URI)
					.contentType(MediaType.APPLICATION_JSON_VALUE)
					.content(DepartmentControllerIntegrationTests.this.objectMapper
						.writeValueAsString(departmentRequest)))
				.andExpect(MockMvcResultMatchers.status().isBadRequest());
		}

		@Test
		@DisplayName("POST: 'http://.../departments' returns CREATED on valid request")
		void givenValidDepartmentRequest_whenCreate_thenStatus201() throws Exception {
			// Arrange
			DepartmentRequest departmentRequest = DepartmentControllerIntegrationTests.this.departmentRequestTestFactory
				.createDefault();
			String requestAsJson = transformRequestToJSONByView(departmentRequest, DataView.POST.class);

			// Act / Assert
			MvcResult mvcResult = DepartmentControllerIntegrationTests.this.mockMvc
				.perform(MockMvcRequestBuilders.post(DepartmentController.BASE_URI)
					.contentType(MediaType.APPLICATION_JSON)
					.content(requestAsJson))
				.andExpect(MockMvcResultMatchers.status().isCreated())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.notNullValue()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.notNullValue()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.departmentName",
						Matchers.is(departmentRequest.departmentName())))
				.andExpect(MockMvcResultMatchers.header()
					.string(HttpHeaders.LOCATION, Matchers.containsString(DepartmentController.BASE_URI)))
				.andReturn();
			String contentAsString = mvcResult.getResponse().getContentAsString();
			DepartmentResponse departmentResponse = DepartmentControllerIntegrationTests.this.objectMapper
				.readValue(contentAsString, DepartmentResponse.class);
			Optional<Department> optionalDepartment = DepartmentControllerIntegrationTests.this.departmentRepository
				.findById(departmentResponse.id());
			Assertions.assertThat(optionalDepartment)
				.hasValueSatisfying((Department value) -> Assertions.assertThat(value).isNotNull());
		}

	}

	@Nested
	@DisplayName("when access")
	class WhenAccess {

		@Test
		@DisplayName("GET: 'https://.../departments/{id}' returns NOT FOUND for unknown id ")
		void givenUnknownId_whenFindById_thenStatus404() throws Exception {
			// Arrange
			Long unknownId = Long.MAX_VALUE;
			String uri = "%s/{id}".formatted(DepartmentController.BASE_URI);

			// Act / Assert
			DepartmentControllerIntegrationTests.this.mockMvc
				.perform(MockMvcRequestBuilders.get(uri, unknownId).contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isNotFound())
				.andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.notNullValue()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.errorMessage", Matchers
					.containsString("The department with the ID [%s] could not be found!".formatted(unknownId))));
		}

		@Test
		@DisplayName("GET: 'https://.../departments/{id}' returns OK and valid department response")
		void givenDepartment_whenFindById_thenStatus200AndReturnValidDepartmentResponse() throws Exception {
			// Arrange
			DepartmentResponse persisted = saveRandomDepartment();
			String uri = "%s/{id}".formatted(DepartmentController.BASE_URI);

			// Act / Assert
			MvcResult mvcResult = DepartmentControllerIntegrationTests.this.mockMvc
				.perform(MockMvcRequestBuilders.get(uri, persisted.id()).contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.notNullValue()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.departmentName", Matchers.is(persisted.departmentName())))
				.andReturn();

			String contentAsString = mvcResult.getResponse().getContentAsString();
			DepartmentResponse departmentResponse = DepartmentControllerIntegrationTests.this.objectMapper
				.readValue(contentAsString, DepartmentResponse.class);
			Assertions.assertThat(departmentResponse).isNotNull();
			Assertions.assertThat(departmentResponse.id()).isEqualTo(persisted.id());
			Assertions.assertThat(departmentResponse.departmentName()).isEqualTo(persisted.departmentName());
		}

		@Test
		@DisplayName("GET: 'https://.../departments/{id}/employees  returns all employees associated to a department")
		void givenMultipleEmployeesAssociatedToDepartment_whenFindAllAssociatedEmployeesByDepId_thenReturnAllAssociatedEmployees()
				throws Exception {
			// Arrange
			DepartmentResponse departmentResponse = saveRandomDepartment();

			int count = RandomUtils.nextInt(20, 30);
			List<EmployeeResponse> employeeResponseList = IntStream.range(0, count)
				.mapToObj((int value) -> saveRandomEmployee(departmentResponse.departmentName()))
				.toList();
			Long id = departmentResponse.id();
			String uri = "%s/{id}/employees".formatted(DepartmentController.BASE_URI);

			// Act / Assert
			DepartmentControllerIntegrationTests.this.mockMvc
				.perform(MockMvcRequestBuilders.get(uri, id).contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.notNullValue()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.content", Matchers.hasSize(employeeResponseList.size())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.content[*].emailAddress").exists())
				.andExpect(MockMvcResultMatchers.jsonPath("$.content[*].firstName").exists())
				.andExpect(MockMvcResultMatchers.jsonPath("$.content[*].lastName").exists())
				.andExpect(MockMvcResultMatchers.jsonPath("$.content[*].birthday").exists())
				.andExpect(MockMvcResultMatchers.jsonPath("$.content[*].departmentName").exists())
				.andExpect(MockMvcResultMatchers.jsonPath("$.content[*].departmentName",
						Matchers.everyItem(Matchers.is(departmentResponse.departmentName()))))
				.andExpect(MockMvcResultMatchers.jsonPath("$.content[*].id").exists());
		}

		@Test
		@DisplayName("GET: 'https://.../departments/{id}/employees Finding returns 404 for unknown id")
		void givenUnknownId_whenFindAllAssociatedEmployeesByDepId_thenStatus404() throws Exception {
			// Arrange
			Long unknownId = Long.MAX_VALUE;
			String uri = "%s/{id}/employees".formatted(DepartmentController.BASE_URI);

			// Act / Assert
			DepartmentControllerIntegrationTests.this.mockMvc
				.perform(MockMvcRequestBuilders.get(uri, unknownId).contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isNotFound())
				.andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.notNullValue()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.errorMessage", Matchers.containsString(
						"The department with the ID [%s] could not be found!".formatted(unknownId.toString()))));
		}

		@Test
		@DisplayName("GET: 'https://.../departments/{id}/employees  returns no employees associated to a department if there is no employee associates to the department")
		void givenNoEmployeeAssociatedToDepartment_whenFindAllAssociatedEmployeesByDepId_thenReturnNoAssociatedEmployees()
				throws Exception {
			// Arrange
			DepartmentResponse departmentResponse = saveRandomDepartment();
			Long id = departmentResponse.id();
			String uri = "%s/{id}/employees".formatted(DepartmentController.BASE_URI);

			// Act / Assert
			DepartmentControllerIntegrationTests.this.mockMvc
				.perform(MockMvcRequestBuilders.get(uri, id).contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.notNullValue()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.content", Matchers.hasSize(0)));
		}

		@Test
		@DisplayName("GET: 'https://.../departments/{id}/revisions succeeds on existing department")
		void givenExistingDepartment_whenFindRevisions_thenSuccessAndReturnPageOfRevisions() throws Exception {
			// Arrange
			DepartmentResponse departmentResponse = saveRandomDepartment();

			DepartmentRequest updateDepartmentRequest = DepartmentControllerIntegrationTests.this.departmentRequestTestFactory
				.builder()
				.departmentName(RandomStringUtils.randomAlphabetic(23))
				.create();

			String updateRequestAsJson = transformRequestToJSONByView(updateDepartmentRequest, DataView.PUT.class);
			String updateUri = "%s/{id}".formatted(DepartmentController.BASE_URI);
			DepartmentControllerIntegrationTests.this.mockMvc
				.perform(MockMvcRequestBuilders.put(updateUri, departmentResponse.id())
					.contentType(MediaType.APPLICATION_JSON)
					.content(updateRequestAsJson));

			String deleteUri = "%s/{id}".formatted(DepartmentController.BASE_URI);
			DepartmentControllerIntegrationTests.this.mockMvc
				.perform(MockMvcRequestBuilders.delete(deleteUri, departmentResponse.id())
					.contentType(MediaType.APPLICATION_JSON));

			String revisionUri = "%s/{id}/revisions".formatted(DepartmentController.BASE_URI);

			// Act / Assert
			DepartmentControllerIntegrationTests.this.mockMvc
				.perform(MockMvcRequestBuilders.get(revisionUri, departmentResponse.id())
					.contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.notNullValue()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.content", Matchers.hasSize(3)))
				.andExpect(MockMvcResultMatchers.jsonPath("$.content[0].metadata.revisionType", Matchers.is("INSERT")))
				.andExpect(MockMvcResultMatchers.jsonPath("$.content[1].metadata.revisionType", Matchers.is("UPDATE")))
				.andExpect(MockMvcResultMatchers.jsonPath("$.content[2].metadata.revisionType", Matchers.is("DELETE")));

		}

		@Test
		@DisplayName("GET: 'https://.../departments succeeds and returns all departments in a page")
		void givenDepartments_whenFindAll_thenSuccessAndReturnPageOfDepartments() throws Exception {
			// Arrange
			int count = RandomUtils.nextInt(20, 30);
			IntStream.range(0, count).forEach((int i) -> saveRandomDepartment());

			String uri = DepartmentController.BASE_URI;

			// Act / Assert
			DepartmentControllerIntegrationTests.this.mockMvc
				.perform(MockMvcRequestBuilders.get(uri).contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.notNullValue()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.content", Matchers.hasSize(count)))
				.andExpect(MockMvcResultMatchers.jsonPath("$.content[*].departmentName").exists())
				.andExpect(MockMvcResultMatchers.jsonPath("$.content[*].id").exists());
		}

	}

	@Nested
	@DisplayName("when update")
	class WhenUpdate {

		@Test
		@DisplayName("PUT: 'http://.../departments/{id} returns NO CONTENT if the specified request (all fields set) is valid ")
		void givenValidFullRequest_whenFullUpdateDepartment_thenStatus204() throws Exception {
			// Arrange
			DepartmentResponse departmentResponse = saveRandomDepartment();

			DepartmentRequest updateDepartmentRequest = DepartmentControllerIntegrationTests.this.departmentRequestTestFactory
				.builder()
				.departmentName(RandomStringUtils.randomAlphabetic(23))
				.create();

			String updateRequestAsJson = transformRequestToJSONByView(updateDepartmentRequest, DataView.PUT.class);
			String uri = "%s/{id}".formatted(DepartmentController.BASE_URI);

			// Act / Assert
			DepartmentControllerIntegrationTests.this.mockMvc
				.perform(MockMvcRequestBuilders.put(uri, departmentResponse.id())
					.contentType(MediaType.APPLICATION_JSON)
					.content(updateRequestAsJson))
				.andExpect(MockMvcResultMatchers.status().isNoContent());

			Department updatedDepartment = DepartmentControllerIntegrationTests.this.departmentRepository
				.findById(departmentResponse.id())
				.orElseThrow();
			Assertions.assertThat(updatedDepartment.getDepartmentName())
				.isEqualTo(updateDepartmentRequest.departmentName());

		}

		@Test
		@DisplayName("PUT: 'http://.../departments/{id} returns NOT FOUND if the specified department does not exists ")
		void givenUnknownId_whenFullUpdateDepartment_thenStatus404() throws Exception {
			// Arrange
			DepartmentRequest updateDepartmentRequest = DepartmentControllerIntegrationTests.this.departmentRequestTestFactory
				.builder()
				.departmentName(RandomStringUtils.randomAlphabetic(23))
				.create();

			String updateRequestAsJson = transformRequestToJSONByView(updateDepartmentRequest, DataView.PUT.class);
			String uri = "%s/{id}".formatted(DepartmentController.BASE_URI);
			Long unknownId = Long.MAX_VALUE;

			// Act / Assert
			DepartmentControllerIntegrationTests.this.mockMvc
				.perform(MockMvcRequestBuilders.put(uri, unknownId)
					.contentType(MediaType.APPLICATION_JSON)
					.content(updateRequestAsJson))
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
					.containsString("The department with the ID [%s] could not be found!".formatted(unknownId))));
		}

		@Test
		@DisplayName("PUT: 'http://.../departments/{id} returns BAD REQUEST on null department name")
		void givenNullDepartmentName_whenFullUpdateDepartment_thenStatus400() throws Exception {
			// Arrange
			DepartmentResponse departmentResponse = saveRandomDepartment();

			DepartmentRequest updateDepartmentRequest = DepartmentControllerIntegrationTests.this.departmentRequestTestFactory
				.builder()
				.departmentName(null)
				.create();

			String updateRequestAsJson = transformRequestToJSONByView(updateDepartmentRequest, DataView.PUT.class);
			String uri = "%s/{id}".formatted(DepartmentController.BASE_URI);

			// Act / Assert
			DepartmentControllerIntegrationTests.this.mockMvc
				.perform(MockMvcRequestBuilders.put(uri, departmentResponse.id())
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
						Matchers.containsString("The department name must not be empty!")));

		}

		@Test
		@DisplayName("PUT: 'http://.../departments/{id} returns BAD REQUEST on already used department name")
		void givenAlreadyUsedDepartmentName_whenFullUpdateDepartment_thenStatus400() throws Exception {
			// Arrange
			DepartmentResponse departmentResponse_1 = saveRandomDepartment();
			DepartmentResponse departmentResponse_2 = saveRandomDepartment();

			DepartmentRequest updateDepartmentRequest_2 = DepartmentControllerIntegrationTests.this.departmentRequestTestFactory
				.builder()
				.departmentName(departmentResponse_1.departmentName())
				.create();

			String updateRequestAsJson = transformRequestToJSONByView(updateDepartmentRequest_2, DataView.PUT.class);
			String uri = "%s/{id}".formatted(DepartmentController.BASE_URI);

			// Act / Assert
			DepartmentControllerIntegrationTests.this.mockMvc
				.perform(MockMvcRequestBuilders.put(uri, departmentResponse_2.id())
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
						"The department name [%s] already exists!".formatted(departmentResponse_1.departmentName()))));

		}

	}

	@Nested
	@DisplayName("when delete")
	class WhenDelete {

		@Test
		@DisplayName("DELETE: 'http://.../departments/{id}' returns NOT FOUND if the specified id doesn't exist")
		void givenUnknownId_whenDeleteDepartmentById_thenStatus404() throws Exception {
			// Arrange
			Long unknownId = Long.MAX_VALUE;
			String uri = "%s/{id}".formatted(DepartmentController.BASE_URI);

			// Act / Assert
			DepartmentControllerIntegrationTests.this.mockMvc
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
					.containsString("The department with the ID [%s] could not be found!".formatted(unknownId))));
		}

		@Test
		@DisplayName("DELETE: 'http://.../departments/{id}' returns CONFLICT if the specified department isn't empty")
		void givenNotEmptyDepartment_whenDeleteDepartmentById_thenStatus409() throws Exception {
			// Arrange
			DepartmentResponse departmentResponse = saveRandomDepartment();
			String uri = "%s/{id}".formatted(DepartmentController.BASE_URI);
			saveRandomEmployee(departmentResponse.departmentName());

			// Act / Assert
			DepartmentControllerIntegrationTests.this.mockMvc
				.perform(MockMvcRequestBuilders.delete(uri, departmentResponse.id())
					.contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isConflict())
				.andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.notNullValue()))
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.notNullValue()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.url", Matchers.notNullValue()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.httpMethod", Matchers.notNullValue()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.httpStatus", Matchers.is(HttpStatus.CONFLICT.name())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.errorDateTime", Matchers.notNullValue()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.errorMessage", Matchers.notNullValue()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.errorMessage",
						Matchers.containsString("Cannot delete department with ID [%s] as it still has employees!"
							.formatted(departmentResponse.id()))));
		}

		@Test
		@DisplayName("DELETE: 'http://.../departments/{id}' returns NO CONTENT if the specified department exists and is empty")
		void givenEmployee_whenDeleteDepartment_thenStatus204() throws Exception {
			// Arrange
			DepartmentResponse departmentResponse = saveRandomDepartment();
			String uri = "%s/{id}".formatted(DepartmentController.BASE_URI);

			// Act / Assert
			DepartmentControllerIntegrationTests.this.mockMvc
				.perform(MockMvcRequestBuilders.delete(uri, departmentResponse.id())
					.contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isNoContent());

			Optional<Department> optionalEmployee = DepartmentControllerIntegrationTests.this.departmentRepository
				.findById(departmentResponse.id());
			Assertions.assertThat(optionalEmployee).isEmpty();
		}

	}

}
