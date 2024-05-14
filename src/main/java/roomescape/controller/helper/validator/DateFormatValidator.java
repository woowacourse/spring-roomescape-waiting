package roomescape.controller.helper.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class DateFormatValidator implements ConstraintValidator<DateFormatConstraint, String> {

    @Override
    public void initialize(DateFormatConstraint constraintAnnotation) {
    }

    @Override
    public boolean isValid(String contactField, ConstraintValidatorContext constraintValidatorContext) {
        try {
            LocalDate.parse(contactField);
        } catch (DateTimeParseException ex) {
            return false;
        }
        return true;
    }
}
