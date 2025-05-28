package roomescape.exception;

import java.time.LocalDateTime;

public record FieldErrorResponse(
        LocalDateTime timestamp,
        String code,
        String fieldName,
        String message
) {
}
