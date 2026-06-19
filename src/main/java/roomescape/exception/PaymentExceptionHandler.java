package roomescape.exception;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import roomescape.exception.PaymentException.AlreadyProcessedException;
import roomescape.exception.PaymentException.CardRejectedException;
import roomescape.exception.PaymentException.InvalidPaymentRequestException;
import roomescape.exception.PaymentException.PaymentAmountMismatchException;
import roomescape.exception.PaymentException.PaymentAuthException;
import roomescape.exception.PaymentException.PaymentConfirmException;
import roomescape.exception.PaymentException.PaymentInternalException;
import roomescape.exception.PaymentException.PaymentNotFoundException;
import roomescape.exception.PaymentException.PaymentRateLimitException;
import roomescape.exception.PaymentException.PaymentResultUnknownException;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class PaymentExceptionHandler {

    @ExceptionHandler(PaymentResultUnknownException.class)
    public ResponseEntity<ErrorResponse> handle(PaymentResultUnknownException e) {
        return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT)
                .body(new ErrorResponse("PAYMENT_RESULT_UNKNOWN", e.getMessage()));
    }

    @ExceptionHandler(PaymentAmountMismatchException.class)
    public ResponseEntity<ErrorResponse> handle(PaymentAmountMismatchException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("PAYMENT_AMOUNT_MISMATCH", e.getMessage()));
    }

    @ExceptionHandler(CardRejectedException.class)
    public ResponseEntity<ErrorResponse> handle(CardRejectedException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("CARD_REJECTED", e.getMessage()));
    }

    @ExceptionHandler(InvalidPaymentRequestException.class)
    public ResponseEntity<ErrorResponse> handle(InvalidPaymentRequestException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("INVALID_PAYMENT_REQUEST", e.getMessage()));
    }

    @ExceptionHandler(AlreadyProcessedException.class)
    public ResponseEntity<ErrorResponse> handle(AlreadyProcessedException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("ALREADY_PROCESSED_PAYMENT", e.getMessage()));
    }

    @ExceptionHandler(PaymentNotFoundException.class)
    public ResponseEntity<ErrorResponse> handle(PaymentNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("PAYMENT_NOT_FOUND", e.getMessage()));
    }

    @ExceptionHandler(PaymentAuthException.class)
    public ResponseEntity<ErrorResponse> handle(PaymentAuthException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("PAYMENT_CONFIG_ERROR", e.getMessage()));
    }

    @ExceptionHandler(PaymentInternalException.class)
    public ResponseEntity<ErrorResponse> handle(PaymentInternalException e) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(new ErrorResponse("PAYMENT_GATEWAY_ERROR", e.getMessage()));
    }

    @ExceptionHandler(PaymentConfirmException.class)
    public ResponseEntity<ErrorResponse> handle(PaymentConfirmException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("PAYMENT_CONFIRM_FAILED", e.getMessage()));
    }

    @ExceptionHandler(PaymentRateLimitException.class)
    public ResponseEntity<ErrorResponse> handle(PaymentRateLimitException e) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ErrorResponse("PAYMENT_GATEWAY_RATE_LIMITED", e.getMessage()));
    }
}
