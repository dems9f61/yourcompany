package de.stminko.eventservice;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.stminko.eventservice.employee.boundary.EmployeeMessageReceiver;
import de.stminko.eventservice.employee.entity.Employee;
import de.stminko.eventservice.employee.entity.EmployeeEventTestFactory;
import de.stminko.eventservice.employee.entity.EmployeeMessage;
import de.stminko.eventservice.employee.entity.EmployeeMessageTestFactory;
import de.stminko.eventservice.employee.entity.EmployeeTestFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.Assert;
import org.springframework.util.StopWatch;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {EventServiceApplication.class})
@Slf4j
@ActiveProfiles("local")
public abstract class AbstractIntegrationTestSuite {

    @Autowired
    protected EmployeeTestFactory employeeTestFactory;

    @Autowired
    protected EmployeeEventTestFactory employeeEventTestFactory;

    @Autowired
    protected EmployeeMessageTestFactory employeeMessageTestFactory;

    @Autowired
    protected DatabaseCleaner databaseCleaner;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    private EmployeeMessageReceiver employeeMessageReceiver;


    private final Map<String, StopWatch> stopWatches = new ConcurrentHashMap<>();

    public void receiveRandomMessageFor(String id) {
        receiveRandomMessageFor(id, 0);
    }

    public void receiveRandomMessageFor(int count) {
        receiveRandomMessageFor(null, 0);
    }

    public void receiveRandomMessageFor(String id, int count) {
        List<Employee> employees = employeeTestFactory.createManyDefault(count <= 0 ? RandomUtils.nextInt(30, 100) : count);
        employees.forEach(employee -> {
            employee.setId(id);
            EmployeeMessage employeeMessage = employeeMessageTestFactory.builder().employee(employee).create();
            employeeMessageReceiver.receiveEmployeeMessage(employeeMessage);
        });
    }

    @BeforeEach
    public final void onBeforeEach(TestInfo testInfo) {
        log.info("BEFORE TEST <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
        String taskName = "TEST_SCOPE";
        StopWatch stopWatch = getStopWatch(taskName);
        log.info("Starting Test: [{}]",
                StringUtils.isBlank(testInfo.getDisplayName()) ? testInfo.getTestMethod() : testInfo.getDisplayName());
        stopWatch.start(taskName);
    }

    private StopWatch getStopWatch(String name) {
        Assert.notNull(name, "stop watch name cannot be null");
        StopWatch stopWatch = this.stopWatches.getOrDefault(name, new StopWatch(name));
        this.stopWatches.put(name, stopWatch);
        return stopWatch;
    }

    @AfterEach
    public final void onAfterEach() {
        StopWatch stopWatch = getStopWatch("TEST_SCOPE");
        stopWatch.stop();
        log.info(stopWatch.shortSummary());
        log.info("AFTER TEST <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
        databaseCleaner.cleanDatabases();
    }


}
