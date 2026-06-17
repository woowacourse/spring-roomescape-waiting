package roomescape.global.exception;

import java.net.URI;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import roomescape.domain.exception.BusinessRuleViolationException;
import roomescape.domain.exception.ConflictException;
import roomescape.domain.exception.ForbiddenException;
import roomescape.domain.exception.NotFoundException;

@RestControllerAdvice
public final class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static final String DETAIL_VALIDATION_ERROR = "요청 본문의 일부 필드가 유효하지 않습니다.";
    private static final String DETAIL_INTERNAL_ERROR = "요청을 처리하는 중 알 수 없는 오류가 발생했습니다.";
    private static final String DETAIL_DATA_INTEGRITY_VIOLATION = "요청한 데이터가 이미 존재하거나 사용 중입니다.";
    private static final String DUPLICATE_RESERVATION = "해당 날짜와 시간, 테마에 이미 예약이 존재합니다.";
    private static final String DUPLICATE_WAITING = "이미 대기를 신청한 예약입니다.";
    private static final String ERRORS_PROPERTY = "errors";
    private static final String POINTER_PREFIX = "/";

    @ExceptionHandler(NotFoundException.class)
    public ProblemDetail handleNotFound(NotFoundException ex, WebRequest request) {
        return buildProblem(HttpStatus.NOT_FOUND, ProblemType.NOT_FOUND, ex, request);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ProblemDetail handleForbidden(ForbiddenException ex, WebRequest request) {
        return buildProblem(HttpStatus.FORBIDDEN, ProblemType.FORBIDDEN, ex, request);
    }

    @ExceptionHandler(ConflictException.class)
    public ProblemDetail handleConflict(ConflictException ex, WebRequest request) {
        return buildProblem(HttpStatus.CONFLICT, ProblemType.CONFLICT, ex, request);
    }

    @ExceptionHandler(BusinessRuleViolationException.class)
    public ProblemDetail handleBusinessRuleViolation(BusinessRuleViolationException ex, WebRequest request) {
        return buildProblem(HttpStatus.UNPROCESSABLE_ENTITY, ProblemType.BUSINESS_RULE_VIOLATION, ex, request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrityViolation(DataIntegrityViolationException ex, WebRequest request) {
        return buildProblem(
                HttpStatus.CONFLICT,
                ProblemType.CONFLICT,
                new ConflictException(toConflictMessage(ex), ex),
                request
        );
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex, WebRequest request) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, DETAIL_INTERNAL_ERROR);
        applyType(problem, ProblemType.INTERNAL_ERROR, request);
        logException(ex, status, request);
        return problem;
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, DETAIL_VALIDATION_ERROR);
        List<FieldErrorDetail> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldErrorDetail::from)
                .toList();
        problem.setProperty(ERRORS_PROPERTY, errors);
        return handleExceptionInternal(ex, problem, headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception ex,
            Object body,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        ResponseEntity<Object> response = super.handleExceptionInternal(ex, body, headers, status, request);
        if (response != null && response.getBody() instanceof ProblemDetail problem) {
            applyType(problem, mappingFor(ex, status), request);
        }
        logException(ex, status, request);
        return response;
    }

    private ProblemDetail buildProblem(
            HttpStatus status,
            ProblemType type,
            Exception ex,
            WebRequest request
    ) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, ex.getMessage());
        applyType(problem, type, request);
        logException(ex, status, request);
        return problem;
    }

    private String toConflictMessage(DataIntegrityViolationException ex) {
        String causeMessage = ex.getMostSpecificCause().getMessage();
        if (causeMessage == null) {
            return DETAIL_DATA_INTEGRITY_VIOLATION;
        }
        String normalized = causeMessage.toUpperCase();
        if (normalized.contains("UNIQUE_RESERVATION_WAITING_DATE_TIME_THEME_NAME")) {
            return DUPLICATE_WAITING;
        }
        if (normalized.contains("UNIQUE_RESERVATION_DATE_TIME_THEME")) {
            return DUPLICATE_RESERVATION;
        }
        return DETAIL_DATA_INTEGRITY_VIOLATION;
    }

    private void applyType(ProblemDetail problem, ProblemType type, WebRequest request) {
        problem.setType(type.uri());
        problem.setTitle(type.title());
        problem.setInstance(URI.create(extractUri(request)));
    }

    private ProblemType mappingFor(Exception ex, HttpStatusCode status) {
        if (ex instanceof MethodArgumentNotValidException) {
            return ProblemType.VALIDATION_ERROR;
        }
        return switch (status.value()) {
            case 400 -> ProblemType.BAD_REQUEST;
            case 404 -> ProblemType.NO_RESOURCE;
            case 405 -> ProblemType.METHOD_NOT_SUPPORTED;
            case 406 -> ProblemType.NOT_ACCEPTABLE;
            case 415 -> ProblemType.MEDIA_TYPE_NOT_SUPPORTED;
            default -> ProblemType.INTERNAL_ERROR;
        };
    }

    private void logException(Exception ex, HttpStatusCode status, WebRequest request) {
        String method = extractMethod(request);
        String uri = extractUri(request);
        int code = status.value();
        if (status.is5xxServerError()) {
            log.error("{} {} → {}", method, uri, code, ex);
            return;
        }
        log.warn("{} {} → {} {}", method, uri, code, ex.getMessage());
    }

    private String extractUri(WebRequest request) {
        if (request instanceof ServletWebRequest servletRequest) {
            return servletRequest.getRequest().getRequestURI();
        }
        return "";
    }

    private String extractMethod(WebRequest request) {
        if (request instanceof ServletWebRequest servletRequest) {
            return servletRequest.getRequest().getMethod();
        }
        return "";
    }

    public record FieldErrorDetail(
            String pointer,
            String reason
    ) {
        public static FieldErrorDetail from(FieldError fieldError) {
            return new FieldErrorDetail(POINTER_PREFIX + fieldError.getField(), fieldError.getDefaultMessage());
        }
    }
}
