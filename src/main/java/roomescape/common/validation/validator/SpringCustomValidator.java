package roomescape.common.validation.validator;

import java.util.List;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import roomescape.common.validation.exception.RequestValidationException;

public class SpringCustomValidator implements Validator {

    private final FieldBlankValidator blankValidator = new FieldBlankValidator();
    private final NotNullValidator notNullValidator = new NotNullValidator();

    @Override
    public boolean supports(Class<?> clazz) {
        return true;
    }

    @Override
    public void validate(Object target, Errors errors) {
        List<String> blankErrors = blankValidator.validate(target);
        if (!blankErrors.isEmpty()) {
            throw new RequestValidationException(blankErrors.getFirst());
        }

        List<String> notNullErrors = notNullValidator.validate(target);
        if (!notNullErrors.isEmpty()) {
            throw new RequestValidationException(notNullErrors.getFirst());
        }
    }

}
