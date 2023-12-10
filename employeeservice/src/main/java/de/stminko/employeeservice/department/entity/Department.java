package de.stminko.employeeservice.department.entity;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonBackReference;
import de.stminko.employeeservice.employee.entity.Employee;
import de.stminko.employeeservice.runtime.persistence.boundary.AbstractEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.envers.AuditOverride;
import org.hibernate.envers.AuditOverrides;
import org.hibernate.envers.Audited;

/**
 * Entity class representing a department.
 * <p>
 * This class defines the department entity with its attributes and relationships. It
 * includes fields like the department's identifier and name, and a set of employees
 * associated with the department. It extends {@link AbstractEntity} to inherit common
 * audit fields and lifecycle callbacks.
 * </p>
 *
 * <p>
 * The class uses annotations to configure ORM (Object-Relational Mapping) via JPA
 * (Jakarta Persistence API), and it's audited using Hibernate Envers for maintaining
 * historical data.
 * </p>
 *
 * @author Stéphan Minko
 * @see AbstractEntity
 * @see Employee
 */
@Audited
@AuditOverrides({ @AuditOverride(forClass = AbstractEntity.class, isAudited = false, name = "createdAt"),
		@AuditOverride(forClass = AbstractEntity.class, isAudited = false, name = "createdBy") })
@Getter
@Setter
@ToString(callSuper = true)
@Entity
@Table(name = "DEPARTMENT", schema = "data")
@SequenceGenerator(name = "department_sequence", allocationSize = 1, sequenceName = "department_sequence",
		schema = "data")
public class Department extends AbstractEntity<Long> {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "department_sequence")
	private Long id;

	@Column(name = "DEPARTMENT_NAME", length = 50, nullable = false, unique = true)
	private String departmentName;

	@JsonBackReference
	@ToString.Exclude
	@OneToMany(mappedBy = "department", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<Employee> employees = new HashSet<>();

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if ((o == null) || (getClass() != o.getClass())) {
			return false;
		}
		Department that = (Department) o;
		return Objects.equals(getId(), that.getId()) && Objects.equals(getDepartmentName(), that.getDepartmentName());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getId(), getDepartmentName());
	}

}
