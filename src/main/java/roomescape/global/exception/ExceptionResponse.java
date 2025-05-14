package roomescape.global.exception;

import java.time.LocalDateTime;

public record ExceptionResponse(
        int status,
        String message,
        LocalDateTime timestamp
) {

    public ExceptionResponse(int status, String message, LocalDateTime timestamp) {
        this.status = status;
        this.message = "[ERROR]" + message;
        this.timestamp = timestamp;
    }
}
