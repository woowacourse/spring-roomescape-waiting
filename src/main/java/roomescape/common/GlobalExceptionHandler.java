package roomescape.common;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import roomescape.common.exception.DomainException;
import roomescape.common.exception.handler.FormatHandler;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final List<FormatHandler> formatHandlers;

    public GlobalExceptionHandler(List<FormatHandler> formatHandlers) {
        this.formatHandlers = formatHandlers;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> handleException(HttpMessageNotReadableException e, HttpServletRequest request) {
        Throwable cause = e.getCause();
        if (cause instanceof InvalidFormatException formatException) {
            String message = formatHandlers.stream()
                    .filter(h -> h.isSupport(formatException))
                    .findAny()
                    .map(h -> h.handle(formatException))
                    .orElse("잘못된 형식입니다.");
            return ResponseEntity.badRequest().body(problem(HttpStatus.BAD_REQUEST, "잘못된 요청 형식", message, request));
        }
        return ResponseEntity.badRequest().body(problem(HttpStatus.BAD_REQUEST, "잘못된 요청 형식", "읽을 수 없는 요청입니다.", request));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidation(MethodArgumentNotValidException e, HttpServletRequest request) {
        List<Map<String, String>> invalidParams = e.getBindingResult().getFieldErrors().stream()
                .map(error -> Map.of("field", error.getField(), "reason", error.getDefaultMessage()))
                .toList();
        ProblemDetail problem = problem(HttpStatus.BAD_REQUEST, "입력값 검증 실패", "하나 이상의 필드가 유효하지 않습니다.", request);
        problem.setProperty("invalid-params", invalidParams);
        return ResponseEntity.badRequest().body(problem);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ProblemDetail> handleTypeMismatch(MethodArgumentTypeMismatchException e, HttpServletRequest request) {
        return ResponseEntity.badRequest()
                .body(problem(HttpStatus.BAD_REQUEST, "타입 불일치", "잘못된 형식의 값입니다: " + e.getName(), request));
    }

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ProblemDetail> handleDomainException(DomainException e, HttpServletRequest request) {
        HttpStatus status = ExceptionHttpStatusMapper.resolve(e);
        return ResponseEntity.status(status)
                .body(problem(status, status.getReasonPhrase(), e.getMessage(), request));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ProblemDetail> handleDataIntegrityViolation(DataIntegrityViolationException e, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(problem(HttpStatus.CONFLICT, "데이터 충돌", "이미 존재하는 데이터와 충돌이 발생했습니다.", request));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ProblemDetail> handleUnexpected(RuntimeException e, HttpServletRequest request) {
        log.error("처리되지 않은 예외가 발생했습니다.", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(problem(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류", "예상치 못한 오류가 발생했습니다.", request));
    }

    private ProblemDetail problem(HttpStatus status, String title, String detail, HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        problem.setInstance(URI.create(request.getRequestURI()));
        return problem;
    }
}
