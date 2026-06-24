package roomescape.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import roomescape.domain.payment.PaymentException;
import roomescape.domain.payment.dto.PaymentErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RoomescapeException.class)
    public ResponseEntity<ErrorResponse> handleRoomEscapeException(RoomescapeException exception) {
        ErrorCode code = exception.getErrorCode();
        return ResponseEntity
            .status(code.getStatus())
            .body(
                ErrorResponse.of(code.getMessage())
            );
    }

    @ExceptionHandler(PaymentException.class)
    public ResponseEntity<PaymentErrorResponse> handlePaymentException(PaymentException exception) {
        return ResponseEntity
            .status(exception.getStatusCode())
            .body(new PaymentErrorResponse(exception.getCode(), exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentException(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult()
            .getFieldErrors()
            .getFirst()
            .getDefaultMessage();
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse.of(message));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException exception) {
        String message = exception.getConstraintViolations()
            .stream()
            .findFirst()
            .map(ConstraintViolation::getMessage)
            .orElse("잘못된 요청입니다.");
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse.of(message));
    }
}
