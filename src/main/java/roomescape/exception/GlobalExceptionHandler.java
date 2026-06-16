package roomescape.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import roomescape.payment.client.TossPaymentException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(RoomescapeException.class)
    public ResponseEntity<ErrorResponse> handleRoomEscapeException(RoomescapeException exception) {
        ErrorCode code = exception.getErrorCode();
        return ResponseEntity
                .status(code.getStatus())
                .body(
                        ErrorResponse.of(code.getMessage())
                );
    }

    @ExceptionHandler(TossPaymentException.class)
    public ResponseEntity<ErrorResponse> handleTossPaymentException(TossPaymentException exception) {
        if (exception instanceof TossPaymentException.GatewayConfig) {
            log.error("[운영 알람] Toss API 키 설정 오류 code={} message={}", exception.getCode(), exception.getMessage());
        } else if (exception instanceof TossPaymentException.Retryable) {
            log.error("[운영 알람] Toss 내부 오류 재시도 초과 code={} message={}", exception.getCode(), exception.getMessage());
        } else {
            log.warn("Toss 결제 오류 code={} message={}", exception.getCode(), exception.getMessage());
        }
        return ResponseEntity
                .status(exception.getStatus())
                .body(ErrorResponse.of(exception.getMessage()));
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

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorResponse> handleDataAccessException(DataAccessException exception) {
        ErrorCode code = ErrorCode.SERVER_OVERLOADED;
        return ResponseEntity
                .status(code.getStatus())
                .body(ErrorResponse.of(code.getMessage()));
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
