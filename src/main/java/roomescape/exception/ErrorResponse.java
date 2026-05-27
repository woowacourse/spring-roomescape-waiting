package roomescape.exception;

import java.time.LocalDateTime;

public record ErrorResponse(
        String message,
        LocalDateTime timeStamp
) {
    public static ErrorResponse of(String message) {
        return new ErrorResponse(
                message,
                LocalDateTime.now()
        );
    }
}
