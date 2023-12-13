package de.stminko.eventservice.employee.boundary;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import com.fasterxml.jackson.core.type.TypeReference;
import de.stminko.eventservice.AbstractIntegrationTestSuite;
import de.stminko.eventservice.CustomPageImpl;
import de.stminko.eventservice.employee.entity.EmployeeEventResponse;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.assertj.core.api.Assertions;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@ExtendWith(OutputCaptureExtension.class)
@AutoConfigureMockMvc
class EmployeeEventControllerIntegrationTests extends AbstractIntegrationTestSuite {

	@Autowired
	private MockMvc mockMvc;

	@Test
	@DisplayName("GET: 'hhtps://.../events/{employeeId}' returns OK and an list ")
	void givenEmployeeVents_whenFindByEmployeeId_thenStatus200AndContent() throws Exception {
		// Arrange
		String employeeId = UUID.randomUUID().toString();
		int expectedEventCount = RandomUtils.nextInt(10, 20);
		receiveRandomMessageFor(employeeId, expectedEventCount);

		String uri = String.format("%s/{employeeId}", EmployeeEventController.BASE_URI);

		// Act / Assert
		MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.get(uri, employeeId))
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.notNullValue()))
			.andExpect(MockMvcResultMatchers.jsonPath("$.content", Matchers.hasSize(expectedEventCount)))
			.andReturn();
		String contentAsString = mvcResult.getResponse().getContentAsString();
		CustomPageImpl<EmployeeEventResponse> employeeResponsePage = this.objectMapper.readValue(contentAsString,
				new TypeReference<>() {
				});

		List<EmployeeEventResponse> employeeEventResponses = employeeResponsePage.stream().toList();
		IntStream.range(0, employeeEventResponses.size() - 1).forEach((int i) -> {
			Instant current = employeeEventResponses.get(i).getCreatedAt();
			Instant next = employeeEventResponses.get(i + 1).getCreatedAt();
			Assertions.assertThat(current).isBefore(next);
		});

	}

	@Test
	@DisplayName("GET: 'hhtps://.../events/{employeeId}' returns INTERNAL_ERROR and ErrorInfo if some unexpected exception is raised ")
	void givenUnexpectedExceptionThrown_whenFindByEmployeeId_thenStatus500AndErrorInfo(CapturedOutput output)
			throws Exception {
		// Arrange
		String errorMessage = RandomStringUtils.randomAlphabetic(23);
		Mockito.doThrow(new RuntimeException(errorMessage))
			.when(this.employeeEventService)
			.findByEmployeeIdOrderByCreatedAtAsc(ArgumentMatchers.any(), ArgumentMatchers.any());
		String employeeId = UUID.randomUUID().toString();
		String uri = "%s/{employeeId}".formatted(EmployeeEventController.BASE_URI);

		// Act/Assert
		this.mockMvc.perform(MockMvcRequestBuilders.get(uri, employeeId))
			.andExpect(MockMvcResultMatchers.status().is5xxServerError())
			.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.notNullValue()))
			.andExpect(MockMvcResultMatchers.jsonPath("$.url", Matchers.is("/api/v1/events/%s".formatted(employeeId))))
			.andExpect(MockMvcResultMatchers.jsonPath("$.urlQueryString", Matchers.nullValue()))
			.andExpect(MockMvcResultMatchers.jsonPath("$.httpMethod", Matchers.is("GET")))
			.andExpect(MockMvcResultMatchers.jsonPath("$.httpStatus",
					Matchers.is(HttpStatus.INTERNAL_SERVER_ERROR.name())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.errorMessage", Matchers.is(errorMessage)));

		Assertions.assertThat(output.getOut())
			.contains("Unhandled Exception occurred. Error: %s".formatted(errorMessage));
	}

}
