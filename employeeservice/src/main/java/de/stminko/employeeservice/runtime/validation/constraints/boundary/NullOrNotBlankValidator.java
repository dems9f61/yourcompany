package de.stminko.employeeservice.runtime.validation.constraints.boundary;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorContextImpl;

import org.springframework.util.ObjectUtils;

/**
 * Validator for the {@link NullOrNotBlank} constraint annotation.
 *
 * @author Stéphan Minko
 */
public class NullOrNotBlankValidator implements ConstraintValidator<NullOrNotBlank, String> {

	@Override
	public void initialize(NullOrNotBlank constraintAnnotation) {
		ConstraintValidator.super.initialize(constraintAnnotation);
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		if (value == null) {
			return true;
		}
		// Add the Value to ConstraintValidatorContext for I18N Message interpolation
		((ConstraintValidatorContextImpl) context).addMessageParameter("value", value);
		return !ObjectUtils.isEmpty(value.trim());
	}

}
