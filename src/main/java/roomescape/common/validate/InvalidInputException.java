package roomescape.common.validate;

import org.springframework.http.HttpStatus;
import roomescape.common.exception.base.BusinessException;
import roomescape.common.validate.Validator.ValidationType;

public class InvalidInputException extends BusinessException {

    public InvalidInputException(final ValidationType type,
                                 final String className,
                                 final String fieldName,
                                 final String fieldDescription) {
        super(
                buildLogMessage(type, className, fieldName),
                buildUserMessage(fieldDescription)
        );
    }

    private static String buildLogMessage(final ValidationType type,
                                          final String className,
                                          final String fieldName) {
        return "Validation failed [" + type.getDescription() + "]: " + className + "." + fieldName;
    }

    private static String buildUserMessage(final String fieldDescription) {
        return fieldDescription + "을(를) 올바르게 입력해주세요.";
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.BAD_REQUEST;
    }
}
