package roomescape.core.dto.exception;

import java.util.List;
import org.springframework.validation.BindingResult;

public class ExceptionDetails {
    private final String message;
    private final String rejectedField;
    private final String rejectedValue;

    private ExceptionDetails(final String message, final String rejectedField, final String rejectedValue) {
        this.message = message;
        this.rejectedField = rejectedField;
        this.rejectedValue = rejectedValue;
    }

    public static List<ExceptionDetails> create(final BindingResult bindingResult) {
        return bindingResult.getFieldErrors().stream()
                .map(fieldError -> new ExceptionDetails(
                        fieldError.getDefaultMessage(),
                        fieldError.getField(),
                        String.valueOf(fieldError.getRejectedValue()))
                ).toList();
    }

    public String getMessage() {
        return message;
    }

    public String getRejectedField() {
        return rejectedField;
    }

    public String getRejectedValue() {
        return rejectedValue;
    }
}
