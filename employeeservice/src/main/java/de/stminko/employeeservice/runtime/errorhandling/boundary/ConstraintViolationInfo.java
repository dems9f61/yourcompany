package de.stminko.employeeservice.runtime.errorhandling.boundary;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents detailed information about a constraint violation that occurs during validation.
 * This class is typically used to encapsulate validation error details, making it easier to
 * convey specific information about what validation constraint was violated.
 * <p>
 * Properties:
 * - isGlobalError: A boolean flag indicating whether the violation is a global error
 * (not related to a specific property) or a field-specific error.
 * - invalidValue: The value that was deemed invalid during validation. This provides context
 * as to what input caused the violation.
 * - propertyPath: The path to the property that has the constraint violation. It helps in pinpointing
 * exactly which part of the data structure caused the violation.
 * - message: A human-readable message describing the nature of the violation. This is useful for
 * informing users or developers about what went wrong and potentially how to fix it.
 * <p>
 * Usage:
 * This class is useful in exception handling mechanisms, particularly when dealing with user inputs
 * in web applications or APIs. It allows for the structured representation of validation errors,
 * which can then be serialized into a response to inform the client about specific issues with their request.
 *
 * @author Stéphan Minko
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConstraintViolationInfo {

    private boolean isGlobalError;

    private String invalidValue;

    private String propertyPath;

    private String message;

}
