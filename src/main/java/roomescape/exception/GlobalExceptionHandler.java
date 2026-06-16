package roomescape.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
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

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

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

    @ExceptionHandler({ConcurrencyConflictException.class, ConcurrencyFailureException.class})
    public ResponseEntity<ErrorResponse> handleConcurrencyConflict() {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("LOCK_CONFLICT", "일시적 충돌이 발생했습니다. 다시 시도해주세요."));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handle(DataIntegrityViolationException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("DATA_INTEGRITY_VIOLATION", "데이터 충돌이 발생했습니다. 다시 시도해주세요."));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handle(HttpMessageNotReadableException e) {
        Throwable cause = e.getCause();
        while (cause != null && !(cause instanceof InvalidInputException)) {
            cause = cause.getCause();
        }
        if (cause == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("INVALID_INPUT", "요청 본문을 읽을 수 없습니다."));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("INVALID_INPUT", cause.getMessage()));
    }

    @ExceptionHandler(PaymentAmountMismatchException.class)
    public ResponseEntity<ErrorResponse> handle(PaymentAmountMismatchException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("PAYMENT_AMOUNT_MISMATCH", "결제 금액이 주문 정보와 일치하지 않습니다."));
    }

    @ExceptionHandler(InvalidPaymentRequestException.class)
    public ResponseEntity<ErrorResponse> handle(InvalidPaymentRequestException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("INVALID_PAYMENT_REQUEST", "결제 요청이 중복되었거나 만료되었습니다. 다시 시도해주세요."));
    }

    @ExceptionHandler(CardRejectedException.class)
    public ResponseEntity<ErrorResponse> handle(CardRejectedException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("CARD_REJECTED", "카드 결제가 거절되었습니다. 다른 카드로 시도해주세요."));
    }

    @ExceptionHandler(AlreadyProcessedException.class)
    public ResponseEntity<ErrorResponse> handle(AlreadyProcessedException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("ALREADY_PROCESSED_PAYMENT", "이미 처리된 결제입니다. 예약 내역을 확인해주세요."));
    }

    @ExceptionHandler(PaymentNotFoundException.class)
    public ResponseEntity<ErrorResponse> handle(PaymentNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("PAYMENT_NOT_FOUND", "결제 건을 찾을 수 없습니다."));
    }

    @ExceptionHandler(PaymentAuthException.class)
    public ResponseEntity<ErrorResponse> handle(PaymentAuthException e) {
        log.error("[결제 키 설정 오류] 운영 확인 필요: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("PAYMENT_CONFIG_ERROR", "결제 처리 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요."));
    }

    @ExceptionHandler(PaymentInternalException.class)
    public ResponseEntity<ErrorResponse> handle(PaymentInternalException e) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(new ErrorResponse("PAYMENT_GATEWAY_ERROR", "결제사 처리 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요."));
    }

    @ExceptionHandler(PaymentConfirmException.class)
    public ResponseEntity<ErrorResponse> handle(PaymentConfirmException e) {
        log.warn("[결제 승인 실패] 미정의 오류: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("PAYMENT_CONFIRM_FAILED", "결제 승인에 실패했습니다. 잠시 후 다시 시도해주세요."));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handle(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("INTERNER_SERVER_ERROR", "서버에 오류가 발생하였습니다. 다시 시도해주세요."));
    }
}
