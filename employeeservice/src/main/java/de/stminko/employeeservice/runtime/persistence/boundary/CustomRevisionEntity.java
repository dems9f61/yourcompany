package de.stminko.employeeservice.runtime.persistence.boundary;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.envers.RevisionEntity;
import org.hibernate.envers.RevisionNumber;
import org.hibernate.envers.RevisionTimestamp;

/**
 * Entity class for audit trail revisions in the database.
 *
 * <p>This class serves as a custom revision entity for auditing entities in a JPA-based application.
 * It is equipped with standard getter and setter methods as well as a no-argument constructor,
 * facilitated by the use of Lombok annotations.</p>
 *
 * <p>The class is marked as a JPA entity and is stored in the {@code audit_trail} table within the {@code history} schema.
 * It utilizes a generated identifier derived from a sequence {@code audit_trail_sequence} in the same schema.</p>
 *
 * <p>The entity contains fields for the revision ID and the timestamp of the revision.
 * These fields are annotated with {@code @RevisionNumber} and {@code @RevisionTimestamp},
 * marking them as special fields for Hibernate Envers auditing.</p>
 *
 * @author Stéphan Minko
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "audit_trail", schema = "history")
@RevisionEntity
@GenericGenerator(name = "audit_trail_sequence", type = org.hibernate.id.enhanced.SequenceStyleGenerator.class,
        parameters = {@Parameter(name = "sequence_name", value = "history.audit_trail_sequence"),
                @Parameter(name = "initial_value", value = "1"), @Parameter(name = "increment_size", value = "1"),
                @Parameter(name = "schema", value = "history")})
public class CustomRevisionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "audit_trail_sequence")
    @RevisionNumber
    private Long id;

    @RevisionTimestamp
    private Long timestamp;

}
