package de.stminko.employeeservice.runtime.persistence.boundary;

import java.io.Serializable;
import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonView;
import de.stminko.employeeservice.runtime.rest.bondary.DataView;
import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.domain.Persistable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * Abstract base class for entities providing common audit fields and lifecycle callbacks.
 * This class can be extended by entities to inherit common fields like 'createdAt',
 * 'modifiedAt', and 'version', along with JPA lifecycle hooks.
 *
 * @param <ID> the type of the identifier of the entity
 * @author Stéphan Minko
 */
@Audited
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class AbstractEntity<ID extends Serializable> implements Persistable<ID> {

	/**
	 * Timestamp of when the entity was created.
	 */
	@JsonView(DataView.GET.class)
	@NotNull
	@CreatedDate
	@Column(nullable = false, updatable = false)
	private ZonedDateTime createdAt;

	/**
	 * The createdBy variable represents the identifier of the user who created the
	 * entity.
	 */
	@JsonView({ DataView.GET.class })
	@NotNull
	@CreatedBy
	@Column(updatable = false)
	private String createdBy;

	/**
	 * Timestamp of when the entity was last modified.
	 */
	@JsonView(DataView.GET.class)
	@NotNull
	@Column(nullable = false)
	@LastModifiedDate
	private ZonedDateTime lastModifiedAt;

	/**
	 * The lastModifiedBy variable represents the user who last modified the data.
	 */
	@JsonView({ DataView.GET.class })
	@NotNull
	@LastModifiedBy
	private String lastModifiedBy;

	/**
	 * Version number for optimistic locking.
	 */
	@Setter(AccessLevel.PRIVATE)
	@Column(nullable = false)
	@Version
	private Long version;

	/**
	 * Determines if the entity is new or already persisted.
	 * @return true if the entity is new, false otherwise
	 */
	@Override
	public final boolean isNew() {
		return getId() == null;
	}

	/**
	 * Lifecycle callback method invoked before a new entity is persisted.
	 */
	@PrePersist
	public final void prePersist() {
		onPrePersist();
	}

	/**
	 * Lifecycle callback method invoked before an existing entity is updated.
	 */
	@PreUpdate
	public final void preUpdate() {
		onPreUpdate();
	}

	/**
	 * Lifecycle callback method invoked after an entity is loaded from the database.
	 */
	@PostLoad
	public final void postLoad() {
		onPostLoad();
	}

	/**
	 * Returns a string representation of the object.
	 * @return a string representation of the object.
	 */
	@Override
	public String toString() {
		return "AbstractEntity{" + "createdAt=" + this.createdAt + ", createdBy='" + this.createdBy + '\''
				+ ", lastModifiedAt=" + this.lastModifiedAt + ", lastModifiedBy='" + this.lastModifiedBy + '\''
				+ ", version=" + this.version + '}';
	}

	/**
	 * Hook method to perform action on pre-update event.
	 */
	protected void onPreUpdate() {
	}

	/**
	 * Hook method to perform action on pre-persist event.
	 */
	protected void onPrePersist() {
	}

	/**
	 * Hook method to perform action on post-load event.
	 */
	protected void onPostLoad() {
	}

}
