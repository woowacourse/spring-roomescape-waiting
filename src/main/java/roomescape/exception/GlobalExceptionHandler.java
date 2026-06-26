package roomescape.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.validation.ConstraintViolationException;
import roomescape.infrastructure.toss.OutboundRateLimitException;
import roomescape.infrastructure.toss.TossPaymentException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException e) {
        ErrorCode errorCode = e.getErrorCode();
        ErrorResponse errorResponse = new ErrorResponse(errorCode.getCode(), errorCode.name(), errorCode.getMessage());
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(errorResponse);
    }

    @ExceptionHandler(TossPaymentException.class)
    public ResponseEntity<ErrorResponse> handleTossPaymentException(TossPaymentException e) {
        ErrorResponse errorResponse = new ErrorResponse(e.getCode(), e.getClass().getSimpleName(), e.getMessage());
        return ResponseEntity
                .status(e.getStatus())
                .body(errorResponse);
    }

    @ExceptionHandler(OutboundRateLimitException.class)
    public ResponseEntity<ErrorResponse> handleOutboundRateLimitException(OutboundRateLimitException e) {
        ErrorResponse errorResponse = new ErrorResponse("OUTBOUND_RATE_LIMIT_429", e.getClass().getSimpleName(), e.getMessage());
        return ResponseEntity
                .status(429)
                .header("Retry-After", String.valueOf(e.getRetryAfterSeconds()))
                .body(errorResponse);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException e
    ) {
        ErrorCode errorCode = ErrorCode.COMMON_BAD_REQUEST;

        String message = e.getConstraintViolations()
                .stream()
                .findFirst()
                .map(violation -> violation.getMessage())
                .orElse(errorCode.getMessage());


        ErrorResponse errorResponse = new ErrorResponse(errorCode.getCode(), errorCode.name(), message);

        return ResponseEntity
                .badRequest()
                .body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e
    ) {
        ErrorCode errorCode = ErrorCode.COMMON_BAD_REQUEST;

        String message = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(fieldError -> fieldError.getDefaultMessage())
                .orElse(ErrorCode.COMMON_BAD_REQUEST.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(errorCode.getCode(), errorCode.name(), message);

        return ResponseEntity
                .badRequest()
                .body(errorResponse);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException e
    ) {
        ErrorCode errorCode = ErrorCode.COMMON_BAD_REQUEST;

        ErrorResponse errorResponse = new ErrorResponse(errorCode.getCode(), errorCode.name(), errorCode.getMessage());

        return ResponseEntity
                .badRequest()
                .body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException e
    ) {
        ErrorCode errorCode = ErrorCode.COMMON_BAD_REQUEST;

        ErrorResponse errorResponse = new ErrorResponse(errorCode.getCode(), errorCode.name(), errorCode.getMessage());

        return ResponseEntity
                .badRequest()
                .body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(
            Exception e
    ) {
        ErrorCode errorCode = ErrorCode.COMMON_SERVER_ERROR;
        ErrorResponse errorResponse = new ErrorResponse(errorCode.getCode(), errorCode.name(), errorCode.getMessage());
        return ResponseEntity
                .internalServerError()
                .body(errorResponse);
    }
}
