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
    public ResponseEntity<Object> handleBadRequestException(final Exception ex, final WebRequest request) {
        final ProblemDetail body = super.createProblemDetail(ex, HttpStatus.BAD_REQUEST, ex.getMessage(), null,
                null, request);
        return super.handleExceptionInternal(ex, body, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Object> handleNotFoundException(final Exception ex, final WebRequest request) {
        final ProblemDetail body = super.createProblemDetail(ex, HttpStatus.NOT_FOUND, ex.getMessage(), null,
                null, request);
        return super.handleExceptionInternal(ex, body, new HttpHeaders(), HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Object> handleUnauthorizedException(final Exception ex, final WebRequest request) {
        final ProblemDetail body = super.createProblemDetail(ex, HttpStatus.UNAUTHORIZED, ex.getMessage(), null,
                null, request);
        return super.handleExceptionInternal(ex, body, new HttpHeaders(), HttpStatus.UNAUTHORIZED, request);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<Object> handleForbiddenException(final Exception ex, final WebRequest request) {
        final ProblemDetail body = super.createProblemDetail(ex, HttpStatus.FORBIDDEN, ex.getMessage(), null,
                null, request);
        return super.handleExceptionInternal(ex, body, new HttpHeaders(), HttpStatus.FORBIDDEN, request);
    }

    @ExceptionHandler(TokenCreationException.class)
    public ResponseEntity<Object> handleTokenCreationException(final Exception ex, final WebRequest request) {
        final ProblemDetail body = super.createProblemDetail(ex, HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(),
                null,
                null, request);
        return super.handleExceptionInternal(ex, body, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleInternalServerException(final Exception ex, final WebRequest request) {
        log.error(ex.getMessage(), ex);
        final ProblemDetail body = super.createProblemDetail(ex, HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.",
                null,
                null, request);
        return super.handleExceptionInternal(ex, body, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }
}
