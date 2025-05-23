package roomescape.handler;

import java.time.LocalDateTime;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import roomescape.exception.CustomException;
import roomescape.exception.ErrorCode;
import roomescape.exception.ErrorResponse;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = CustomException.class)
    public ResponseEntity<ErrorResponse> handleInvalidInput(CustomException e) {
        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity
            .status(errorCode.getHttpStatus())
            .body(new ErrorResponse(LocalDateTime.now(), errorCode.name(), e.getMessage()));
    }

    @ExceptionHandler(value = DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDeleteConflict(DataIntegrityViolationException e) {
        ErrorCode errorCode = ErrorCode.DELETE_CONFLICT;
        return ResponseEntity
            .status(errorCode.getHttpStatus())
            .body(new ErrorResponse(LocalDateTime.now(), errorCode.name(), errorCode.getMessage()));
    }
}
