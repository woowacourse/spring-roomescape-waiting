package roomescape.dto;

import java.time.LocalDateTime;
import org.springframework.http.HttpStatus;

public class ErrorResponse {

    private final String message;
    private final LocalDateTime timestamp;
    private final int status;
    private final String error;

    public ErrorResponse(String message, HttpStatus status) {
        this.message = message;
        this.timestamp = LocalDateTime.now();
        this.status = status.value();
        this.error = status.name();
    }

    public int getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }
}
