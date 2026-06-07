package roomescape.presentation.error;

import jakarta.validation.ConstraintViolationException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import roomescape.domain.exception.BusinessException;
import roomescape.domain.exception.ErrorCode;
import roomescape.presentation.error.ErrorResponse.FieldErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException exception) {
        ErrorCode errorCode = exception.getErrorCode();
        log.warn("Business exception handled: {}", errorCode.name());

        return ResponseEntity.status(resolveStatus(errorCode))
                .body(ErrorResponse.of(errorCode.name(), errorCode.message()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException exception
    ) {
        log.warn("Validation failed: {}", exception.getMessage());

        List<FieldErrorResponse> errors = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new FieldErrorResponse(
                        error.getField(),
                        error.getDefaultMessage()
                ))
                .toList();

        return badRequest(errors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(
            ConstraintViolationException exception
    ) {
        log.warn("Constraint violation: {}", exception.getMessage());

        List<FieldErrorResponse> errors = exception.getConstraintViolations()
                .stream()
                .map(error -> new FieldErrorResponse(
                        extractFieldName(error.getPropertyPath().toString()),
                        error.getMessage()
                ))
                .toList();

        return badRequest(errors);
    }

    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class,
            IllegalArgumentException.class
    })
    public ResponseEntity<ErrorResponse> handleInputException(Exception exception) {
        log.warn("Bad request: {} - {}", exception.getClass().getSimpleName(), exception.getMessage());

        return badRequest();
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception exception) {
        log.error("Unhandled exception", exception);

        return ResponseEntity.internalServerError()
                .body(ErrorResponse.of(
                        ErrorCode.INTERNAL_SERVER_ERROR.name(),
                        ErrorCode.INTERNAL_SERVER_ERROR.message()
                ));
    }

    private ResponseEntity<ErrorResponse> badRequest() {
        return ResponseEntity.badRequest()
                .body(ErrorResponse.of(
                        ErrorCode.INPUT_FORMAT_ERROR.name(),
                        ErrorCode.INPUT_FORMAT_ERROR.message()
                ));
    }

    private ResponseEntity<ErrorResponse> badRequest(List<FieldErrorResponse> errors) {
        return ResponseEntity.badRequest()
                .body(ErrorResponse.of(
                        ErrorCode.INPUT_FORMAT_ERROR.name(),
                        ErrorCode.INPUT_FORMAT_ERROR.message(),
                        errors
                ));
    }

    private String extractFieldName(String propertyPath) {
        int lastDotIndex = propertyPath.lastIndexOf('.');

        if (lastDotIndex == -1) {
            return propertyPath;
        }

        return propertyPath.substring(lastDotIndex + 1);
    }

    private HttpStatus resolveStatus(ErrorCode errorCode) {
        return switch (errorCode) {
            case RESERVATION_SLOT_IN_PAST,
                 RESERVATION_ALREADY_EXISTS,
                 RESERVATION_SAME_SLOT,
                 RESERVATION_TIME_IN_USE,
                 THEME_IN_USE, INPUT_FORMAT_ERROR -> HttpStatus.BAD_REQUEST;

            case RESERVATION_SLOT_NOT_FOUND,
                 RESERVATION_NOT_FOUND,
                 RESERVATION_TIME_NOT_FOUND,
                 THEME_NOT_FOUND,
                 USER_NOT_FOUND -> HttpStatus.NOT_FOUND;

            case RESERVATION_NOT_OWNER -> HttpStatus.FORBIDDEN;

            case INTERNAL_SERVER_ERROR -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}
