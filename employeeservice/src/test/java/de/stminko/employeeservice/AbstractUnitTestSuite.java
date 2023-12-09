package de.stminko.employeeservice;

import de.stminko.employeeservice.department.entity.DepartmentRequestTestFactory;
import de.stminko.employeeservice.department.entity.DepartmentTestFactory;
import de.stminko.employeeservice.employee.entity.EmployeeRequestTestFactory;
import de.stminko.employeeservice.employee.entity.EmployeeTestFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public abstract class AbstractUnitTestSuite {

	protected DepartmentTestFactory departmentTestFactory;

	protected DepartmentRequestTestFactory departmentRequestTestFactory;

	protected EmployeeTestFactory employeeTestFactory;

	protected EmployeeRequestTestFactory employeeRequestTestFactory;

	@BeforeEach
	public void initFactories() {
		this.departmentTestFactory = new DepartmentTestFactory();
		this.departmentRequestTestFactory = new DepartmentRequestTestFactory();
		this.employeeTestFactory = new EmployeeTestFactory();
		this.employeeRequestTestFactory = new EmployeeRequestTestFactory();
	}

}
