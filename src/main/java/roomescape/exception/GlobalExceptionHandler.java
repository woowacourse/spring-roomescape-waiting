package roomescape.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler({ReservationException.class, IllegalArgumentException.class})
    public ResponseEntity<Object> handleBadRequestException(final Exception e, final WebRequest request) {
        final ProblemDetail body = super.createProblemDetail(e, HttpStatus.BAD_REQUEST, e.getMessage(), null,
                null, request);
        return super.handleExceptionInternal(e, body, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Object> handleNotFoundException(final Exception e, final WebRequest request) {
        final ProblemDetail body = super.createProblemDetail(e, HttpStatus.NOT_FOUND, e.getMessage(), null,
                null, request);
        return super.handleExceptionInternal(e, body, new HttpHeaders(), HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Object> handleUnauthorizedException(final Exception e, final WebRequest request) {
        final ProblemDetail body = super.createProblemDetail(e, HttpStatus.UNAUTHORIZED, e.getMessage(), null,
                null, request);
        log.error(e.getMessage(), e);
        return super.handleExceptionInternal(e, body, new HttpHeaders(), HttpStatus.UNAUTHORIZED, request);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<Object> handleForbiddenException(final Exception e, final WebRequest request) {
        final ProblemDetail body = super.createProblemDetail(e, HttpStatus.FORBIDDEN, e.getMessage(), null,
                null, request);
        return super.handleExceptionInternal(e, body, new HttpHeaders(), HttpStatus.FORBIDDEN, request);
    }

    @ExceptionHandler(TokenCreationException.class)
    public ResponseEntity<Object> handleTokenCreationException(final Exception e, final WebRequest request) {
        final ProblemDetail body = super.createProblemDetail(e, HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(),
                null, null, request);
        log.error("JWT 토큰 생성 실패: " + e.getMessage(), e);
        return super.handleExceptionInternal(e, body, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleInternalServerException(final Exception e, final WebRequest request) {
        log.error(e.getMessage(), e);
        final ProblemDetail body = super.createProblemDetail(e, HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.",
                null, null, request);
        return super.handleExceptionInternal(e, body, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }
}
