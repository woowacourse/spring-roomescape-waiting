package roomescape.exception;

import jakarta.validation.ConstraintViolationException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(RoomEscapeException.class)
    public ResponseEntity<ErrorResponse> handleRoomEscapeException(
            RoomEscapeException e
    ) {
        return ResponseEntity
                .status(e.getErrorStatus())
                .body(new ErrorResponse(e.getErrorCode().getErrorName(), e.getMessage(), null));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException e
    ) {
        List<ErrorResponse.FieldErrorDetail> errors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new ErrorResponse.FieldErrorDetail(
                        error.getField(),
                        error.getDefaultMessage()
                ))
                .toList();

        return ResponseEntity
                .badRequest()
                .body(new ErrorResponse(
                        "INVALID_ARGUMENT",
                        "입력 형식이 올바르지 않습니다.",
                        errors)
                );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(
            ConstraintViolationException e
    ) {
        List<ErrorResponse.FieldErrorDetail> errors = e.getConstraintViolations()
                .stream()
                .map(constraintViolation -> {
                    String propertyPath = constraintViolation.getPropertyPath().toString();
                    String fieldName = propertyPath.substring(propertyPath.lastIndexOf('.') + 1);

                    return new ErrorResponse.FieldErrorDetail(
                            fieldName,
                            constraintViolation.getMessage()
                    );
                }).toList();

        return ResponseEntity
                .badRequest()
                .body(new ErrorResponse(
                        "INVALID_ARGUMENT",
                        "입력 형식이 올바르지 않습니다.",
                        errors
                ));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException e
    ) {
        return ResponseEntity
                .badRequest()
                .body(new ErrorResponse(
                        "INVALID_ARGUMENT",
                        "입력 형식이 올바르지 않습니다.",
                        null
                ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException e
    ) {
        log.warn("IllegalArgumentException occurred: {}", e.getMessage());

        return ResponseEntity
                .badRequest()
                .body(new ErrorResponse(
                        "INVALID_ARGUMENT",
                        "잘못된 요청입니다.",
                        null
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(
            Exception e
    ) {
        log.error("Unhandled server error occurred", e);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(
                        "INTERNAL_SERVER_ERROR",
                        "서버 내부 오류가 발생했습니다.",
                        null
                ));
    }
}
