package roomescape.presentation.error;

import java.time.LocalDateTime;

public record ErrorResponse(
        LocalDateTime timestamp,
        String message
) {

    public static ErrorResponse of (String message) {
        return new ErrorResponse(LocalDateTime.now(), message);
    }
}
