package roomescape.common.exception;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(HttpMessageNotReadableException.class)
    private ResponseEntity<ErrorResponse> handleJsonParseError(
            final HttpMessageNotReadableException e,
            final HttpServletRequest request
    ) {
        System.out.println(e);
        final String path = extractPath(request);
        final ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                BAD_REQUEST.value(),
                BAD_REQUEST.getReasonPhrase(),
                "요청 형식이 올바르지 않습니다.",
                path
        );
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    private ResponseEntity<ErrorResponse> handleMethodArgumentException(
            final MethodArgumentNotValidException e,
            final HttpServletRequest request
    ) {
        System.out.println(e);
        final String path = extractPath(request);
        final ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                BAD_REQUEST.value(),
                BAD_REQUEST.getReasonPhrase(),
                e.getBindingResult().getFieldError().getDefaultMessage(),
                path
        );
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    private ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            final IllegalArgumentException e,
            final HttpServletRequest request
    ) {
        System.out.println(e);
        final String path = extractPath(request);
        final ErrorResponse errorResponse = makeGeneralErrorResponse(e, BAD_REQUEST, path);
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(MissingLoginException.class)
    private ResponseEntity<ErrorResponse> handleMissingLoginException(
            final MissingLoginException e,
            final HttpServletRequest request
    ) {
        System.out.println(e);
        final String path = extractPath(request);
        final ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                UNAUTHORIZED.value(),
                UNAUTHORIZED.getReasonPhrase(),
                "로그인이 필요합니다.",
                path
        );
        return ResponseEntity.status(UNAUTHORIZED).body(errorResponse);
    }

    @ExceptionHandler(NoPermissionException.class)
    private ResponseEntity<ErrorResponse> handleNoPermissionException(
            final NoPermissionException e,
            final HttpServletRequest request
    ) {
        System.out.println(e);
        final String path = extractPath(request);
        final ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                FORBIDDEN.value(),
                FORBIDDEN.getReasonPhrase(),
                "접근 권한이 없습니다.",
                path
        );
        return ResponseEntity.status(FORBIDDEN).body(errorResponse);
    }

    @ExceptionHandler(ExpiredJwtException.class)
    private ResponseEntity<ErrorResponse> handleExpiredJwtException(
            final ExpiredJwtException e,
            final HttpServletRequest request
    ) {
        System.out.println(e);
        final String path = extractPath(request);
        final ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                UNAUTHORIZED.value(),
                UNAUTHORIZED.getReasonPhrase(),
                "인증 토큰이 만료되었습니다. 다시 로그인 해주세요.",
                path
        );
        return ResponseEntity.status(UNAUTHORIZED).body(errorResponse);
    }

    @ExceptionHandler(JwtException.class)
    private ResponseEntity<ErrorResponse> handleJwtException(
            final JwtException e,
            final HttpServletRequest request
    ) {
        System.out.println(e);
        final String path = extractPath(request);
        final ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                BAD_REQUEST.value(),
                BAD_REQUEST.getReasonPhrase(),
                "인증 토큰이 유효하지 않습니다.",
                path
        );
        return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    private ResponseEntity<ErrorResponse> handleException(
            final Exception e,
            final HttpServletRequest request
    ) {
        System.out.println(e);
        final String path = extractPath(request);
        final ErrorResponse errorResponse = makeGeneralErrorResponse(e, INTERNAL_SERVER_ERROR, path);
        return ResponseEntity.badRequest().body(errorResponse);
    }

    private ErrorResponse makeGeneralErrorResponse(
            final Exception e,
            final HttpStatus status,
            final String path
    ) {
        return new ErrorResponse(LocalDateTime.now(), status.value(), status.getReasonPhrase(), e.getMessage(), path);
    }

    private String extractPath(HttpServletRequest request) {
        String path = request.getServletPath();
        if (request.getQueryString() != null) {
            path += "?" + request.getQueryString();
        }
        return path;
    }
}
