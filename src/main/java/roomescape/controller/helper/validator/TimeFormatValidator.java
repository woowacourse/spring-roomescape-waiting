package roomescape.controller.helper.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.LocalTime;

public class TimeFormatValidator implements ConstraintValidator<TimeFormatConstraint, String> {

    @Override
    public void initialize(TimeFormatConstraint constraintAnnotation) {
    }

    @Override
    public boolean isValid(String contactField, ConstraintValidatorContext constraintValidatorContext) {
        try {
            LocalTime.parse(contactField);
        } catch (Exception ex) {
            return false;
        }
        return true;
    }
}
