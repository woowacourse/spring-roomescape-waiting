package roomescape.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;

public record ErrorResponse(
        LocalDateTime timestamp,
        HttpStatus status,
        String exceptionType,
        String message
) {
    public static ErrorResponse plainResponse(HttpStatus httpStatus, ErrorCode errorCode) {
        return new ErrorResponse(
                LocalDateTime.now(),
                httpStatus,
                errorCode.name(),
                errorCode.message()
        );
    }

    public static ErrorResponse securedResponse(HttpStatus httpStatus, String message) {
        return new ErrorResponse(
                LocalDateTime.now(),
                httpStatus,
                "",
                message
        );
    }

    public static ErrorResponse securedResponse() {
        return new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR,
                "",
                "서버에서 심각한 오류가 발생하였습니다."
        );
    }

    public ResponseEntity<ErrorResponse> toResponseEntity() {
        return ResponseEntity.status(status).body(this);
    }
}
