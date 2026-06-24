package roomescape.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.TransientDataAccessException;
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
import roomescape.payment.toss.TossPaymentException;

import java.net.URI;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static final String DETAIL_VALIDATION_ERROR = "요청 본문의 일부 필드가 유효하지 않습니다.";
    private static final String DETAIL_DUPLICATE_KEY = "요청이 현재 상태와 충돌하여 처리할 수 없습니다.";
    private static final String DETAIL_TRANSIENT = "일시적인 문제로 요청을 처리하지 못했습니다. 잠시 후 다시 시도해주세요.";
    private static final String DETAIL_INTERNAL_ERROR = "요청을 처리하는 중 알 수 없는 오류가 발생했습니다.";
    private static final String DETAIL_PAYMENT_FAILED = "결제 처리 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.";
    private static final String ERRORS_PROPERTY = "errors";
    private static final String POINTER_PREFIX = "/";

    @ExceptionHandler(NotFoundException.class)
    public ProblemDetail handleNotFound(NotFoundException ex, WebRequest request) {
        return buildProblem(HttpStatus.NOT_FOUND, ProblemType.NOT_FOUND, Level.INFO, ex, request);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ProblemDetail handleUnauthorized(UnauthorizedException ex, WebRequest request) {
        return buildProblem(HttpStatus.UNAUTHORIZED, ProblemType.UNAUTHORIZED, Level.WARN, ex, request);
    }

    @ExceptionHandler(ConflictException.class)
    public ProblemDetail handleConflict(ConflictException ex, WebRequest request) {
        return buildProblem(HttpStatus.CONFLICT, ProblemType.CONFLICT, Level.INFO, ex, request);
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public ProblemDetail handleDuplicateKey(DuplicateKeyException ex, WebRequest request) {
        return buildProblem(HttpStatus.CONFLICT, ProblemType.CONFLICT, DETAIL_DUPLICATE_KEY, Level.INFO, ex, request);
    }

    @ExceptionHandler(BusinessRuleViolationException.class)
    public ProblemDetail handleBusinessRuleViolation(BusinessRuleViolationException ex, WebRequest request) {
        return buildProblem(HttpStatus.UNPROCESSABLE_ENTITY, ProblemType.BUSINESS_RULE_VIOLATION, Level.INFO, ex, request);
    }

    @ExceptionHandler(PaymentAmountMismatchException.class)
    public ProblemDetail handlePaymentAmountMismatch(PaymentAmountMismatchException ex, WebRequest request) {
        return buildProblem(HttpStatus.UNPROCESSABLE_ENTITY, ProblemType.PAYMENT_AMOUNT_MISMATCH, Level.WARN, ex, request);
    }

    @ExceptionHandler(TossPaymentException.class)
    public ProblemDetail handleTossPayment(TossPaymentException ex, WebRequest request) {
        return buildProblem(HttpStatus.BAD_GATEWAY, ProblemType.PAYMENT_FAILED, DETAIL_PAYMENT_FAILED, Level.WARN, ex, request);
    }

    @ExceptionHandler(TransientDataAccessException.class)
    public ProblemDetail handleTransient(TransientDataAccessException ex, WebRequest request) {
        return buildProblem(HttpStatus.CONFLICT, ProblemType.CONCURRENCY_CONFLICT, DETAIL_TRANSIENT, Level.WARN, ex, request);
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex, WebRequest request) {
        return buildProblem(HttpStatus.INTERNAL_SERVER_ERROR, ProblemType.INTERNAL_ERROR, DETAIL_INTERNAL_ERROR, Level.ERROR, ex, request);
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
        logException(levelFor(status), ex, status, request);
        return response;
    }

    private ProblemDetail buildProblem(
            HttpStatus status,
            ProblemType type,
            Level level,
            Exception ex,
            WebRequest request
    ) {
        return buildProblem(status, type, ex.getMessage(), level, ex, request);
    }

    private ProblemDetail buildProblem(
            HttpStatus status,
            ProblemType type,
            String detail,
            Level level,
            Exception ex,
            WebRequest request
    ) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        applyType(problem, type, request);
        logException(level, ex, status, request);
        return problem;
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

    private Level levelFor(HttpStatusCode status) {
        if (status.is5xxServerError()) {
            return Level.ERROR;
        }
        return Level.INFO;
    }

    private void logException(Level level, Exception ex, HttpStatusCode status, WebRequest request) {
        String method = extractMethod(request);
        String uri = extractUri(request);
        int code = status.value();
        switch (level) {
            case ERROR -> log.error("{} {} → {}", method, uri, code, ex);
            case WARN -> log.warn("{} {} → {} {}", method, uri, code, ex.getMessage());
            default -> log.info("{} {} → {} {}", method, uri, code, ex.getMessage());
        }
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

    public record FieldErrorDetail(String pointer, String reason) {
        public static FieldErrorDetail from(FieldError fieldError) {
            return new FieldErrorDetail(POINTER_PREFIX + fieldError.getField(), fieldError.getDefaultMessage());
        }
    }
}
