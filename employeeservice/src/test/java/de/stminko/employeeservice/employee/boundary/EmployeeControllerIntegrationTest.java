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
import de.stminko.employeeservice.department.entity.DepartmentRequest;
import de.stminko.employeeservice.department.entity.DepartmentResponse;
import de.stminko.employeeservice.employee.control.EmployeeRepository;
import de.stminko.employeeservice.employee.entity.Employee;
import de.stminko.employeeservice.employee.entity.EmployeeRequest;
import de.stminko.employeeservice.employee.entity.EmployeeResponse;
import de.stminko.employeeservice.employee.entity.UsableDateFormat;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@AutoConfigureMockMvc
class EmployeeControllerIntegrationTest extends AbstractIntegrationTestSuite {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EmployeeRepository employeeRepository;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(UsableDateFormat.DEFAULT.getDateFormat());


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
            EmployeeRequest employeeRequest = employeeRequestTestFactory.builder()
                    .departmentName(departmentResponse.departmentName()).create();

            String requestAsJson = transformRequestToJSONByView(employeeRequest, DataView.POST.class);
            MvcResult mvcResult = mockMvc.perform(
                            MockMvcRequestBuilders.post(uri).contentType(MediaType.APPLICATION_JSON).content(requestAsJson))
                    .andReturn();
            String contentAsString = mvcResult.getResponse().getContentAsString();
            result.add(this.objectMapper.readValue(contentAsString, EmployeeResponse.class));
        }
        return result;
    }

    private DepartmentResponse saveRandomDepartment() throws Exception {
        DepartmentRequest departmentRequest = departmentRequestTestFactory.createDefault();
        String departmentRequestAsJson = transformRequestToJSONByView(departmentRequest, DataView.POST.class);
        MvcResult departmentMvcResult = mockMvc.perform(post(DepartmentController.BASE_URI).contentType(MediaType.APPLICATION_JSON)
                .content(departmentRequestAsJson)).andReturn();
        return objectMapper.readValue(departmentMvcResult.getResponse()
                .getContentAsString(), DepartmentResponse.class);
    }

    @Nested
    @DisplayName("when create")
    class WhenCreate {

        @Test
        @DisplayName("POST: 'http://.../employees' returns CREATED for valid request")
        void givenValidRequest_whenCreateEmployee_thenStatus201() throws Exception {
            // Arrange
            DepartmentResponse departmentResponse = saveRandomDepartment();
            EmployeeRequest toPersist = employeeRequestTestFactory.builder()
                    .departmentName(departmentResponse.departmentName()).create();
            String requestAsJson = transformRequestToJSONByView(toPersist, DataView.POST.class);
            String uri = "%s".formatted(EmployeeController.BASE_URI);
            String formattedBirthday = dateFormatter.format(toPersist.birthday());


            // Act / Assert
            MvcResult mvcResult = mockMvc.perform(post(uri).contentType(MediaType.APPLICATION_JSON)
                            .content(requestAsJson))
                    .andExpect(MockMvcResultMatchers.status().isCreated())
                    .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.notNullValue()))
                    .andExpect(MockMvcResultMatchers.header().string(HttpHeaders.LOCATION,
                            Matchers.containsString(EmployeeController.BASE_URI)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.notNullValue()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.notNullValue()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.firstName", Matchers.is(toPersist.firstName())))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.lastName", Matchers.is(toPersist.lastName())))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.birthday", Matchers.is(formattedBirthday)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.emailAddress", Matchers.is(toPersist.emailAddress())))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.departmentName", Matchers.is(toPersist.departmentName()))).
                    andReturn();
            String contentAsString = mvcResult.getResponse().getContentAsString();
            EmployeeResponse employeeResponse = objectMapper.readValue(contentAsString, EmployeeResponse.class);
            Optional<Employee> optionalEmployee = employeeRepository.findById(employeeResponse.id());
            Assertions.assertThat(optionalEmployee).hasValueSatisfying((Employee value) -> Assertions.assertThat(value).isNotNull());

        }

        @Test
        @DisplayName("POST: 'http://.../employees' returns CREATED even if only the department name is specified")
        void givenOnlyDepartment_whenCreateEmployee_thenStatus201() throws Exception {
            // Arrange
            DepartmentResponse departmentResponse = saveRandomDepartment();
            EmployeeRequest toPersist = employeeRequestTestFactory
                    .builder()
                    .birthday(null)
                    .emailAddress(null)
                    .firstName(null)
                    .lastName(null)
                    .departmentName(departmentResponse.departmentName())
                    .create();

            String requestAsJson = transformRequestToJSONByView(toPersist, DataView.POST.class);
            String uri = "%s".formatted(EmployeeController.BASE_URI);

            // Act / Assert
            MvcResult mvcResult = mockMvc.perform(post(uri).contentType(MediaType.APPLICATION_JSON)
                            .content(requestAsJson))
                    .andExpect(MockMvcResultMatchers.status().isCreated())
                    .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.notNullValue()))
                    .andExpect(MockMvcResultMatchers.header().string(HttpHeaders.LOCATION,
                            Matchers.containsString(EmployeeController.BASE_URI)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.notNullValue()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.notNullValue()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.firstName", Matchers.nullValue()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.lastName", Matchers.nullValue()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.birthday", Matchers.nullValue()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.emailAddress", Matchers.nullValue()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.departmentName", Matchers.is(toPersist.departmentName()))).
                    andReturn();
            String contentAsString = mvcResult.getResponse().getContentAsString();
            EmployeeResponse employeeResponse = objectMapper.readValue(contentAsString, EmployeeResponse.class);
            Optional<Employee> optionalEmployee = employeeRepository.findById(employeeResponse.id());
            Assertions.assertThat(optionalEmployee).hasValueSatisfying((Employee value) -> Assertions.assertThat(value).isNotNull());
        }

        @Test
        @DisplayName("POST: 'http://.../employees' returns BAD REQUEST if the specified department name doesn't exist ")
        void givenUnknownDepartment_whenCreateEmployee_thenStatus400() throws Exception {
            // Arrange
            EmployeeRequest toPersist = employeeRequestTestFactory.createDefault();
            String requestAsJson = transformRequestToJSONByView(toPersist, DataView.POST.class);
            String uri = "%s".formatted(EmployeeController.BASE_URI);

            // Act / Assert
            mockMvc.perform(post(uri).contentType(MediaType.APPLICATION_JSON)
                            .content(requestAsJson))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.notNullValue()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.url", Matchers.notNullValue()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.httpMethod", Matchers.notNullValue()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.httpStatus", Matchers.is(HttpStatus.BAD_REQUEST.name())))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.errorDateTime", Matchers.notNullValue()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.errorMessage", Matchers.notNullValue()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.errorMessage", Matchers.containsString("Could not find Department by the name [%s]".formatted(toPersist.departmentName()))));
        }

        @Test
        @DisplayName("POST: 'http://.../employees' returns BAD REQUEST if the specified email is not valid ")
        void givenInvalidEmail_whenCreateEmployee_thenStatus400() throws Exception {
            // Arrange
            EmployeeRequest toPersist = employeeRequestTestFactory.builder()
                    .emailAddress(RandomStringUtils.randomAlphabetic(10)).create();
            String requestAsJson = transformRequestToJSONByView(toPersist, DataView.POST.class);
            String uri = "%s".formatted(EmployeeController.BASE_URI);

            // Act / Assert
            mockMvc.perform(post(uri).contentType(MediaType.APPLICATION_JSON)
                            .content(requestAsJson))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.notNullValue()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.url", Matchers.notNullValue()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.httpMethod", Matchers.notNullValue()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.httpStatus", Matchers.is(HttpStatus.BAD_REQUEST.name())))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.errorDateTime", Matchers.notNullValue()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.errorMessage", Matchers.notNullValue()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.errorMessage", Matchers
                            .containsString("must match \"" + EmployeeRequest.EMAIL_REGEX + "\"")));
        }

        @Test
        @DisplayName("POST: 'http://.../employees' returns BAD REQUEST if the specified email already exists ")
        void givenAlreadyUsedEmail_whenCreateEmployee_thenStatus400() throws Exception {
            // Arrange
            DepartmentResponse departmentResponse = saveRandomDepartment();
            EmployeeRequest firstEmployeeRequest = employeeRequestTestFactory.builder().departmentName(departmentResponse.departmentName()).create();

            String requestAsJson = transformRequestToJSONByView(firstEmployeeRequest, DataView.POST.class);
            String uri = "%s".formatted(EmployeeController.BASE_URI);
            mockMvc.perform(MockMvcRequestBuilders.post(uri).contentType(MediaType.APPLICATION_JSON).content(requestAsJson));

            EmployeeRequest secondEmployeeRequest = employeeRequestTestFactory.builder()
                    .departmentName(departmentResponse.departmentName())
                    .emailAddress(firstEmployeeRequest.emailAddress()).create();

            // Act / Assert
            mockMvc.perform(post(uri).contentType(MediaType.APPLICATION_JSON)
                            .content(requestAsJson))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.notNullValue()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.url", Matchers.notNullValue()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.httpMethod", Matchers.notNullValue()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.httpStatus", Matchers.is(HttpStatus.BAD_REQUEST.name())))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.errorDateTime", Matchers.notNullValue()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.errorMessage", Matchers.notNullValue()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.errorMessage", Matchers.containsString("Email [%s] is already used"
                            .formatted(secondEmployeeRequest.emailAddress()))));
        }

        @Test
        @DisplayName("POST: 'http://.../employees' returns BAD REQUEST if no field is set")
        void givenEmptyRequest_whenCreateEmployee_thenStatus400() throws Exception {
            // Arrange
            EmployeeRequest toPersist = employeeRequestTestFactory
                    .builder()
                    .birthday(null)
                    .emailAddress(null)
                    .firstName(null)
                    .lastName(null)
                    .departmentName(null)
                    .create();

            String requestAsJson = transformRequestToJSONByView(toPersist, DataView.POST.class);
            String uri = "%s".formatted(EmployeeController.BASE_URI);

            // Act / Assert
            mockMvc.perform(post(uri).contentType(MediaType.APPLICATION_JSON)
                            .content(requestAsJson))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.notNullValue()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.url", Matchers.notNullValue()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.httpMethod", Matchers.notNullValue()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.httpStatus", Matchers.is(HttpStatus.BAD_REQUEST.name())))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.errorDateTime", Matchers.notNullValue()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.errorMessage", Matchers.notNullValue()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.errorMessage", Matchers.containsString("departmentName: must not be blank")));
        }
    }

    @Nested
    @DisplayName("when access")
    class WhenAccess {

        @Test
        @DisplayName("GET: 'https://.../employees/{id}' returns NOT FOUND for unknown id ")
        void givenUnknownId_whenFindById_thenStatus404() throws Exception {
            // Arrange
            String unknownId = UUID.randomUUID().toString();
            String uri = "%s/{id}".formatted(EmployeeController.BASE_URI);

            // Act / Assert
            mockMvc.perform(MockMvcRequestBuilders.get(uri, unknownId).contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isNotFound())
                    .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.notNullValue()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.errorMessage", Matchers
                            .containsString(String.format("Could not find employee for Id [%s]", unknownId))));
        }

        @Test
        @DisplayName("GET: 'https://.../employees/{id}' returns OK and valid employee response")
        void givenEmployee_whenFindById_thenStatus200AndReturnValidEmployeeResponse() throws Exception {
            // Arrange
            EmployeeResponse persisted = saveRandomEmployees(1).get(0);
            String uri = "%s/{id}".formatted(EmployeeController.BASE_URI);
            String formattedBirthday = dateFormatter.format(persisted.birthday());

            // Act / Assert
            MvcResult mvcResult = mockMvc
                    .perform(MockMvcRequestBuilders.get(uri, persisted.id())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.notNullValue()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.departmentName", Matchers.is(persisted.departmentName())))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.is(persisted.id())))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.birthday", Matchers.is(formattedBirthday)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.firstName", Matchers.is(persisted.firstName())))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.lastName", Matchers.is(persisted.lastName())))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.departmentName", Matchers.is(persisted.departmentName())))
                    .andReturn();

            String contentAsString = mvcResult.getResponse().getContentAsString();
            EmployeeResponse employeeResponse = objectMapper.readValue(contentAsString, EmployeeResponse.class);
            Assertions.assertThat(employeeResponse).isNotNull();
            Assertions.assertThat(employeeResponse.id()).isEqualTo(persisted.id());
            Assertions.assertThat(employeeResponse.birthday()).isEqualTo(persisted.birthday());
            Assertions.assertThat(employeeResponse.firstName()).isEqualTo(persisted.firstName());
            Assertions.assertThat(employeeResponse.lastName()).isEqualTo(persisted.lastName());
            Assertions.assertThat(employeeResponse.departmentName()).isEqualTo(persisted.departmentName());
        }


        @Test
        @DisplayName("GET: 'https://.../employees/{id}' returns OK and valid employee response on multiple employees")
        void givenEmployees_whenFindById_thenStatus200AndReturnValidEmployeeResponse() throws Exception {
            // Arrange
            int count = 10;
            EmployeeResponse persisted = saveRandomEmployees(count).get(RandomUtils.nextInt(0, count - 1));
            String uri = "%s/{id}".formatted(EmployeeController.BASE_URI);

            // Act / Assert
            mockMvc.perform(MockMvcRequestBuilders.get(uri, persisted.id())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.notNullValue()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.departmentName", Matchers.is(persisted.departmentName())))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.is(persisted.id())))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.birthday", Matchers.is(dateFormatter.format(persisted.birthday()))))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.firstName", Matchers.is(persisted.firstName())))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.lastName", Matchers.is(persisted.lastName())))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.departmentName", Matchers.is(persisted.departmentName())));
        }

    }

    @Nested
    @DisplayName("when partial update")
    class WhenPartialUpdate {

        @Test
        @DisplayName("PATCH: 'http://.../employees/{id}' returns NOT FOUND if the specified employee doesn't exist ")
        void givenUnknownEmployeeId_whenPartialUpdateEmployee_thenStatus404() throws Exception {
            // Arrange
            EmployeeRequest updateRequest = employeeRequestTestFactory.createDefault();
            String uri = "%s/{id}".formatted(EmployeeController.BASE_URI);
            String requestAsJson = transformRequestToJSONByView(updateRequest, DataView.PATCH.class);
            String wrongId = UUID.randomUUID().toString();

            // Act / Assert
            mockMvc.perform(MockMvcRequestBuilders.patch(uri, wrongId).contentType(MediaType.APPLICATION_JSON)
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
                            .containsString("Could not find employee for Id [%s]!".formatted(wrongId))));
        }

        @Test
        @DisplayName("PATCH: 'http://.../employees/{id}' returns BAD REQUEST if the specified department doesn't exist ")
        void givenUnknownDepartment_whenPartialUpdateEmployee_thenStatus400() throws Exception {
            // Arrange
            DepartmentResponse departmentResponse = saveRandomDepartment();
            EmployeeRequest initialEmployeeRequest = employeeRequestTestFactory.builder().departmentName(departmentResponse.departmentName()).create();
            String requestAsJson = transformRequestToJSONByView(initialEmployeeRequest, DataView.POST.class);
            String createUri = "%s".formatted(EmployeeController.BASE_URI);
            MvcResult mvcResult = mockMvc.perform(post(createUri).contentType(MediaType.APPLICATION_JSON)
                    .content(requestAsJson)).andReturn();
            EmployeeResponse employeeResponse = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), EmployeeResponse.class);

            String newDepartmentName = RandomStringUtils.randomAlphabetic(10);
            EmployeeRequest updateEmployeeRequest = employeeRequestTestFactory.builder().departmentName(newDepartmentName).create();
            String updateRequestAsJson = transformRequestToJSONByView(updateEmployeeRequest, DataView.PATCH.class);
            String patchUri = "%s/{id}".formatted(EmployeeController.BASE_URI);

            // Act / Assert
            mockMvc.perform(MockMvcRequestBuilders.patch(patchUri, employeeResponse.id()).contentType(MediaType.APPLICATION_JSON)
                            .content(updateRequestAsJson))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.notNullValue()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.url", Matchers.notNullValue()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.httpMethod", Matchers.notNullValue()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.httpStatus", Matchers.is(HttpStatus.BAD_REQUEST.name())))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.errorDateTime", Matchers.notNullValue()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.errorMessage", Matchers.notNullValue()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.errorMessage", Matchers
                            .containsString("Could not find Department by the name [%s]!".formatted(newDepartmentName))));
        }

        @Test
        @DisplayName("PATCH: 'http://.../employees/{id} returns NO CONTENT if the specified request (all fields set) is valid ")
        void givenValidRequestWithAllFieldsSet_whenPartialUpdateEmployee_thenStatus204() throws Exception {
            // Arrange
            DepartmentResponse departmentResponse = saveRandomDepartment();
            EmployeeRequest initialEmployeeRequest = employeeRequestTestFactory.builder().departmentName(departmentResponse.departmentName()).create();
            String requestAsJson = transformRequestToJSONByView(initialEmployeeRequest, DataView.POST.class);
            String createUri = "%s".formatted(EmployeeController.BASE_URI);
            MvcResult mvcResult = mockMvc.perform(post(createUri).contentType(MediaType.APPLICATION_JSON)
                    .content(requestAsJson)).andReturn();
            EmployeeResponse employeeResponse = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), EmployeeResponse.class);

            DepartmentResponse otherDepartmentResponse = saveRandomDepartment();
            EmployeeRequest updateEmployeeRequest = employeeRequestTestFactory.builder()
                    .departmentName(otherDepartmentResponse.departmentName()).create();
            String updateRequestAsJson = transformRequestToJSONByView(updateEmployeeRequest, DataView.PATCH.class);
            String patchUri = "%s/{id}".formatted(EmployeeController.BASE_URI);

            // Act / Assert
            mockMvc.perform(MockMvcRequestBuilders.patch(patchUri, employeeResponse.id()).contentType(MediaType.APPLICATION_JSON)
                            .content(updateRequestAsJson))
                    .andExpect(MockMvcResultMatchers.status().isNoContent());

            Employee updateEmployee = employeeRepository.findById(employeeResponse.id()).orElseThrow();
            Assertions.assertThat(dateFormatter.format(updateEmployee.getBirthday()))
                    .isEqualTo(dateFormatter.format(updateEmployeeRequest.birthday()));
            Assertions.assertThat(updateEmployee.getFullName().getFirstName()).isEqualTo(updateEmployeeRequest.firstName());
            Assertions.assertThat(updateEmployee.getFullName().getLastName()).isEqualTo(updateEmployeeRequest.lastName());
            Assertions.assertThat(updateEmployee.getEmailAddress()).isEqualTo(updateEmployeeRequest.emailAddress());
            Assertions.assertThat(updateEmployee.getDepartment().getDepartmentName())
                    .isEqualTo(updateEmployeeRequest.departmentName());
        }

        @Test
        @DisplayName("PATCH: 'http://.../employees/{id} returns NO CONTENT on only updating birthday")
        void givenNewBirthDay_whenPartialUpdateEmployee_thenStatus204andUpdateOnlyBirthDay() throws Exception {
            // Arrange
            DepartmentResponse departmentResponse = saveRandomDepartment();
            EmployeeRequest initialEmployeeRequest = employeeRequestTestFactory.builder().departmentName(departmentResponse.departmentName()).create();
            String requestAsJson = transformRequestToJSONByView(initialEmployeeRequest, DataView.POST.class);
            String createUri = "%s".formatted(EmployeeController.BASE_URI);
            MvcResult mvcResult = mockMvc.perform(post(createUri).contentType(MediaType.APPLICATION_JSON)
                    .content(requestAsJson)).andReturn();
            EmployeeResponse employeeResponse = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), EmployeeResponse.class);

            LocalDate localDate = LocalDate.parse("1979-12-03", dateFormatter);
            ZonedDateTime newBirthDay = localDate.atStartOfDay(ZoneId.systemDefault());
            EmployeeRequest updateEmployeeRequest = employeeRequestTestFactory.builder()
                    .departmentName(null)
                    .emailAddress(null)
                    .firstName(null)
                    .lastName(null)
                    .birthday(newBirthDay).create();
            String updateRequestAsJson = transformRequestToJSONByView(updateEmployeeRequest, DataView.PATCH.class);
            String patchUri = "%s/{id}".formatted(EmployeeController.BASE_URI);

            // Act / Assert
            mockMvc.perform(MockMvcRequestBuilders.patch(patchUri, employeeResponse.id()).contentType(MediaType.APPLICATION_JSON)
                            .content(updateRequestAsJson))
                    .andExpect(MockMvcResultMatchers.status().isNoContent());

            Employee updateEmployee = employeeRepository.findById(employeeResponse.id()).orElseThrow();
            Assertions.assertThat(dateFormatter.format(updateEmployee.getBirthday()))
                    .isEqualTo(dateFormatter.format(updateEmployeeRequest.birthday()));
            Assertions.assertThat(updateEmployee.getFullName().getFirstName()).isEqualTo(employeeResponse.firstName());
            Assertions.assertThat(updateEmployee.getFullName().getLastName()).isEqualTo(employeeResponse.lastName());
            Assertions.assertThat(updateEmployee.getEmailAddress()).isEqualTo(employeeResponse.emailAddress());
            Assertions.assertThat(updateEmployee.getDepartment().getDepartmentName())
                    .isEqualTo(employeeResponse.departmentName());

        }

        @Test
        @DisplayName("PATCH: 'http://.../employees/{id} returns NO CONTENT on only updating first name")
        void givenNewFirstName_whenPartialUpdateEmployee_thenStatus204andUpdateOnlyFirstName() throws Exception {
            // Arrange
            DepartmentResponse departmentResponse = saveRandomDepartment();
            EmployeeRequest initialEmployeeRequest = employeeRequestTestFactory.builder().departmentName(departmentResponse.departmentName()).create();
            String requestAsJson = transformRequestToJSONByView(initialEmployeeRequest, DataView.POST.class);
            String createUri = "%s".formatted(EmployeeController.BASE_URI);
            MvcResult mvcResult = mockMvc.perform(post(createUri).contentType(MediaType.APPLICATION_JSON)
                    .content(requestAsJson)).andReturn();
            EmployeeResponse employeeResponse = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), EmployeeResponse.class);
            String newFirstName = RandomStringUtils.randomAlphabetic(23);
            EmployeeRequest updateEmployeeRequest = employeeRequestTestFactory.builder()
                    .departmentName(null)
                    .emailAddress(null)
                    .firstName(newFirstName)
                    .lastName(null)
                    .birthday(null).create();
            String updateRequestAsJson = transformRequestToJSONByView(updateEmployeeRequest, DataView.PATCH.class);
            String patchUri = "%s/{id}".formatted(EmployeeController.BASE_URI);

            // Act / Assert
            mockMvc.perform(MockMvcRequestBuilders.patch(patchUri, employeeResponse.id()).contentType(MediaType.APPLICATION_JSON)
                            .content(updateRequestAsJson))
                    .andExpect(MockMvcResultMatchers.status().isNoContent());

            Employee updateEmployee = employeeRepository.findById(employeeResponse.id()).orElseThrow();
            Assertions.assertThat(dateFormatter.format(updateEmployee.getBirthday()))
                    .isEqualTo(dateFormatter.format(employeeResponse.birthday()));
            Assertions.assertThat(updateEmployee.getFullName().getFirstName()).isEqualTo(updateEmployeeRequest.firstName());
            Assertions.assertThat(updateEmployee.getFullName().getLastName()).isEqualTo(employeeResponse.lastName());
            Assertions.assertThat(updateEmployee.getEmailAddress()).isEqualTo(employeeResponse.emailAddress());
            Assertions.assertThat(updateEmployee.getDepartment().getDepartmentName())
                    .isEqualTo(employeeResponse.departmentName());

        }

        @Test
        @DisplayName("PATCH: 'http://.../employees/{id} returns BAD REQUEST on only updating first name")
        void givenBlankFirstName_whenPartialUpdateEmployee_thenStatus400() throws Exception {
            // Arrange
            DepartmentResponse departmentResponse = saveRandomDepartment();
            EmployeeRequest initialEmployeeRequest = employeeRequestTestFactory.builder().departmentName(departmentResponse.departmentName()).create();
            String requestAsJson = transformRequestToJSONByView(initialEmployeeRequest, DataView.POST.class);
            String createUri = "%s".formatted(EmployeeController.BASE_URI);
            MvcResult mvcResult = mockMvc.perform(post(createUri).contentType(MediaType.APPLICATION_JSON)
                    .content(requestAsJson)).andReturn();
            EmployeeResponse employeeResponse = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), EmployeeResponse.class);
            EmployeeRequest updateEmployeeRequest = employeeRequestTestFactory.builder()
                    .departmentName(null)
                    .emailAddress(null)
                    .firstName("   ")
                    .lastName(null)
                    .birthday(null).create();
            String updateRequestAsJson = transformRequestToJSONByView(updateEmployeeRequest, DataView.PATCH.class);
            String patchUri = "%s/{id}".formatted(EmployeeController.BASE_URI);

            // Act / Assert
            mockMvc.perform(MockMvcRequestBuilders.patch(patchUri, employeeResponse.id()).contentType(MediaType.APPLICATION_JSON)
                            .content(updateRequestAsJson))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.notNullValue()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.url", Matchers.notNullValue()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.httpMethod", Matchers.notNullValue()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.httpStatus", Matchers.is(HttpStatus.BAD_REQUEST.name())))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.errorDateTime", Matchers.notNullValue()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.errorMessage", Matchers.notNullValue()))
                    .andExpect(MockMvcResultMatchers
                            .jsonPath("$.errorMessage",
                                    Matchers.containsString("firstName: The value: [   ] must either be 'null' or contain a non empty trimmed value")));
        }

        @Test
        @DisplayName("PATCH: 'http://.../employees/{id} returns NO CONTENT on only updating last name")
        void givenNewLastName_whenPartialUpdateEmployee_thenStatus204andUpdateOnlyLastName() throws Exception {
            // Arrange
            DepartmentResponse departmentResponse = saveRandomDepartment();
            EmployeeRequest initialEmployeeRequest = employeeRequestTestFactory.builder().departmentName(departmentResponse.departmentName()).create();
            String requestAsJson = transformRequestToJSONByView(initialEmployeeRequest, DataView.POST.class);
            String createUri = "%s".formatted(EmployeeController.BASE_URI);
            MvcResult mvcResult = mockMvc.perform(post(createUri).contentType(MediaType.APPLICATION_JSON)
                    .content(requestAsJson)).andReturn();
            EmployeeResponse employeeResponse = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), EmployeeResponse.class);
            String newLastName = RandomStringUtils.randomAlphabetic(23);
            EmployeeRequest updateEmployeeRequest = employeeRequestTestFactory.builder()
                    .departmentName(null)
                    .emailAddress(null)
                    .firstName(null)
                    .lastName(newLastName)
                    .birthday(null).create();
            String updateRequestAsJson = transformRequestToJSONByView(updateEmployeeRequest, DataView.PATCH.class);
            String patchUri = "%s/{id}".formatted(EmployeeController.BASE_URI);

            // Act / Assert
            mockMvc.perform(MockMvcRequestBuilders.patch(patchUri, employeeResponse.id()).contentType(MediaType.APPLICATION_JSON)
                            .content(updateRequestAsJson))
                    .andExpect(MockMvcResultMatchers.status().isNoContent());

            Employee updateEmployee = employeeRepository.findById(employeeResponse.id()).orElseThrow();
            Assertions.assertThat(dateFormatter.format(updateEmployee.getBirthday()))
                    .isEqualTo(dateFormatter.format(employeeResponse.birthday()));
            Assertions.assertThat(updateEmployee.getFullName().getFirstName()).isEqualTo(employeeResponse.firstName());
            Assertions.assertThat(updateEmployee.getFullName().getLastName()).isEqualTo(updateEmployeeRequest.lastName());
            Assertions.assertThat(updateEmployee.getEmailAddress()).isEqualTo(employeeResponse.emailAddress());
            Assertions.assertThat(updateEmployee.getDepartment().getDepartmentName())
                    .isEqualTo(employeeResponse.departmentName());

        }

        @Test
        @DisplayName("PATCH: 'http://.../employees/{id} returns NO CONTENT on only updating email")
        void givenNewEmail_whenPartialUpdateEmployee_thenStatus204andUpdateOnlyEmail() throws Exception {
            // Arrange
            DepartmentResponse departmentResponse = saveRandomDepartment();
            EmployeeRequest initialEmployeeRequest = employeeRequestTestFactory.builder().departmentName(departmentResponse.departmentName()).create();
            String requestAsJson = transformRequestToJSONByView(initialEmployeeRequest, DataView.POST.class);
            String createUri = "%s".formatted(EmployeeController.BASE_URI);
            MvcResult mvcResult = mockMvc.perform(post(createUri).contentType(MediaType.APPLICATION_JSON)
                    .content(requestAsJson)).andReturn();
            EmployeeResponse employeeResponse = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), EmployeeResponse.class);
            String newEmail = generateRandomEmail();
            EmployeeRequest updateEmployeeRequest = employeeRequestTestFactory.builder()
                    .departmentName(null)
                    .emailAddress(newEmail)
                    .firstName(null)
                    .lastName(null)
                    .birthday(null).create();
            String updateRequestAsJson = transformRequestToJSONByView(updateEmployeeRequest, DataView.PATCH.class);
            String patchUri = "%s/{id}".formatted(EmployeeController.BASE_URI);

            // Act / Assert
            mockMvc.perform(MockMvcRequestBuilders.patch(patchUri, employeeResponse.id()).contentType(MediaType.APPLICATION_JSON)
                            .content(updateRequestAsJson))
                    .andExpect(MockMvcResultMatchers.status().isNoContent());

            Employee updateEmployee = employeeRepository.findById(employeeResponse.id()).orElseThrow();
            Assertions.assertThat(dateFormatter.format(updateEmployee.getBirthday()))
                    .isEqualTo(dateFormatter.format(employeeResponse.birthday()));
            Assertions.assertThat(updateEmployee.getFullName().getFirstName()).isEqualTo(employeeResponse.firstName());
            Assertions.assertThat(updateEmployee.getFullName().getLastName()).isEqualTo(employeeResponse.lastName());
            Assertions.assertThat(updateEmployee.getEmailAddress()).isEqualTo(updateEmployeeRequest.emailAddress());
            Assertions.assertThat(updateEmployee.getDepartment().getDepartmentName())
                    .isEqualTo(employeeResponse.departmentName());

        }

        @Test
        @DisplayName("PATCH: 'http://.../employees/{id} returns BAD REQUEST on invalid email")
        void givenNewInvalidEmail_whenPartialUpdateEmployee_thenStatus400() throws Exception {
            // Arrange
            DepartmentResponse departmentResponse = saveRandomDepartment();
            EmployeeRequest initialEmployeeRequest = employeeRequestTestFactory.builder().departmentName(departmentResponse.departmentName()).create();
            String requestAsJson = transformRequestToJSONByView(initialEmployeeRequest, DataView.POST.class);
            String createUri = "%s".formatted(EmployeeController.BASE_URI);
            MvcResult mvcResult = mockMvc.perform(post(createUri).contentType(MediaType.APPLICATION_JSON)
                    .content(requestAsJson)).andReturn();
            EmployeeResponse employeeResponse = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), EmployeeResponse.class);
            String newEmail = RandomStringUtils.randomAlphabetic(23);
            EmployeeRequest updateEmployeeRequest = employeeRequestTestFactory.builder()
                    .departmentName(null)
                    .emailAddress(newEmail)
                    .firstName(null)
                    .lastName(null)
                    .birthday(null).create();
            String updateRequestAsJson = transformRequestToJSONByView(updateEmployeeRequest, DataView.PATCH.class);
            String patchUri = "%s/{id}".formatted(EmployeeController.BASE_URI);

            // Act / Assert
            mockMvc.perform(MockMvcRequestBuilders.patch(patchUri, employeeResponse.id()).contentType(MediaType.APPLICATION_JSON)
                            .content(updateRequestAsJson))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.notNullValue()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.url", Matchers.notNullValue()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.httpMethod", Matchers.notNullValue()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.httpStatus", Matchers.is(HttpStatus.BAD_REQUEST.name())))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.errorDateTime", Matchers.notNullValue()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.errorMessage", Matchers.notNullValue()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.errorMessage", Matchers
                            .containsString("must match \"" + EmployeeRequest.EMAIL_REGEX + "\"")));

        }

        @Test
        @DisplayName("PATCH: 'http://.../employees/{id} returns NO CONTENT on only updating department")
        void givenNewDepartment_whenPartialUpdateEmployee_thenStatus204andUpdateOnlyDeparmtent() throws Exception {
            // Arrange
            DepartmentResponse departmentResponse = saveRandomDepartment();
            EmployeeRequest initialEmployeeRequest = employeeRequestTestFactory.builder().departmentName(departmentResponse.departmentName()).create();
            String requestAsJson = transformRequestToJSONByView(initialEmployeeRequest, DataView.POST.class);
            String createUri = "%s".formatted(EmployeeController.BASE_URI);
            MvcResult mvcResult = mockMvc.perform(post(createUri).contentType(MediaType.APPLICATION_JSON)
                    .content(requestAsJson)).andReturn();
            EmployeeResponse employeeResponse = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), EmployeeResponse.class);

            DepartmentResponse newDepartmentResponse = saveRandomDepartment();
            EmployeeRequest updateEmployeeRequest = employeeRequestTestFactory.builder()
                    .departmentName(newDepartmentResponse.departmentName())
                    .emailAddress(null)
                    .firstName(null)
                    .lastName(null)
                    .birthday(null).create();
            String updateRequestAsJson = transformRequestToJSONByView(updateEmployeeRequest, DataView.PATCH.class);
            String patchUri = "%s/{id}".formatted(EmployeeController.BASE_URI);

            // Act / Assert
            mockMvc.perform(MockMvcRequestBuilders.patch(patchUri, employeeResponse.id()).contentType(MediaType.APPLICATION_JSON)
                            .content(updateRequestAsJson))
                    .andExpect(MockMvcResultMatchers.status().isNoContent());

            Employee updateEmployee = employeeRepository.findById(employeeResponse.id()).orElseThrow();
            Assertions.assertThat(dateFormatter.format(updateEmployee.getBirthday()))
                    .isEqualTo(dateFormatter.format(employeeResponse.birthday()));
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
        @DisplayName("PUT: 'http://.../employees/{id} returns NO CONTENT if the specified request (all fields set) is valid ")
        void givenValidRequestWithAllFieldsSet_whenFullUpdateEmployee_thenStatus204() throws Exception {
            // Arrange
            DepartmentResponse departmentResponse = saveRandomDepartment();
            EmployeeRequest initialEmployeeRequest = employeeRequestTestFactory.builder().departmentName(departmentResponse.departmentName()).create();
            String requestAsJson = transformRequestToJSONByView(initialEmployeeRequest, DataView.POST.class);
            String createUri = "%s".formatted(EmployeeController.BASE_URI);
            MvcResult mvcResult = mockMvc.perform(post(createUri).contentType(MediaType.APPLICATION_JSON)
                    .content(requestAsJson)).andReturn();
            EmployeeResponse employeeResponse = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), EmployeeResponse.class);

            DepartmentResponse otherDepartmentResponse = saveRandomDepartment();
            EmployeeRequest updateEmployeeRequest = employeeRequestTestFactory.builder()
                    .departmentName(otherDepartmentResponse.departmentName()).create();
            String updateRequestAsJson = transformRequestToJSONByView(updateEmployeeRequest, DataView.PUT.class);
            String putUri = "%s/{id}".formatted(EmployeeController.BASE_URI);

            // Act / Assert
            mockMvc.perform(MockMvcRequestBuilders.put(putUri, employeeResponse.id()).contentType(MediaType.APPLICATION_JSON)
                            .content(updateRequestAsJson))
                    .andExpect(MockMvcResultMatchers.status().isNoContent());

            Employee updateEmployee = employeeRepository.findById(employeeResponse.id()).orElseThrow();
            Assertions.assertThat(dateFormatter.format(updateEmployee.getBirthday()))
                    .isEqualTo(dateFormatter.format(updateEmployeeRequest.birthday()));
            Assertions.assertThat(updateEmployee.getFullName().getFirstName()).isEqualTo(updateEmployeeRequest.firstName());
            Assertions.assertThat(updateEmployee.getFullName().getLastName()).isEqualTo(updateEmployeeRequest.lastName());
            Assertions.assertThat(updateEmployee.getEmailAddress()).isEqualTo(updateEmployeeRequest.emailAddress());
            Assertions.assertThat(updateEmployee.getDepartment().getDepartmentName())
                    .isEqualTo(updateEmployeeRequest.departmentName());
        }

        @Test
        @DisplayName("PUT: 'http://.../employees/{id} returns BAD REQUEST on null birthday")
        void givenNullBirthDay_whenFullUpdateEmployee_thenStatus400() throws Exception {
            // Arrange
            DepartmentResponse departmentResponse = saveRandomDepartment();
            EmployeeRequest initialEmployeeRequest = employeeRequestTestFactory.builder().departmentName(departmentResponse.departmentName()).create();
            String requestAsJson = transformRequestToJSONByView(initialEmployeeRequest, DataView.POST.class);
            String createUri = "%s".formatted(EmployeeController.BASE_URI);
            MvcResult mvcResult = mockMvc.perform(post(createUri).contentType(MediaType.APPLICATION_JSON)
                    .content(requestAsJson)).andReturn();
            EmployeeResponse employeeResponse = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), EmployeeResponse.class);

            EmployeeRequest updateEmployeeRequest = employeeRequestTestFactory.builder()
                    .departmentName(departmentResponse.departmentName())
                    .birthday(null).create();
            String updateRequestAsJson = transformRequestToJSONByView(updateEmployeeRequest, DataView.PUT.class);
            String putUri = "%s/{id}".formatted(EmployeeController.BASE_URI);

            // Act / Assert
            mockMvc.perform(MockMvcRequestBuilders.put(putUri, employeeResponse.id()).contentType(MediaType.APPLICATION_JSON)
                            .content(updateRequestAsJson))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.notNullValue()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.url", Matchers.notNullValue()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.httpMethod", Matchers.notNullValue()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.httpStatus", Matchers.is(HttpStatus.BAD_REQUEST.name())))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.errorDateTime", Matchers.notNullValue()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.errorMessage", Matchers.notNullValue()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.errorMessage", Matchers
                            .containsString("birthday: must not be null")));
        }

        @Test
        @DisplayName("PUT: 'http://.../employees/{id} returns BAD REQUEST on invalid email")
        void givenInvalidEmail_whenFullUpdateEmployee_thenStatus400() throws Exception {
            // Arrange
            DepartmentResponse departmentResponse = saveRandomDepartment();
            EmployeeRequest initialEmployeeRequest = employeeRequestTestFactory.builder().departmentName(departmentResponse.departmentName()).create();
            String requestAsJson = transformRequestToJSONByView(initialEmployeeRequest, DataView.POST.class);
            String createUri = "%s".formatted(EmployeeController.BASE_URI);
            MvcResult mvcResult = mockMvc.perform(post(createUri).contentType(MediaType.APPLICATION_JSON)
                    .content(requestAsJson)).andReturn();
            EmployeeResponse employeeResponse = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), EmployeeResponse.class);

            EmployeeRequest updateEmployeeRequest = employeeRequestTestFactory.builder()
                    .departmentName(departmentResponse.departmentName())
                    .emailAddress(RandomStringUtils.randomAlphabetic(10)).create();
            String updateRequestAsJson = transformRequestToJSONByView(updateEmployeeRequest, DataView.PUT.class);
            String putUri = "%s/{id}".formatted(EmployeeController.BASE_URI);

            // Act / Assert
            mockMvc.perform(MockMvcRequestBuilders.put(putUri, employeeResponse.id()).contentType(MediaType.APPLICATION_JSON)
                            .content(updateRequestAsJson))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.notNullValue()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.url", Matchers.notNullValue()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.httpMethod", Matchers.notNullValue()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.httpStatus", Matchers.is(HttpStatus.BAD_REQUEST.name())))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.errorDateTime", Matchers.notNullValue()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.errorMessage", Matchers.notNullValue()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.errorMessage", Matchers
                            .containsString("must match \"" + EmployeeRequest.EMAIL_REGEX + "\"")));
        }


        @Test
        @DisplayName("PUT: 'http://.../employees/{id} returns BAD REQUEST on null email")
        void givenNullEmail_whenFullUpdateEmployee_thenStatus400() throws Exception {
            // Arrange
            DepartmentResponse departmentResponse = saveRandomDepartment();
            EmployeeRequest initialEmployeeRequest = employeeRequestTestFactory.builder().departmentName(departmentResponse.departmentName()).create();
            String requestAsJson = transformRequestToJSONByView(initialEmployeeRequest, DataView.POST.class);
            String createUri = "%s".formatted(EmployeeController.BASE_URI);
            MvcResult mvcResult = mockMvc.perform(post(createUri).contentType(MediaType.APPLICATION_JSON)
                    .content(requestAsJson)).andReturn();
            EmployeeResponse employeeResponse = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), EmployeeResponse.class);

            EmployeeRequest updateEmployeeRequest = employeeRequestTestFactory.builder()
                    .departmentName(departmentResponse.departmentName())
                    .emailAddress(null).create();
            String updateRequestAsJson = transformRequestToJSONByView(updateEmployeeRequest, DataView.PATCH.class);
            String putUri = "%s/{id}".formatted(EmployeeController.BASE_URI);

            // Act / Assert
            mockMvc.perform(MockMvcRequestBuilders.put(putUri, employeeResponse.id()).contentType(MediaType.APPLICATION_JSON)
                            .content(updateRequestAsJson))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.notNullValue()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.url", Matchers.notNullValue()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.httpMethod", Matchers.notNullValue()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.httpStatus", Matchers.is(HttpStatus.BAD_REQUEST.name())))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.errorDateTime", Matchers.notNullValue()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.errorMessage", Matchers.notNullValue()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.errorMessage", Matchers
                            .containsString("emailAddress: must not be blank")));
        }

        @Test
        @DisplayName("PUT: 'http://.../employees/{id} returns BAD REQUEST on null first name")
        void givenNullFirstName_whenFullUpdateEmployee_thenStatus400() throws Exception {
            // Arrange
            DepartmentResponse departmentResponse = saveRandomDepartment();
            EmployeeRequest initialEmployeeRequest = employeeRequestTestFactory.builder().departmentName(departmentResponse.departmentName()).create();
            String requestAsJson = transformRequestToJSONByView(initialEmployeeRequest, DataView.POST.class);
            String createUri = "%s".formatted(EmployeeController.BASE_URI);
            MvcResult mvcResult = mockMvc.perform(post(createUri).contentType(MediaType.APPLICATION_JSON)
                    .content(requestAsJson)).andReturn();
            EmployeeResponse employeeResponse = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), EmployeeResponse.class);

            EmployeeRequest updateEmployeeRequest = employeeRequestTestFactory.builder()
                    .departmentName(departmentResponse.departmentName())
                    .firstName(null).create();
            String updateRequestAsJson = transformRequestToJSONByView(updateEmployeeRequest, DataView.PATCH.class);
            String putUri = "%s/{id}".formatted(EmployeeController.BASE_URI);

            // Act / Assert
            mockMvc.perform(MockMvcRequestBuilders.put(putUri, employeeResponse.id()).contentType(MediaType.APPLICATION_JSON)
                            .content(updateRequestAsJson))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.notNullValue()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.url", Matchers.notNullValue()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.httpMethod", Matchers.notNullValue()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.httpStatus", Matchers.is(HttpStatus.BAD_REQUEST.name())))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.errorDateTime", Matchers.notNullValue()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.errorMessage", Matchers.notNullValue()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.errorMessage", Matchers
                            .containsString("firstName: must not be blank")));
        }

        @Test
        @DisplayName("PUT: 'http://.../employees/{id} returns BAD REQUEST on null last name")
        void givenNullLastName_whenFullUpdateEmployee_thenStatus400() throws Exception {
            // Arrange
            DepartmentResponse departmentResponse = saveRandomDepartment();
            EmployeeRequest initialEmployeeRequest = employeeRequestTestFactory.builder().departmentName(departmentResponse.departmentName()).create();
            String requestAsJson = transformRequestToJSONByView(initialEmployeeRequest, DataView.POST.class);
            String createUri = "%s".formatted(EmployeeController.BASE_URI);
            MvcResult mvcResult = mockMvc.perform(post(createUri).contentType(MediaType.APPLICATION_JSON)
                    .content(requestAsJson)).andReturn();
            EmployeeResponse employeeResponse = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), EmployeeResponse.class);

            EmployeeRequest updateEmployeeRequest = employeeRequestTestFactory.builder()
                    .departmentName(departmentResponse.departmentName())
                    .lastName(null).create();
            String updateRequestAsJson = transformRequestToJSONByView(updateEmployeeRequest, DataView.PATCH.class);
            String putUri = "%s/{id}".formatted(EmployeeController.BASE_URI);

            // Act / Assert
            mockMvc.perform(MockMvcRequestBuilders.put(putUri, employeeResponse.id()).contentType(MediaType.APPLICATION_JSON)
                            .content(updateRequestAsJson))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.notNullValue()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.url", Matchers.notNullValue()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.httpMethod", Matchers.notNullValue()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.httpStatus", Matchers.is(HttpStatus.BAD_REQUEST.name())))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.errorDateTime", Matchers.notNullValue()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.errorMessage", Matchers.notNullValue()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.errorMessage", Matchers
                            .containsString("lastName: must not be blank")));
        }

        @Test
        @DisplayName("PUT: 'http://.../employees/{id} returns BAD REQUEST on null department name")
        void givenNullDepartmentName_whenFullUpdateEmployee_thenStatus400() throws Exception {
            // Arrange
            DepartmentResponse departmentResponse = saveRandomDepartment();
            EmployeeRequest initialEmployeeRequest = employeeRequestTestFactory.builder().departmentName(departmentResponse.departmentName()).create();
            String requestAsJson = transformRequestToJSONByView(initialEmployeeRequest, DataView.POST.class);
            String createUri = "%s".formatted(EmployeeController.BASE_URI);
            MvcResult mvcResult = mockMvc.perform(post(createUri).contentType(MediaType.APPLICATION_JSON)
                    .content(requestAsJson)).andReturn();
            EmployeeResponse employeeResponse = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), EmployeeResponse.class);

            EmployeeRequest updateEmployeeRequest = employeeRequestTestFactory.builder()
                    .departmentName(null).create();
            String updateRequestAsJson = transformRequestToJSONByView(updateEmployeeRequest, DataView.PATCH.class);
            String putUri = "%s/{id}".formatted(EmployeeController.BASE_URI);

            // Act / Assert
            mockMvc.perform(MockMvcRequestBuilders.put(putUri, employeeResponse.id()).contentType(MediaType.APPLICATION_JSON)
                            .content(updateRequestAsJson))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.notNullValue()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.url", Matchers.notNullValue()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.httpMethod", Matchers.notNullValue()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.httpStatus", Matchers.is(HttpStatus.BAD_REQUEST.name())))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.errorDateTime", Matchers.notNullValue()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.errorMessage", Matchers.notNullValue()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.errorMessage", Matchers
                            .containsString("departmentName: must not be blank")));
        }
    }

    @Nested
    @DisplayName("when delete")
    class WhenDelete {

        @Test
        @DisplayName("DELETE: 'http://.../employees/{id}' returns NOT FOUND if the specified uuid doesn't exist")
        void givenUnknownId_whenDeleteEmployeeById_thenStatus404() throws Exception {
            // Arrange
            String unknownId = UUID.randomUUID().toString();
            String uri = "%s/{id}".formatted(EmployeeController.BASE_URI);

            // Act  / Assert
            mockMvc.perform(MockMvcRequestBuilders.delete(uri, unknownId).contentType(MediaType.APPLICATION_JSON))
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
                            .containsString("Could not find employee for Id [%s]!".formatted(unknownId))));
        }

        @Test
        @DisplayName("DELETE: 'http://.../employees/{id}' returns NO CONTENT if the specified uuid exists")
        void givenEmployee_whenDeleteEmployeeById_thenStatus204() throws Exception {
            // Arrange
            DepartmentResponse departmentResponse = saveRandomDepartment();
            EmployeeRequest initialEmployeeRequest = employeeRequestTestFactory.builder().departmentName(departmentResponse.departmentName()).create();
            String requestAsJson = transformRequestToJSONByView(initialEmployeeRequest, DataView.POST.class);
            String createUri = "%s".formatted(EmployeeController.BASE_URI);
            MvcResult mvcResult = mockMvc.perform(post(createUri).contentType(MediaType.APPLICATION_JSON)
                    .content(requestAsJson)).andReturn();
            EmployeeResponse employeeResponse = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), EmployeeResponse.class);
            String uri = "%s/{id}".formatted(EmployeeController.BASE_URI);

            // Acr / Assert
            mockMvc.perform(MockMvcRequestBuilders.delete(uri, employeeResponse.id()).contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isNoContent());

            Optional<Employee> optionalEmployee = employeeRepository.findById(employeeResponse.id());
            Assertions.assertThat(optionalEmployee).isEmpty();
        }

    }

}