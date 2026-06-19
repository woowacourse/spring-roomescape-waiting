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
import roomescape.payment.NetworkUncertain;
import roomescape.payment.PaymentGatewayConfigException;
import roomescape.payment.PaymentGatewayException;
import roomescape.payment.PaymentTransientException;

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

    @ExceptionHandler(PaymentGatewayConfigException.class)
    public ResponseEntity<ErrorResponse> handleGatewayConfig(PaymentGatewayConfigException exception) {
        log.error("[운영 알람] 결제 게이트웨이 설정 오류 code={} message={}", exception.getCode(), exception.getMessage());
        return ResponseEntity
                .status(exception.getStatus())
                .body(ErrorResponse.of(exception.getMessage()));
    }

    @ExceptionHandler(PaymentTransientException.class)
    public ResponseEntity<ErrorResponse> handlePaymentTransient(PaymentTransientException exception) {
        log.error("[운영 알람] 결제 게이트웨이 일시적 오류 재시도 초과 code={} message={}", exception.getCode(), exception.getMessage());
        return ResponseEntity
                .status(exception.getStatus())
                .body(ErrorResponse.of(exception.getMessage()));
    }

    @ExceptionHandler(NetworkUncertain.class)
    public ResponseEntity<ErrorResponse> handleTossNetworkUncertain(NetworkUncertain exception) {
        log.error("[운영 알람] 결제 네트워크 오류 - 승인 여부 불명확 {}", exception.getMessage());
        return ResponseEntity
                .status(exception.getStatus())
                .body(ErrorResponse.of(exception.getMessage()));
    }

    @ExceptionHandler(PaymentGatewayException.class)
    public ResponseEntity<ErrorResponse> handlePaymentGatewayException(PaymentGatewayException exception) {
        log.warn("결제 게이트웨이 오류 code={} message={}", exception.getCode(), exception.getMessage());
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
