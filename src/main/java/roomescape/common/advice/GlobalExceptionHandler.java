package roomescape.common.advice;

import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import roomescape.common.exception.AlreadyInUseException;
import roomescape.common.exception.DuplicateException;
import roomescape.common.exception.ExternalSystemException;
import roomescape.common.exception.IllegalDateTimeException;
import roomescape.common.exception.NotFoundException;
import roomescape.common.exception.PaymentException;
import roomescape.common.exception.UnauthorizedException;
import roomescape.payment.application.exception.OrderUpdateException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleUnhandledException(final Exception e) {
        String traceId = UUID.randomUUID().toString().substring(0, 8);
        log.error("[TraceID: {}] Unhandled Exception 발생 : ", traceId, e);
        return ResponseEntity.internalServerError().body("일시적인 서버 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.");
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<String> handleValidationException(final BindException e) {
        log.error("Bind Exception 발생 : {}", e.getMessage());
        return getStringResponseEntity(e.getBindingResult());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleMethodArgumentNotValidException(final MethodArgumentNotValidException e) {
        log.error("MethodArgumentNotValid Exception 발생 : {}", e.getMessage());
        return getStringResponseEntity(e.getBindingResult());
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<String> handleReservationNotFoundException(final NotFoundException e) {
        log.error("Reservation Not Found Exception 발생 : {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }

    @ExceptionHandler(AlreadyInUseException.class)
    public ResponseEntity<String> handleReservationTimeInUseException(final AlreadyInUseException e) {
        log.error("Reservation Already In Use Exception 발생 : {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> handleIllegalStateException(final IllegalStateException e) {
        log.error("Illegal State Exception 발생 : {}", e.getMessage());
        return ResponseEntity.unprocessableEntity().body(e.getMessage());
    }

    @ExceptionHandler(DuplicateException.class)
    public ResponseEntity<String> handleDuplicateException(final DuplicateException e) {
        log.error("Duplicate Exception 발생 : {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
    }

    @ExceptionHandler(IllegalDateTimeException.class)
    public ResponseEntity<String> handleIllegalDateTimeException(final IllegalDateTimeException e) {
        log.error("Illegal Date Time Exception 발생 : {}", e.getMessage());
        return ResponseEntity.unprocessableEntity().body(e.getMessage());
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<String> handleUnauthorizedReservationChangeException(final UnauthorizedException e) {
        log.error("Unauthorized Exception 발생 : {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
    }

    @ExceptionHandler(OrderUpdateException.class)
    public ResponseEntity<String> handleOrderException(OrderUpdateException e) {
        log.error("주문 상태 갱신 실패 (자동 환불 처리됨): {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
    }

    @ExceptionHandler(PaymentException.class)
    public ResponseEntity<String> handlePaymentException(PaymentException e) {
        log.warn("결제 처리 오류: {}", e.getMessage());
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(ExternalSystemException.class)
    public ResponseEntity<String> handleExternalSystemException(ExternalSystemException e) {
        log.error("외부 서비스 오류 발생: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("결제 서비스 이용이 일시적으로 제한되었습니다.");
    }

    private ResponseEntity<String> getStringResponseEntity(final BindingResult e) {
        String message = e
                .getAllErrors()
                .getFirst()
                .getDefaultMessage();
        return ResponseEntity.badRequest().body(message);
    }
}
