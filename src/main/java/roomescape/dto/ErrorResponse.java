package roomescape.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

public class ErrorResponse {

    private String message;
    private final LocalDateTime timestamp;
    private final int status;
    private final String error;

    public ErrorResponse(String message, HttpStatus status) {
        this.message = message;
        this.timestamp = LocalDateTime.now();
        this.status = status.value();
        this.error = status.name();
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }
}
