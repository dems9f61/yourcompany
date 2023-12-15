package de.stminko.employeeservice;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import de.stminko.employeeservice.department.boundary.dto.DepartmentRequestTestFactory;
import de.stminko.employeeservice.department.entity.DepartmentTestFactory;
import de.stminko.employeeservice.employee.boundary.dto.EmployeeRequestTestFactory;
import de.stminko.employeeservice.employee.control.EmployeeEventPublisher;
import de.stminko.employeeservice.employee.entity.EmployeeTestFactory;
import lombok.extern.slf4j.Slf4j;
import nz.lae.stacksrc.junit5.ErrorDecorator;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.Assert;
import org.springframework.util.StopWatch;

@ExtendWith({ SpringExtension.class, ErrorDecorator.class })
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		classes = { EmployeeServiceApplication.class })
@ActiveProfiles("local")
@Slf4j
public abstract class AbstractIntegrationTestSuite {

	@Autowired
	protected DepartmentRequestTestFactory departmentRequestTestFactory;

	@Autowired
	protected DepartmentTestFactory departmentTestFactory;

	@Autowired
	protected EmployeeRequestTestFactory employeeRequestTestFactory;

	@Autowired
	protected EmployeeTestFactory employeeTestFactory;

	@Autowired
	protected DatabaseCleaner databaseCleaner;

	@SpyBean
	protected EmployeeEventPublisher employeeEventPublisher;

	@Autowired
	protected ObjectMapper objectMapper;

	private final Map<String, StopWatch> stopWatches = new ConcurrentHashMap<>();

	@BeforeEach
	void setUp() {
		Mockito.doNothing().when(this.employeeEventPublisher).employeeCreated(ArgumentMatchers.any());
		Mockito.doNothing().when(this.employeeEventPublisher).employeeDeleted(ArgumentMatchers.any());
		Mockito.doNothing().when(this.employeeEventPublisher).employeeUpdated(ArgumentMatchers.any());
	}

	@BeforeEach
	public final void onBeforeEach(TestInfo testInfo) {
		LocaleContextHolder.setLocale(Locale.ENGLISH);
		log.info("BEFORE TEST <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
		String taskName = "TEST_SCOPE";
		StopWatch stopWatch = getStopWatch(taskName);
		log.info("Starting Test: [{}]",
				StringUtils.isBlank(testInfo.getDisplayName()) ? testInfo.getTestMethod() : testInfo.getDisplayName());
		stopWatch.start(taskName);
	}

	@AfterEach
	public final void onAfterEach() {
		LocaleContextHolder.resetLocaleContext();
		StopWatch stopWatch = getStopWatch("TEST_SCOPE");
		stopWatch.stop();
		log.info(stopWatch.shortSummary());
		log.info("AFTER TEST <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
		this.databaseCleaner.cleanDatabases();
	}

	protected String transformRequestToJSON(Object object) throws Exception {
		return transformRequestToJSONByView(object, null);
	}

	protected String transformRequestToJSONByView(Object object, Class<?> serializationView) throws Exception {
		ObjectWriter objectWriter = (serializationView != null)
				? this.objectMapper.writerWithView(serializationView).withDefaultPrettyPrinter()
				: this.objectMapper.writer().withDefaultPrettyPrinter();
		String result = objectWriter.writeValueAsString(object);
		this.objectMapper.writer().withDefaultPrettyPrinter();
		return result;
	}

	private StopWatch getStopWatch(String name) {
		Assert.notNull(name, "stop watch name cannot be null");
		StopWatch stopWatch = this.stopWatches.getOrDefault(name, new StopWatch(name));
		this.stopWatches.put(name, stopWatch);
		return stopWatch;
	}

}
