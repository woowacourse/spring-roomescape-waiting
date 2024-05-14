package roomescape.exception;

import jakarta.servlet.http.HttpServletRequest;
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

    @ExceptionHandler(value = InvalidClientFieldException.class)
    public ResponseEntity<ErrorResponse> handleInvalidClientField(InvalidClientFieldException ex, HttpServletRequest req) {
        final String message = MESSAGE_FORMAT.formatted(ex.getErrorType().getMessage(), ex.getFieldName());
        final ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST, req.getRequestURI(), message);
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(value = InvalidClientFieldWithValueException.class)
    public ResponseEntity<ErrorResponse> handleInvalidClientFieldWithValue(InvalidClientFieldWithValueException ex, HttpServletRequest req) {
        final String message = MESSAGE_FORMAT_WITH_VALUE.formatted(ex.getErrorType().getMessage(), ex.getFieldName(), ex.getValue());
        final ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST, req.getRequestURI(), message);
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(ReservationFailException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(ReservationFailException e, HttpServletRequest req) {
        final ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST, req.getRequestURI(), e.getMessage());
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(DeleteNotAllowException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(DeleteNotAllowException e, HttpServletRequest req) {
        final ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST, req.getRequestURI(), e.getMessage());
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(SignupFailException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(SignupFailException e, HttpServletRequest req) {
        final ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST, req.getRequestURI(), e.getMessage());
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(DuplicateNotAllowException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(DuplicateNotAllowException e, HttpServletRequest req) {
        final ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST, req.getRequestURI(), e.getMessage());
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(AccessNotAllowException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(AccessNotAllowException e, HttpServletRequest req) {
        final ErrorResponse errorResponse = new ErrorResponse(HttpStatus.UNAUTHORIZED, req.getRequestURI(), e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }


    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Object handleMissingServletRequestParameterException(
            MissingServletRequestParameterException ex, HttpServletRequest req) {
        final String message = MESSAGE_FORMAT.formatted(ErrorType.EMPTY_VALUE_NOT_ALLOWED, ex.getParameterName());
        final ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST, req.getRequestURI(), message);
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException e, HttpServletRequest req) {
        final String message = MESSAGE_FORMAT.formatted(ErrorType.INVALID_DATA_TYPE, e.getName(), e.getValue().toString());
        final ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST, req.getRequestURI(), message);
        return ResponseEntity.badRequest().body(errorResponse);
    }
}
