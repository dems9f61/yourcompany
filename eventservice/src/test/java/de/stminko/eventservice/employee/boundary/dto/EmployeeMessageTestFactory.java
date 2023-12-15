package de.stminko.eventservice.employee.boundary.dto;

import java.util.Random;

import org.springframework.stereotype.Component;

@Component
public class EmployeeMessageTestFactory {

	public EmployeeMessage createDefault() {
		return builder().create();
	}

	public Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private Employee employee;

		private EventType eventType;

		Builder() {
			EmployeeTestFactory employeeTestFactory = new EmployeeTestFactory();
			this.employee = employeeTestFactory.createDefault();
			EventType[] values = EventType.values();
			Random random = new Random();
			this.eventType = values[random.nextInt(values.length)];
		}

		public Builder employee(Employee employee) {
			this.employee = employee;
			return this;
		}

		public Builder eventType(EventType eventType) {
			this.eventType = eventType;
			return this;
		}

		public EmployeeMessage create() {
			return new EmployeeMessage(this.eventType, this.employee);
		}

	}

}
