package roomescape.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.time.format.DateTimeParseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DateTimeParseException.class)
    public ResponseEntity<ErrorResponse> handleDateTimeParseException(final HttpServletRequest request) {
        ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.INVALID_DATETIME_FORMAT, request);
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(final CustomException e,
                                                               final HttpServletRequest request) {
        ErrorCode errorCode = e.getErrorCode();
        ErrorResponse errorResponse = ErrorResponse.of(errorCode, request);
        return ResponseEntity.status(errorCode.getStatus()).body(errorResponse);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleAllException(final HttpServletRequest request, RuntimeException e) {
        log.error(e.getMessage(), e);
        ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR, request);
        return ResponseEntity.internalServerError().body(errorResponse);
    }
}
