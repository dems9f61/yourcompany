package de.stminko.eventservice.employee.boundary;


import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import com.fasterxml.jackson.core.type.TypeReference;
import de.stminko.eventservice.AbstractIntegrationTestSuite;
import de.stminko.eventservice.employee.entity.EmployeeEventResponse;
import de.stminko.eventservice.employee.entity.PersistentEmployeeEvent;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
        MvcResult mvcResult = mockMvc.perform(get(uri, employeeId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", notNullValue()))
                .andExpect(jsonPath("$.content", hasSize(expectedEventCount))).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        Page<EmployeeEventResponse> employeeResponsePage = objectMapper.readValue(contentAsString, new TypeReference<>() {
        });

        List<EmployeeEventResponse> employeeEventResponses = employeeResponsePage.stream().toList();
        IntStream.range(0, employeeEventResponses.size() - 1)
                .forEach(i -> {
                    Instant current = employeeEventResponses.get(i).getCreatedAt();
                    Instant next = employeeEventResponses.get(i + 1).getCreatedAt();
                    assertThat(current).isBefore(next);
                });

    }

}
