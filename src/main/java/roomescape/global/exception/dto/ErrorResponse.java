package roomescape.global.exception.dto;

import java.time.LocalDateTime;

public record ErrorResponse(
        String message,
        LocalDateTime timestamp
) {

    public ErrorResponse(final String message) {
        this(message, LocalDateTime.now());
    }
}
