package de.stminko.employeeservice.runtime.validation.constraints.boundary;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * Validate that a given {@link String} field is either <code>null</code> or contains a
 * non-empty trimmed String value.
 *
 * @author Stéphan Minko
 */
@Target({ ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NullOrNotBlankValidator.class)
@Documented
public @interface NullOrNotBlank {

	String message() default "{errors.nullOrNotBlank}";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

}
