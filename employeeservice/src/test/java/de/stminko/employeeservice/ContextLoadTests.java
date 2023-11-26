package de.stminko.employeeservice;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Context loading integration test")
class ContextLoadTests extends AbstractIntegrationTestSuite {

	@DisplayName("The spring related application context should be loaded")
	@Test
	void givenApplicationContext_whenLoad_thenPass() {
	}

}
