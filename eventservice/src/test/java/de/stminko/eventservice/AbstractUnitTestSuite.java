package de.stminko.eventservice;

import de.stminko.eventservice.employee.entity.EmployeeEventTestFactory;
import de.stminko.eventservice.employee.entity.EmployeeMessageTestFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public abstract class AbstractUnitTestSuite {

	protected EmployeeEventTestFactory employeeEventTestFactory;

	protected EmployeeMessageTestFactory employeeMessageTestFactory;

	@BeforeEach
	public void initFactories() {
		this.employeeEventTestFactory = new EmployeeEventTestFactory();
		this.employeeMessageTestFactory = new EmployeeMessageTestFactory();
	}

}
