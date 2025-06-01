package roomescape.common.exception;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleJsonParseError(
            final HttpMessageNotReadableException e,
            final HttpServletRequest request
    ) {
        log.warn("JSON parse error", e);
        return createErrorResponse(BAD_REQUEST, "요청 형식이 올바르지 않습니다.", request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentException(
            final MethodArgumentNotValidException e,
            final HttpServletRequest request
    ) {
        log.warn("Validation failed", e);
        String message = Optional.ofNullable(e.getBindingResult().getFieldError())
                .map(FieldError::getDefaultMessage)
                .orElse("유효하지 않은 요청입니다.");
        return createErrorResponse(BAD_REQUEST, message, request);
    }

    @ExceptionHandler(RoomescapeException.class)
    public ResponseEntity<ErrorResponse> handleRoomescapeException(
            final RoomescapeException e,
            final HttpServletRequest request
    ) {
        log.warn("Roomescape exception", e);
        return createErrorResponse(BAD_REQUEST, e.getMessage(), request);
    }

    @ExceptionHandler(MissingLoginException.class)
    public ResponseEntity<ErrorResponse> handleMissingLoginException(
            final MissingLoginException e,
            final HttpServletRequest request
    ) {
        log.warn("Missing login", e);
        return createErrorResponse(UNAUTHORIZED, "로그인이 필요합니다.", request);
    }

    @ExceptionHandler(NoPermissionException.class)
    public ResponseEntity<ErrorResponse> handleNoPermissionException(
            final NoPermissionException e,
            final HttpServletRequest request
    ) {
        log.warn("No permission", e);
        return createErrorResponse(FORBIDDEN, "접근 권한이 없습니다.", request);
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<ErrorResponse> handleExpiredJwtException(
            final ExpiredJwtException e,
            final HttpServletRequest request
    ) {
        log.warn("Expired JWT token", e);
        return createErrorResponse(UNAUTHORIZED, "인증 토큰이 만료되었습니다. 다시 로그인 해주세요.", request);
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ErrorResponse> handleJwtException(
            final JwtException e,
            final HttpServletRequest request
    ) {
        log.warn("Invalid JWT token", e);
        return createErrorResponse(BAD_REQUEST, "인증 토큰이 유효하지 않습니다.", request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(
            final Exception e,
            final HttpServletRequest request
    ) {
        log.error("Unexpected error", e);
        return createErrorResponse(INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.", request);
    }

    private ResponseEntity<ErrorResponse> createErrorResponse(
            final HttpStatus status,
            final String message,
            final HttpServletRequest request
    ) {
        final ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                extractPath(request)
        );
        return ResponseEntity.status(status).body(errorResponse);
    }

    private String extractPath(final HttpServletRequest request) {
        String path = request.getServletPath();
        if (request.getQueryString() != null) {
            path += "?" + request.getQueryString();
        }
        return path;
    }
}
