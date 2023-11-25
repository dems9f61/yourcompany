package de.stminko.employeeservice.employee.entity;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import de.stminko.employeeservice.department.entity.Department;
import de.stminko.employeeservice.runtime.persistence.boundary.AbstractEntity;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.envers.AuditOverride;
import org.hibernate.envers.Audited;

/**
 * Entity representing an employee in the Employee Service application.
 * <p>
 * This class serves as the domain model for an employee, defining various attributes such
 * as email address, full name, birthday, and department association. It extends
 * {@link AbstractEntity} to leverage common entity functionalities like ID management.
 * The class is annotated with JPA annotations to map it to the 'EMPLOYEE' table in the
 * 'data' schema.
 * </p>
 *
 * @see AbstractEntity for base entity functionalities
 * @see Department for the associated department entity
 * @author Stéphan Minko
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Audited
@AuditOverride(forClass = AbstractEntity.class)
@Getter
@Setter
@ToString(callSuper = true)
@Entity
@Table(name = "EMPLOYEE", schema = "data")
public class Employee extends AbstractEntity<String> {

	@Id
	@Column(length = 36)
	private String id;

	@Column(name = "EMAIL_ADDRESS", unique = true)
	private String emailAddress;

	@Embedded
	@AttributeOverrides(value = { @AttributeOverride(name = "firstName", column = @Column(name = "FIRST_NAME")),
			@AttributeOverride(name = "lastName", column = @Column(name = "LAST_NAME")) })
	private FullName fullName = new FullName();

	private ZonedDateTime birthday;

	@JsonManagedReference
	@ManyToOne(optional = false, cascade = CascadeType.REFRESH)
	@JoinColumn(name = "DEPARTMENT_ID", nullable = false)
	private Department department;

	@Override
	protected void onPrePersist() {
		if (isNew()) {
			setId(UUID.randomUUID().toString());
		}
	}

	@Setter
	@Getter
	@Embeddable
	@ToString
	@EqualsAndHashCode
	public static class FullName {

		private String firstName;

		private String lastName;

	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		Employee employee = (Employee) o;
		return Objects.equals(getId(), employee.getId())
				&& Objects.equals(getEmailAddress(), employee.getEmailAddress())
				&& Objects.equals(getFullName(), employee.getFullName())
				&& Objects.equals(getBirthday(), employee.getBirthday())
				&& Objects.equals(getDepartment(), employee.getDepartment());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getId(), getEmailAddress(), getFullName(), getBirthday(), getDepartment());
	}

}
