package de.stminko.employeeservice.employee.entity;

import java.time.ZonedDateTime;

import de.stminko.employeeservice.AbstractTestFactory;
import de.stminko.employeeservice.department.entity.Department;
import de.stminko.employeeservice.department.entity.DepartmentTestFactory;
import org.apache.commons.lang3.RandomStringUtils;

import org.springframework.stereotype.Component;

@Component
public class EmployeeTestFactory extends AbstractTestFactory<Employee, EmployeeTestFactory.Builder> {

	public Builder builder() {
		return new Builder();
	}

	public static class Builder implements AbstractTestFactory.Builder<Employee> {

		private String emailAddress;

		private Employee.FullName fullName;

		private ZonedDateTime birthday;

		private Department department;

		Builder() {
			this.emailAddress = generateRandomEmail();
			DepartmentTestFactory departmentTestFactory = new DepartmentTestFactory();
			this.department = departmentTestFactory.createDefault();
			this.birthday = createRandomBirthday();
			this.fullName = new Employee.FullName();
			this.fullName.setLastName(RandomStringUtils.randomAlphabetic(12));
			this.fullName.setFirstName(RandomStringUtils.randomAlphabetic(12));
		}

		public Builder emailAddress(String emailAddress) {
			this.emailAddress = emailAddress;
			return this;
		}

		public Builder fullName(Employee.FullName fullName) {
			this.fullName = fullName;
			return this;
		}

		public Builder birthday(ZonedDateTime birthday) {
			this.birthday = birthday;
			return this;
		}

		public Builder department(Department department) {
			this.department = department;
			return this;
		}

		public Employee create() {
			Employee employee = new Employee();
			employee.setBirthday(this.birthday);
			employee.setDepartment(this.department);
			employee.setEmailAddress(this.emailAddress);
			employee.setFullName(this.fullName);

			return employee;
		}

	}

}
