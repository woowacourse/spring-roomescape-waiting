package roomescape.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final String MESSAGE_FORMAT = "%s 필드명 : [%s]";
    private static final String MESSAGE_FORMAT_WITH_VALUE = "%s 필드명 : [%s], 값 : [%s]";

    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    @ExceptionHandler(value = InvalidClientFieldException.class)
    public ResponseEntity<ErrorResponse> handleInvalidClientField(InvalidClientFieldException e,
                                                                  HttpServletRequest req) {
        logWarn(e);
        final String message = MESSAGE_FORMAT.formatted(e.getErrorType().getMessage(), e.getFieldName());
        final ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST, req.getRequestURI(), message);
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(value = InvalidClientFieldWithValueException.class)
    public ResponseEntity<ErrorResponse> handleInvalidClientFieldWithValue(InvalidClientFieldWithValueException e,
                                                                           HttpServletRequest req) {
        logWarn(e);
        final String message = MESSAGE_FORMAT_WITH_VALUE.formatted(e.getErrorType().getMessage(), e.getFieldName(),
                e.getValue());
        final ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST, req.getRequestURI(), message);
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(ReservationFailException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(ReservationFailException e, HttpServletRequest req) {
        logWarn(e);
        final ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST, req.getRequestURI(),
                e.getMessage());
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(DeleteNotAllowException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(DeleteNotAllowException e, HttpServletRequest req) {
        logWarn(e);
        final ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST, req.getRequestURI(),
                e.getMessage());
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(SignupFailException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(SignupFailException e, HttpServletRequest req) {
        logWarn(e);
        final ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST, req.getRequestURI(),
                e.getMessage());
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(DuplicateNotAllowException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(DuplicateNotAllowException e, HttpServletRequest req) {
        logWarn(e);
        final ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST, req.getRequestURI(),
                e.getMessage());
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(AccessNotAllowException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(AccessNotAllowException e, HttpServletRequest req) {
        logWarn(e);
        final ErrorResponse errorResponse = new ErrorResponse(HttpStatus.UNAUTHORIZED, req.getRequestURI(),
                e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Object handleMissingServletRequestParameterException(
            MissingServletRequestParameterException e, HttpServletRequest req) {
        logWarn(e);
        final String message = MESSAGE_FORMAT
                .formatted(ErrorType.EMPTY_VALUE_NOT_ALLOWED.getMessage(), e.getParameterName());
        final ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST, req.getRequestURI(), message);
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException e,
                                                            HttpServletRequest req) {
        logWarn(e);
        final String message = MESSAGE_FORMAT
                .formatted(ErrorType.INVALID_DATA_TYPE.getMessage(), e.getName(), e.getValue().toString());
        final ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST, req.getRequestURI(), message);
        return ResponseEntity.badRequest().body(errorResponse);
    }

    private void logWarn(Exception exception) {
        logger.warn(exception.toString());
    }
}
