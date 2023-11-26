package de.stminko.employeeservice.employee.entity;

import java.time.ZonedDateTime;

import de.stminko.employeeservice.AbstractTestFactory;
import org.apache.commons.lang3.RandomStringUtils;

import org.springframework.stereotype.Component;

@Component
public class EmployeeRequestTestFactory
		extends AbstractTestFactory<EmployeeRequest, EmployeeRequestTestFactory.Builder> {

	@Override
	public Builder builder() {
		return new Builder();
	}

	public static class Builder implements AbstractTestFactory.Builder<EmployeeRequest> {

		private String emailAddress;

		private String firstName;

		private String lastName;

		private ZonedDateTime birthday;

		private String departmentName;

		Builder() {
			this.emailAddress = generateRandomEmail();
			this.firstName = RandomStringUtils.randomAlphabetic(10);
			this.lastName = RandomStringUtils.randomAlphabetic(13);
			this.birthday = createRandomBirthday();
			this.departmentName = RandomStringUtils.randomAlphabetic(24);
		}

		public Builder emailAddress(String emailAddress) {
			this.emailAddress = emailAddress;
			return this;
		}

		public Builder firstName(String firstName) {
			this.firstName = firstName;
			return this;
		}

		public Builder lastName(String lastName) {
			this.lastName = lastName;
			return this;
		}

		public Builder birthday(ZonedDateTime birthday) {
			this.birthday = birthday;
			return this;
		}

		public Builder departmentName(String departmentName) {
			this.departmentName = departmentName;
			return this;
		}

		public EmployeeRequest create() {
			return new EmployeeRequest(this.emailAddress, this.firstName, this.lastName, this.birthday,
					this.departmentName);
		}

	}

}
