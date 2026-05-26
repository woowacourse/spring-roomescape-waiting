package roomescape.common.handler;

import org.springframework.validation.FieldError;

public record ErrorDetail(
    String field,
    String message
) {

    public static ErrorDetail from(FieldError fieldError) {
        return new ErrorDetail(fieldError.getField(), fieldError.getDefaultMessage());
    }
}
