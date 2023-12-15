package de.stminko.eventservice.employee.boundary.dto;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;

import org.springframework.stereotype.Component;

@Component
public class EmployeeTestFactory {

	public Employee createDefault() {
		return builder().create();
	}

	public List<Employee> createManyDefault(int count) {
		return manyBuilders(count).map(Builder::create).collect(Collectors.toList());
	}

	private Stream<Builder> manyBuilders(int count) {
		return IntStream.range(0, count).mapToObj((int i) -> builder());
	}

	public Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private String uuid;

		private String emailAddress;

		private Employee.FullName fullName;

		private ZonedDateTime birthday;

		private Department department;

		Builder() {
			this.uuid = UUID.randomUUID().toString();
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

		private LocalDate generateRandomDate() {
			return createRandomDate(2000);
		}

		private String generateRandomEmail() {
			return RandomStringUtils.randomAlphanumeric(RandomUtils.nextInt(10, 24)) + "@"
					+ (RandomStringUtils.randomAlphanumeric(10) + ".com");
		}

		public Employee create() {
			Employee employee = new Employee();
			employee.setBirthday(this.birthday);
			employee.setId(this.uuid);
			employee.setDepartment(this.department);
			employee.setEmailAddress(this.emailAddress);
			employee.setFullName(this.fullName);
			return employee;
		}

		private ZonedDateTime createRandomBirthday() {
			return createRandomDate(2010).atStartOfDay(ZoneOffset.UTC);
		}

		private LocalDate createRandomDate(int endYear) {
			int day = createRandomIntBetween(1, 28);
			int month = createRandomIntBetween(1, 12);
			int year = createRandomIntBetween(1900, endYear);
			return LocalDate.of(year, month, day);
		}

		private int createRandomIntBetween(int start, int end) {
			return start + (int) Math.round(Math.random() * (end - start));
		}

	}

}
