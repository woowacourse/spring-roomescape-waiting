package roomescape.global;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import roomescape.global.exception.ErrorCode;
import roomescape.global.exception.ErrorResponse;
import roomescape.global.exception.RoomescapeException;
import roomescape.payment.OutboundRateLimitException;
import roomescape.payment.PaymentConnectionException;
import roomescape.payment.PaymentResultUnknownException;
import roomescape.payment.TossPaymentException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        ErrorResponse response = ErrorResponse.of(ErrorCode.INVALID_REQUEST);

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    protected ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        ErrorResponse response = ErrorResponse.of(ErrorCode.INVALID_DATE_FORMAT);

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(RoomescapeException.class)
    protected ResponseEntity<ErrorResponse> handleRoomescapeException(RoomescapeException ex) {
        ErrorCode error = ex.getErrorCode();

        return ResponseEntity.status(error.getStatus()).body(ErrorResponse.of(error));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    protected ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException ex) {
        ErrorResponse response = ErrorResponse.of(ErrorCode.INVALID_REQUEST);

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(TossPaymentException.class)
    protected ResponseEntity<ErrorResponse> handleTossPaymentException(TossPaymentException ex) {
        // 토스가 명확히 거절한 결제 ("거절")
        return ResponseEntity.badRequest().body(new ErrorResponse("TOSS_PAYMENT_ERROR", ex.getMessage()));
    }

    @ExceptionHandler(PaymentConnectionException.class)
    protected ResponseEntity<ErrorResponse> handlePaymentConnectionException(PaymentConnectionException ex) {
        // 연결 실패: 요청이 토스에 닿지 못함 → 재시도 가능 ("답 없음")
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ErrorResponse("PAYMENT_CONNECTION_ERROR", ex.getMessage()));
    }

    @ExceptionHandler(PaymentResultUnknownException.class)
    protected ResponseEntity<ErrorResponse> handlePaymentResultUnknownException(PaymentResultUnknownException ex) {
        // read timeout: 승인 여부 불명 → "결제 실패"로 단정하지 않고 "확인 필요"로 안내
        return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT)
                .body(new ErrorResponse("PAYMENT_RESULT_UNKNOWN", ex.getMessage()));
    }

    @ExceptionHandler(OutboundRateLimitException.class)
    protected ResponseEntity<ErrorResponse> handleOutboundRateLimitException(OutboundRateLimitException ex) {
        // 나가는 호출이 자체 한도를 넘어 외부로 보내지 않음 → 잠시 후 재시도 안내
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ErrorResponse("OUTBOUND_RATE_LIMIT", ex.getMessage()));
    }
}
