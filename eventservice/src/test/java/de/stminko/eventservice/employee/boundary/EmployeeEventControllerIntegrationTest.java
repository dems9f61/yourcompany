package de.stminko.eventservice.employee.boundary;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import com.fasterxml.jackson.core.type.TypeReference;
import de.stminko.eventservice.AbstractIntegrationTestSuite;
import de.stminko.eventservice.employee.entity.EmployeeEventResponse;
import org.apache.commons.lang3.RandomUtils;
import org.assertj.core.api.Assertions;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@AutoConfigureMockMvc
class EmployeeEventControllerIntegrationTest extends AbstractIntegrationTestSuite {

	@Autowired
	private MockMvc mockMvc;

	@Test
	@DisplayName("GET: 'http://.../events/{employeeId}' returns OK and an list ")
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
		Page<EmployeeEventResponse> employeeResponsePage = this.objectMapper.readValue(contentAsString,
				new TypeReference<>() {
				});

		List<EmployeeEventResponse> employeeEventResponses = employeeResponsePage.stream().toList();
		IntStream.range(0, employeeEventResponses.size() - 1).forEach((int i) -> {
			Instant current = employeeEventResponses.get(i).getCreatedAt();
			Instant next = employeeEventResponses.get(i + 1).getCreatedAt();
			Assertions.assertThat(current).isBefore(next);
		});

	}

}
