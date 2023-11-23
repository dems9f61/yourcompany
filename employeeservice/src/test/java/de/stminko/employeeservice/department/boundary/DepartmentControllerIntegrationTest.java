package de.stminko.employeeservice.department.boundary;

import java.util.Optional;

import de.stminko.employeeservice.AbstractIntegrationTestSuite;
import de.stminko.employeeservice.department.control.DepartmentRepository;
import de.stminko.employeeservice.department.entity.Department;
import de.stminko.employeeservice.department.entity.DepartmentRequest;
import de.stminko.employeeservice.department.entity.DepartmentResponse;
import de.stminko.employeeservice.runtime.rest.bondary.DataView;
import org.assertj.core.api.Assertions;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class DepartmentControllerIntegrationTest extends AbstractIntegrationTestSuite {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private DepartmentRepository departmentRepository;

	@Nested
	@DisplayName("when new")
	class WhenCreate {

		@Test
		@DisplayName("POST: 'http://.../departments' returns BAD_REQUEST on empty department name")
		void givenEmptyDepartmentName_whenCreate_thenStatus400() throws Exception {
			// Arrange
			DepartmentRequest departmentRequest = departmentRequestTestFactory.builder().departmentName("").create();

			// Act / Assert
			mockMvc.perform(post(DepartmentController.BASE_URI).contentType(MediaType.APPLICATION_JSON_UTF8)
					.content(objectMapper.writeValueAsString(departmentRequest))).andExpect(status().isBadRequest());
		}

		@Test
		@DisplayName("POST: 'http://.../departments' returns BAD_REQUEST on blank department name")
		void givenBlankDepartmentName_whenCreate_thenStatus400() throws Exception {
			// Arrange
			DepartmentRequest departmentRequest = departmentRequestTestFactory.builder().departmentName("  ").create();

			// Act / Assert
			mockMvc.perform(post(DepartmentController.BASE_URI).contentType(MediaType.APPLICATION_JSON_UTF8)
					.content(objectMapper.writeValueAsString(departmentRequest))).andExpect(status().isBadRequest());
		}

		@Test
		@DisplayName("POST: 'http://.../departments' returns BAD_REQUEST on null department name")
		void givenNullDepartmentName_whenCreate_thenStatus400() throws Exception {
			// Arrange
			DepartmentRequest departmentRequest = departmentRequestTestFactory.builder().departmentName("").create();

			// Act / Assert
			mockMvc.perform(post(DepartmentController.BASE_URI).contentType(MediaType.APPLICATION_JSON_UTF8)
					.content(objectMapper.writeValueAsString(departmentRequest))).andExpect(status().isBadRequest());
		}

		@Test
		@DisplayName("POST: 'http://.../departments' returns CREATED on valid request")
		void givenValidDepartmentRequest_whenCreate_thenStatus201() throws Exception {
			// Arrange
			DepartmentRequest departmentRequest = departmentRequestTestFactory.createDefault();
			String requestAsJson = transformRequestToJSONByView(departmentRequest, DataView.POST.class);

			// Act / Assert
			MvcResult mvcResult = mockMvc
					.perform(post(DepartmentController.BASE_URI).contentType(MediaType.APPLICATION_JSON)
							.content(requestAsJson))
					.andExpect(status().isCreated())
					.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
					.andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.notNullValue()))
					.andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.notNullValue()))
					.andExpect(MockMvcResultMatchers.jsonPath("$.departmentName",
							Matchers.is(departmentRequest.departmentName())))
					.andExpect(MockMvcResultMatchers.header().string(HttpHeaders.LOCATION,
							Matchers.containsString(DepartmentController.BASE_URI)))
					.andReturn();
			String contentAsString = mvcResult.getResponse().getContentAsString();
			DepartmentResponse departmentResponse = objectMapper.readValue(contentAsString, DepartmentResponse.class);
			Optional<Department> optionalDepartment = departmentRepository.findById(departmentResponse.id());
			Assertions.assertThat(optionalDepartment).hasValueSatisfying((Department value) -> Assertions.assertThat(value).isNotNull());
		}

	}

}