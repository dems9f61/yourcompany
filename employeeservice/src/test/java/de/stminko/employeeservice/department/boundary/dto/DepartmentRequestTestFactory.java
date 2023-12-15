package de.stminko.employeeservice.department.boundary.dto;

import de.stminko.employeeservice.AbstractTestFactory;
import org.apache.commons.lang3.RandomStringUtils;

import org.springframework.stereotype.Component;

@Component
public class DepartmentRequestTestFactory
		extends AbstractTestFactory<DepartmentRequest, DepartmentRequestTestFactory.Builder> {

	@Override
	public Builder builder() {
		return new Builder();
	}

	public static final class Builder implements AbstractTestFactory.Builder<DepartmentRequest> {

		private String departmentName;

		private Builder() {
			this.departmentName = RandomStringUtils.randomAlphabetic(8);
		}

		public Builder departmentName(String departmentName) {
			this.departmentName = departmentName;
			return this;
		}

		public DepartmentRequest create() {
			return new DepartmentRequest(this.departmentName);
		}

	}

}
