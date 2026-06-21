package roomescape.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import roomescape.infrastructure.toss.TossPaymentException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ReservationAlreadyExistException.class)
    public ResponseEntity<ErrorResponse> handle(ReservationAlreadyExistException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("DUPLICATE_RESERVATION", e.getMessage()));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handle(ResourceNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("RESERVATION_NOT_FOUND", e.getMessage()));
    }

    @ExceptionHandler(ReservationTimeNotFoundException.class)
    public ResponseEntity<ErrorResponse> handle(ReservationTimeNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("TIME_NOT_FOUND", e.getMessage()));
    }

    @ExceptionHandler(ThemeNotFoundException.class)
    public ResponseEntity<ErrorResponse> handle(ThemeNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("THEME_NOT_FOUND", e.getMessage()));
    }

    @ExceptionHandler(ExpiredDateTimeException.class)
    public ResponseEntity<ErrorResponse> handle(ExpiredDateTimeException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("INVALID_DATE_OR_TIME", e.getMessage()));
    }

    @ExceptionHandler(ReferencedDataException.class)
    public ResponseEntity<ErrorResponse> handle(ReferencedDataException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("DIFFERENCE_DATA_EXISTS", e.getMessage()));
    }

    @ExceptionHandler(InvalidInputException.class)
    public ResponseEntity<ErrorResponse> handle(InvalidInputException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("INVALID_INPUT", e.getMessage()));
    }

    @ExceptionHandler(WaitingNotFoundException.class)
    public ResponseEntity<ErrorResponse> handle(WaitingNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("WAITING_NOT_FOUND", e.getMessage()));
    }

    @ExceptionHandler(ReservationWaitingAlreadyExistException.class)
    public ResponseEntity<ErrorResponse> handle(ReservationWaitingAlreadyExistException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("DUPLICATE_WAITING", e.getMessage()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handle(DataIntegrityViolationException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("DATA_INTEGRITY_VIOLATION", "요청을 처리할 수 없습니다."));
    }

    @ExceptionHandler(PaymentAmountMismatchException.class)
    public ResponseEntity<ErrorResponse> handle(PaymentAmountMismatchException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("PAYMENT_AMOUNT_MISMATCH", e.getMessage()));
    }

    @ExceptionHandler(TossPaymentException.GatewayConfig.class)
    public ResponseEntity<ErrorResponse> handle(TossPaymentException.GatewayConfig e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(e.getCode(), "결제 시스템 오류입니다. 관리자에게 문의하세요."));
    }

    @ExceptionHandler(TossPaymentException.class)
    public ResponseEntity<ErrorResponse> handle(TossPaymentException e) {
        return ResponseEntity.status(e.getStatus())
                .body(new ErrorResponse(e.getCode(), e.getMessage()));
    }

    @ExceptionHandler(PaymentConnectionException.class)
    public ResponseEntity<ErrorResponse> handle(PaymentConnectionException e) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ErrorResponse("PAYMENT_CONNECTION_FAILED", e.getMessage()));
    }

    @ExceptionHandler(PaymentUncertainException.class)
    public ResponseEntity<ErrorResponse> handle(PaymentUncertainException e) {
        return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT)
                .body(new ErrorResponse("PAYMENT_UNCERTAIN", e.getMessage()));
    }

    @ExceptionHandler(PaymentRateLimitException.class)
    public ResponseEntity<ErrorResponse> handle(PaymentRateLimitException e) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(new ErrorResponse("PAYMENT_RATE_LIMIT_EXCEEDED", e.getMessage()));
    }

    @ExceptionHandler(OutboundRateLimitException.class)
    public ResponseEntity<ErrorResponse> handle(OutboundRateLimitException e) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(new ErrorResponse("OUTBOUND_RATE_LIMIT_EXCEEDED", e.getMessage()));
    }
}
