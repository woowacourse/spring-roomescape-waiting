package roomescape.common.exception;

import java.util.Comparator;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.TypeMismatchException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import roomescape.payment.TossPaymentException;
import roomescape.ratelimit.OutboundRateLimitException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException e, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .sorted(Comparator.comparing(FieldError::getField))
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("유효하지 않은 입력값: {}", message);
        return ResponseEntity.badRequest().body(new ErrorResponse("INVALID_INPUT", message));
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException e, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        log.warn("읽을 수 없는 요청: {}", e.getMessage());
        return ResponseEntity.badRequest().body(new ErrorResponse("INVALID_INPUT", "요청 본문을 읽을 수 없습니다."));
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(
            TypeMismatchException e, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        log.warn("타입 변환 실패: {}", e.getMessage());
        return ResponseEntity.badRequest().body(new ErrorResponse("INVALID_INPUT", "요청 값의 타입이 올바르지 않습니다."));
    }

    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(
            HttpRequestMethodNotSupportedException e, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        log.warn("지원하지 않는 HTTP 메서드: {}", e.getMethod());
        return ResponseEntity.status(status).body(new ErrorResponse("METHOD_NOT_ALLOWED", "지원하지 않는 HTTP 메서드입니다."));
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            MissingServletRequestParameterException e, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        log.warn("필수 파라미터 누락: {}", e.getParameterName());
        return ResponseEntity.badRequest()
                .body(new ErrorResponse("INVALID_INPUT", e.getParameterName() + " 파라미터가 누락됐습니다."));
    }

    @Override
    protected ResponseEntity<Object> handleNoResourceFoundException(
            NoResourceFoundException e, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        log.warn("존재하지 않는 경로: {}", e.getResourcePath());
        return ResponseEntity.status(status).body(new ErrorResponse("NOT_FOUND", "존재하지 않는 경로입니다."));
    }

    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException e, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        log.warn("지원하지 않는 Content-Type: {}", e.getContentType());
        return ResponseEntity.status(status).body(new ErrorResponse("INVALID_INPUT", "지원하지 않는 Content-Type입니다."));
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception e, Object body, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        log.warn("Spring MVC 예외: {}", e.getMessage());
        return ResponseEntity.status(status)
                .body(new ErrorResponse(HttpStatus.valueOf(status.value()).name(), "잘못된 요청입니다."));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException e) {
        log.warn("잘못된 인자: {}", e.getMessage());
        return ResponseEntity.badRequest().body(new ErrorResponse("INVALID_INPUT", e.getMessage()));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException e) {
        log.warn("비즈니스 규칙 위반 [{}]: {}", e.getStatus(), e.getMessage());
        return ResponseEntity.status(e.getStatus()).body(ErrorResponse.from(e.getErrorCode()));
    }

    @ExceptionHandler(TossPaymentException.class)
    public ResponseEntity<ErrorResponse> handleTossPayment(TossPaymentException e) {
        if (e instanceof TossPaymentException.GatewayConfig) {
            log.error("Toss 결제 키 설정 오류 [{}]: {}", e.getCode(), e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(new ErrorResponse("PAYMENT_CONFIG_ERROR", "결제 시스템 설정 오류가 발생했습니다."));
        }
        log.warn("Toss 결제 오류 [{}]: {}", e.getCode(), e.getMessage());
        return ResponseEntity.status(e.getStatus())
                .body(new ErrorResponse(e.getCode(), e.getMessage()));
    }

    @ExceptionHandler(OutboundRateLimitException.class)
    public ResponseEntity<ErrorResponse> handleOutboundRateLimit(OutboundRateLimitException e) {
        log.warn("아웃바운드 Rate Limit 초과: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header("Retry-After", String.valueOf(e.getRetryAfterSeconds()))
                .body(new ErrorResponse("OUTBOUND_RATE_LIMIT_EXCEEDED", "현재 결제 요청이 너무 많습니다. 잠시 후 다시 시도해주세요."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception e) {
        log.error("예상치 못한 오류 발생", e);
        return ResponseEntity.internalServerError()
                .body(new ErrorResponse("INTERNAL_SERVER_ERROR", "일시적인 오류가 발생했습니다."));
    }
}
