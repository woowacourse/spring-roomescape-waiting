package roomescape.support.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import roomescape.support.exception.errors.Errors;

public record ErrorResponse(
    String code,
    String message
) {

    public static ResponseEntity<ErrorResponse> of(HttpStatus httpStatus, RoomescapeException exception) {
        return ResponseEntity.status(httpStatus)
            .body(new ErrorResponse(exception.getErrors().getCode(), exception.getMessage()));
    }

    public static ResponseEntity<ErrorResponse> of(HttpStatus httpStatus, Errors errors) {
        return ResponseEntity.status(httpStatus)
            .body(new ErrorResponse(errors.getCode(), errors.getMessage()));
    }

    public static ResponseEntity<ErrorResponse> of(HttpStatus httpStatus, Errors errors, String message) {
        return ResponseEntity.status(httpStatus)
            .body(new ErrorResponse(errors.getCode(), message));
    }
}
