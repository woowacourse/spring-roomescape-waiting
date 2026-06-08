package roomescape.presentation.error;

import java.time.LocalDateTime;
import java.util.List;

public record ErrorResponse(
        String code,
        String message,
        LocalDateTime timestamp,
        List<FieldErrorResponse> fieldErrors
) {

    public static ErrorResponse of(String code, String message) {
        return new ErrorResponse(code, message, LocalDateTime.now(), List.of());
    }

    public static ErrorResponse of(String code, String message, List<FieldErrorResponse> fieldErrors) {
        return new ErrorResponse(code, message, LocalDateTime.now(), fieldErrors);
    }

    public record FieldErrorResponse(
            String field,
            String message
    ) {
    }
}
