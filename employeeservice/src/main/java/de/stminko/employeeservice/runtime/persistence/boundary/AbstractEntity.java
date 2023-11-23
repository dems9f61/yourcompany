package de.stminko.employeeservice.runtime.persistence.boundary;

import java.io.Serializable;
import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonView;
import de.stminko.employeeservice.runtime.rest.bondary.DataView;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Setter;
import org.hibernate.envers.Audited;

import org.springframework.data.domain.Persistable;

/**
 * Abstract base class for entities providing common audit fields and lifecycle callbacks.
 * This class can be extended by entities to inherit common fields like 'createdAt',
 * 'modifiedAt', and 'version', along with JPA lifecycle hooks.
 *
 * @param <ID> the type of the identifier of the entity
 * @author Stéphan Minko
 */
@Audited
@MappedSuperclass
public abstract class AbstractEntity<ID extends Serializable> implements Persistable<ID> {

    /**
     * Timestamp of when the entity was created.
     */
    @JsonView(DataView.GET.class)
    @NotNull(groups = {DataView.GET.class})
    @Column(nullable = false)
    private ZonedDateTime createdAt;

    /**
     * Timestamp of when the entity was last modified.
     */
    @JsonView(DataView.GET.class)
    @NotNull(groups = {DataView.GET.class})
    @Column(nullable = false)
    private ZonedDateTime modifiedAt;

    /**
     * Version number for optimistic locking.
     */
    @Setter(value = AccessLevel.PRIVATE)
    @Column(nullable = false)
    @Version
    private Long version;

    /**
     * Determines if the entity is new or already persisted.
     *
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
        patchAuditData();
        onPrePersist();
    }

    /**
     * Lifecycle callback method invoked before an existing entity is updated.
     */
    @PreUpdate
    public final void preUpdate() {
        patchAuditData();
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
     * Provides a string representation of the entity, including its identifier, creation,
     * and modification timestamps.
     *
     * @return a string representation of the entity
     */
    @Override
    public String toString() {
        return "AbstractEntity{" + "id=" + getId() + ", created at=" + this.createdAt + ", modified At="
                + this.modifiedAt + '}';
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

    /**
     * Updates audit data fields (createdAt and modifiedAt).
     */
    private void patchAuditData() {
        this.modifiedAt = ZonedDateTime.now();
        if (this.createdAt == null) {
            this.createdAt = this.modifiedAt;
        }
    }

}
