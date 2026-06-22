package roomescape.global.error.handler;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import roomescape.feature.payment.PaymentConnectionException;
import roomescape.feature.payment.PaymentException;
import roomescape.feature.payment.PaymentFailureType;
import roomescape.feature.payment.PaymentRateLimitedException;
import roomescape.feature.payment.PaymentTimeoutException;
import roomescape.global.error.dto.ErrorResponseDto;
import roomescape.global.ratelimit.RateLimitException;
import roomescape.global.error.dto.ParameterErrorResponseDto;
import roomescape.global.error.dto.ParameterErrorResponsesDto;
import roomescape.global.error.exception.GeneralException;
import roomescape.global.error.exception.GeneralParametersException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponseDto> handleHttpRequestMethodNotSupportedException(Exception e) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
            .body(new ErrorResponseDto("METHOD_NOT_ALLOWED", "지원하지 않는 HTTP 메서드입니다."));
    }

    @ExceptionHandler({
        NoHandlerFoundException.class,
        NoResourceFoundException.class
    })
    public ResponseEntity<ErrorResponseDto> handleNotFoundException(Exception e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponseDto("NOT_FOUND", "존재하지 않는 API입니다."));
    }

    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            HandlerMethodValidationException.class,
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class
    })
    public ResponseEntity<ErrorResponseDto> handleIllegalRequestForm(
        Exception e
    ) {
        return ResponseEntity.badRequest().body(new ErrorResponseDto("INVALID_REQUEST", "요청 형식이 올바르지 않습니다."));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ParameterErrorResponsesDto> handleConstraintViolationException(
        ConstraintViolationException e
    ) {
        List<ParameterErrorResponseDto> parameterErrors = e.getConstraintViolations()
            .stream()
            .map(violation -> new ParameterErrorResponseDto(
                getParameterName(violation),
                violation.getMessage()
            ))
            .toList();

        return ResponseEntity.badRequest()
            .body(new ParameterErrorResponsesDto("요청 값이 올바르지 않습니다.", parameterErrors));
    }

    private String getParameterName(ConstraintViolation<?> violation) {
        String propertyPath = violation.getPropertyPath().toString();
        int lastSeparatorIndex = propertyPath.lastIndexOf('.');

        if (lastSeparatorIndex == -1) {
            return propertyPath;
        }
        return propertyPath.substring(lastSeparatorIndex + 1);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ParameterErrorResponsesDto> handleValidationException(
        MethodArgumentNotValidException exception
    ) {
        List<ParameterErrorResponseDto> parameterErrors = exception.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(error -> new ParameterErrorResponseDto(
                error.getField(),
                error.getDefaultMessage()
            ))
            .toList();

        return ResponseEntity.badRequest()
            .body(new ParameterErrorResponsesDto("요청 값이 올바르지 않습니다.", parameterErrors));
    }

    @ExceptionHandler(GeneralException.class)
    public ResponseEntity<ErrorResponseDto> handleReservationException(GeneralException e) {
        return ResponseEntity.status(e.getStatus()).body(new ErrorResponseDto(e.getCode(), e.getMessage()));
    }

    @ExceptionHandler(GeneralParametersException.class)
    public ResponseEntity<ParameterErrorResponsesDto> handleReservationNotFoundException(GeneralParametersException e) {
        return ResponseEntity.status(e.getStatus())
            .body(new ParameterErrorResponsesDto(e.getMessage(), e.getParameterErrors()));
    }

    @ExceptionHandler(PaymentException.class)
    public ResponseEntity<ErrorResponseDto> handlePaymentException(PaymentException e) {
        HttpStatus status = resolvePaymentStatus(e.getFailureType());
        // message 는 클라이언트 개발자 디버깅용(토스 원본 code + message). 사용자 노출 UX 는 클라이언트가 code 로 분기한다.
        String debugMessage = e.getCode() + " - " + e.getMessage();

        return ResponseEntity.status(status)
            .body(new ErrorResponseDto(e.getFailureType().name(), debugMessage));
    }

    private HttpStatus resolvePaymentStatus(PaymentFailureType failureType) {
        return switch (failureType) {
            case CARD_DECLINED -> HttpStatus.BAD_REQUEST;
            case RETRYABLE -> HttpStatus.SERVICE_UNAVAILABLE;
            case ALREADY_DONE -> HttpStatus.OK;
            case CLIENT_FAULT, UNKNOWN -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }

    @ExceptionHandler(PaymentConnectionException.class)
    public ResponseEntity<ErrorResponseDto> handlePaymentConnectionException(PaymentConnectionException e) {
        // 연결 실패: 요청이 토스에 전송된 적 없어 '미청구'가 확정 → 재시도 안내가 가능한 결제 실패.
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(new ErrorResponseDto("PAYMENT_GATEWAY_UNREACHABLE", "결제 서버에 연결할 수 없습니다. 잠시 후 다시 시도해주세요."));
    }

    @ExceptionHandler(PaymentTimeoutException.class)
    public ResponseEntity<ErrorResponseDto> handlePaymentTimeoutException(PaymentTimeoutException e) {
        // 읽기 타임아웃: 결제 결과가 불명 → 실패로 단정하지 않고 '확인 중'으로 안내한다. (중복 결제는 멱등키로 방지)
        return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT)
            .body(new ErrorResponseDto("PAYMENT_RESULT_UNKNOWN", "결제 결과를 확인하고 있습니다. 잠시 후 다시 확인해주세요."));
    }

    @ExceptionHandler(RateLimitException.class)
    public ResponseEntity<ErrorResponseDto> handleRateLimitException(RateLimitException e) {
        // 아웃바운드 자체 한도 소진: 클라이언트는 정상 요청 1건을 보냈고 서버 용량이 일시 부족 → 503 + Retry-After.
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .header(HttpHeaders.RETRY_AFTER, String.valueOf(e.getRetryAfterSeconds()))
            .body(new ErrorResponseDto("RATE_LIMITED", "요청이 많아 일시적으로 처리할 수 없습니다. 잠시 후 다시 시도해주세요."));
    }

    @ExceptionHandler(PaymentRateLimitedException.class)
    public ResponseEntity<ErrorResponseDto> handlePaymentRateLimitedException(PaymentRateLimitedException e) {
        // 토스가 429를 반복 → 다운스트림 스로틀. 클라이언트 잘못이 아니므로 503 + Retry-After.
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .header(HttpHeaders.RETRY_AFTER, String.valueOf(e.getRetryAfterSeconds()))
            .body(new ErrorResponseDto("PAYMENT_RATE_LIMITED", "결제 요청이 일시적으로 지연되고 있습니다. 잠시 후 다시 시도해주세요."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleException(Exception e) {
        e.printStackTrace();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ErrorResponseDto("INTERNAL_ERROR", "예상치 못한 오류가 발생했습니다."));
    }
}
