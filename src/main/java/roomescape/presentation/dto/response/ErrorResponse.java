package roomescape.presentation.dto.response;

import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String message
) {

    public static ErrorResponse of(HttpStatus status, String message) {
        LocalDateTime timestamp = LocalDateTime.now();
        return new ErrorResponse(timestamp, status.value(), message);
    }
}
