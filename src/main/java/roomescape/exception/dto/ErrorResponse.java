package roomescape.exception.dto;

import org.springframework.http.HttpStatus;
import roomescape.exception.ErrorCode;

public record ErrorResponse(
        HttpStatus status,
        String message
) {

    public ErrorResponse(ErrorCode code) {
        this(code.getStatus(), code.getMessage());
    }

    public ErrorResponse(ErrorCode code, String message) {
        this(code.getStatus(), message);
    }
}
