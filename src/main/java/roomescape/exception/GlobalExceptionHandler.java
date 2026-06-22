package roomescape.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.dao.DuplicateKeyException;
import roomescape.client.OutboundRateLimitException;
import roomescape.domain.exception.DomainValidationException;
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

    @ExceptionHandler(ResourceConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleConflict(ResourceConflictException e) {
        return new ErrorResponse(ErrorCode.CONFLICT.name(), e.getMessage());
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

    @ExceptionHandler(OutboundRateLimitException.class)
    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
    public ErrorResponse handleOutboundRateLimit(OutboundRateLimitException e) {
        log.warn("나가는 호출이 자체 한도를 넘어 외부로 보내지 않고 거부했습니다: {}", e.getMessage());
        return new ErrorResponse(ErrorCode.TOO_MANY_REQUESTS.name(), "요청이 많아 잠시 후 다시 시도해주세요.");
    }

    @ExceptionHandler(DuplicateKeyException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleDuplicateKey(DuplicateKeyException e) {
        log.warn("중복 키 예외 발생 (동시성/중복 생성): {}", e.getMessage());
        return new ErrorResponse(ErrorCode.INVALID_INPUT.name(), "이미 사용 중이거나 중복된 데이터입니다.");
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
