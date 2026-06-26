package roomescape.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import roomescape.domain.exception.DomainValidationException;
import roomescape.payment.PaymentAmountMismatchException;
import roomescape.payment.PaymentConnectionException;
import roomescape.payment.PaymentRetryExhaustedException;
import roomescape.payment.PaymentTimeoutException;
import roomescape.payment.client.OutboundRateLimitException;
import roomescape.payment.client.TossPaymentException;
import roomescape.service.exception.PastReservationException;
import roomescape.service.exception.ResourceConflictException;
import roomescape.service.exception.ResourceNotFoundException;
import roomescape.service.exception.UnauthorizedReservationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String INTERNAL_ERROR_MESSAGE = "서버 내부에 오류가 발생하였습니다.";

    @ExceptionHandler(PastReservationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handlePastReservation(PastReservationException e) {
        return new ErrorResponse(ErrorCode.PAST_RESERVATION.name(), e.getMessage());
    }

    @ExceptionHandler(PaymentAmountMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handlePaymentAmountMismatch(PaymentAmountMismatchException e) {
        log.warn("결제 금액 위변조 차단: {}", e.getMessage());
        return new ErrorResponse(ErrorCode.PAYMENT_AMOUNT_MISMATCH.name(), e.getMessage());
    }

    @ExceptionHandler(TossPaymentException.class)
    public ResponseEntity<ErrorResponse> handleTossPayment(TossPaymentException e) {
        log.warn("결제 승인 실패: status={}, code={}, message={}", e.getStatus(), e.getCode(), e.getMessage());
        ErrorResponse body = new ErrorResponse(e.getCode(), e.getMessage());
        return ResponseEntity.status(e.getStatus()).body(body);
    }

    @ExceptionHandler(PaymentTimeoutException.class)
    @ResponseStatus(HttpStatus.GATEWAY_TIMEOUT)
    public ErrorResponse handlePaymentTimeout(PaymentTimeoutException e) {
        log.warn("결제 승인 결과 불확실(read timeout): {}", e.getMessage());
        return new ErrorResponse(ErrorCode.PAYMENT_RESULT_UNKNOWN.name(),
                "결제 결과를 확인 중입니다. 잠시 후 결제 내역에서 상태를 확인해 주세요.");
    }

    @ExceptionHandler(PaymentConnectionException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public ErrorResponse handlePaymentConnection(PaymentConnectionException e) {
        log.warn("결제 서버 연결 실패: {}", e.getMessage());
        return new ErrorResponse(ErrorCode.PAYMENT_GATEWAY_UNAVAILABLE.name(),
                "결제 서버에 일시적으로 연결할 수 없습니다. 잠시 후 다시 시도해 주세요.");
    }

    @ExceptionHandler(OutboundRateLimitException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public ErrorResponse handleOutboundRateLimit(OutboundRateLimitException e) {
        log.warn("나가는 결제 호출 Rate Limit 초과로 차단: {}", e.getMessage());
        return new ErrorResponse(ErrorCode.PAYMENT_GATEWAY_BUSY.name(),
                "결제 요청이 많아 잠시 후 다시 시도해 주세요.");
    }

    @ExceptionHandler(PaymentRetryExhaustedException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public ErrorResponse handlePaymentRetryExhausted(PaymentRetryExhaustedException e) {
        log.warn("결제 서버 혼잡으로 재시도 한도 초과: {}", e.getMessage());
        return new ErrorResponse(ErrorCode.PAYMENT_GATEWAY_BUSY.name(),
                "결제 서버가 혼잡합니다. 잠시 후 다시 시도해 주세요.");
    }

    @ExceptionHandler(ResourceConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleConflict(ResourceConflictException e) {
        return new ErrorResponse(ErrorCode.CONFLICT.name(), e.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleDataIntegrityViolation(DataIntegrityViolationException e) {
        log.warn("데이터 무결성 제약 위반(동시 중복 요청 등): {}", e.getMessage());
        return new ErrorResponse(ErrorCode.CONFLICT.name(), "이미 예약 또는 대기중인 시간입니다.");
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFound(ResourceNotFoundException e) {
        return new ErrorResponse(ErrorCode.NOT_FOUND.name(), e.getMessage());
    }

    @ExceptionHandler(UnauthorizedReservationException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleUnauthorized(UnauthorizedReservationException e) {
        return new ErrorResponse(ErrorCode.FORBIDDEN.name(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleBodyValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return new ErrorResponse(ErrorCode.INVALID_INPUT.name(), message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleConstraintViolation(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));
        return new ErrorResponse(ErrorCode.INVALID_INPUT.name(), message);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        return new ErrorResponse(ErrorCode.INVALID_PARAMETER.name(), "파라미터 '" + e.getName() + "'의 형식이 올바르지 않습니다");
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMissingParam(MissingServletRequestParameterException e) {
        return new ErrorResponse(ErrorCode.INVALID_PARAMETER.name(), "필수 파라미터 '" + e.getParameterName() + "'가 누락되었습니다");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleNotReadable(HttpMessageNotReadableException e) {
        return new ErrorResponse(ErrorCode.INVALID_PARAMETER.name(), "요청 본문 형식이 올바르지 않습니다");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNoResource(NoResourceFoundException e) {
        return new ErrorResponse(ErrorCode.RESOURCE_NOT_FOUND.name(), "요청한 리소스를 찾을 수 없습니다");
    }

    @ExceptionHandler(DomainValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleDomainValidation(DomainValidationException e) {
        log.error("도메인 검증 실패 — 잘못된 값이 도메인 계층에 도달했습니다: {}", e.getMessage(), e);
        return new ErrorResponse(ErrorCode.DOMAIN_VALIDATION_FAILED.name(), INTERNAL_ERROR_MESSAGE);
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleRuntime(RuntimeException e) {
        log.error("처리되지 않은 런타임 예외가 발생했습니다", e);
        return new ErrorResponse(ErrorCode.INTERNAL_SERVER_ERROR.name(), INTERNAL_ERROR_MESSAGE);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleUnexpected(Exception e) {
        log.error("처리되지 않은 예외가 발생했습니다", e);
        return new ErrorResponse(ErrorCode.INTERNAL_SERVER_ERROR.name(), INTERNAL_ERROR_MESSAGE);
    }
}
