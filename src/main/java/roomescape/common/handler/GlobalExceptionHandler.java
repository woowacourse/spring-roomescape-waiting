package roomescape.common.handler;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import roomescape.common.exception.BadRequestException;
import roomescape.common.exception.ConflictException;
import roomescape.common.exception.ForbiddenException;
import roomescape.common.exception.NotFoundException;
import roomescape.common.exception.OutboundRateLimitException;
import roomescape.common.exception.PaymentAmountMismatchException;
import roomescape.common.exception.PaymentGatewayException;
import roomescape.common.exception.UnprocessableEntityException;
import roomescape.domain.history.PaymentHistory;
import roomescape.domain.history.PaymentHistoryRepository;
import roomescape.infrastructure.payment.toss.TossPaymentException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final PaymentHistoryRepository historyRepository;

    public GlobalExceptionHandler(PaymentHistoryRepository historyRepository) {
        this.historyRepository = historyRepository;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<GlobalErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
        List<ErrorDetail> fieldErrors = e.getBindingResult().getFieldErrors().stream()
                .map(ErrorDetail::from)
                .toList();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(GlobalErrorResponse.of("입력값 이상", fieldErrors));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<GlobalErrorResponse> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException e) {

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(GlobalErrorResponse.from("요청 JSON 형식이 잘못되었습니다."));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<GlobalErrorResponse> handleIllegalArgumentException(IllegalArgumentException e) {

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(GlobalErrorResponse.from(e.getMessage()));
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<GlobalErrorResponse> handleBadRequestException(BadRequestException e) {

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(GlobalErrorResponse.from(e.getMessage()));
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<GlobalErrorResponse> handleForbiddenException(ForbiddenException e) {

        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(GlobalErrorResponse.from(e.getMessage()));
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<GlobalErrorResponse> handleNotFoundException(NotFoundException e) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(GlobalErrorResponse.from(e.getMessage()));
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<GlobalErrorResponse> handleConflictException(ConflictException e) {

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(GlobalErrorResponse.from(e.getMessage()));
    }

    @ExceptionHandler(UnprocessableEntityException.class)
    public ResponseEntity<GlobalErrorResponse> handleUnprocessableEntityException(UnprocessableEntityException e) {

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(GlobalErrorResponse.from(e.getMessage()));
    }

    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<GlobalErrorResponse> handleNullPointerException(NullPointerException e) {

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(GlobalErrorResponse.from(e.getMessage()));
    }

    @ExceptionHandler(PaymentAmountMismatchException.class)
    public ResponseEntity<GlobalErrorResponse> handlePaymentAmountMismatchException(PaymentAmountMismatchException e) {
        historyRepository.save(PaymentHistory.of(
                false, e.getOrderId(), e.getAmount(), e.getPaymentKey(), "금액 불일치", e.getMessage()
        ));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(GlobalErrorResponse.from(e.getMessage()));
    }

    @ExceptionHandler(OutboundRateLimitException.class)
    public ResponseEntity<GlobalErrorResponse> handleOutboundRateLimitException(OutboundRateLimitException e) {
        historyRepository.save(PaymentHistory.of(
                false, e.getOrderId(), e.getAmount(), e.getPaymentKey(), "나가는 한도 차단", e.getMessage()
        ));

        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(GlobalErrorResponse.from(e.getMessage()));
    }

    @ExceptionHandler(PaymentGatewayException.class)
    public ResponseEntity<GlobalErrorResponse> handleTossPaymentGatewayException(PaymentGatewayException e) {
        historyRepository.save(PaymentHistory.of(
                false, e.getOrderId(), e.getAmount(), e.getPaymentKey(), "게이트웨이 거부/통신에러",
                e.getCause().getClass().getSimpleName()
        ));

        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(GlobalErrorResponse.from(e.getMessage()));
    }

    @ExceptionHandler(TossPaymentException.class)
    public ResponseEntity<GlobalErrorResponse> handleTossPaymentException(TossPaymentException e) {

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(GlobalErrorResponse.from(e.getMessage()));
    }
}
