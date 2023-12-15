package de.stminko.eventservice.employee.boundary.dto;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;

import org.springframework.stereotype.Component;

@Component
public class DepartmentTestFactory {

	public Builder builder() {
		return new Builder();
	}

	public Department createDefault() {
		return builder().create();
	}

	public static class Builder {

		private long id;

		private String departmentName;

		Builder() {
			this.id = RandomUtils.nextInt(10, 10_000);
			this.departmentName = RandomStringUtils.randomAlphabetic(24);
		}

		public Builder id(long id) {
			this.id = id;
			return this;
		}

		public Builder departmentName(String departmentName) {
			this.departmentName = departmentName;
			return this;
		}

		public Department create() {
			Department department = new Department();
			department.setId(this.id);
			department.setDepartmentName(this.departmentName);
			return department;
		}

	}

}
