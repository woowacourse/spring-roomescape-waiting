package roomescape.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler({ReservationException.class, IllegalArgumentException.class})
    public ResponseEntity<Object> handleBadRequestException(final RuntimeException e, final WebRequest request) {
        return buildResponseEntity(e, HttpStatus.BAD_REQUEST, e.getMessage(), request);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Object> handleNotFoundException(final NotFoundException e, final WebRequest request) {
        return buildResponseEntity(e, HttpStatus.NOT_FOUND, e.getMessage(), request);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Object> handleUnauthorizedException(final UnauthorizedException e,
                                                              final WebRequest request) {
        log.error(e.getMessage(), e);
        return buildResponseEntity(e, HttpStatus.UNAUTHORIZED, e.getMessage(), request);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<Object> handleForbiddenException(final ForbiddenException e, final WebRequest request) {
        return buildResponseEntity(e, HttpStatus.FORBIDDEN, e.getMessage(), request);
    }

    @ExceptionHandler(TokenCreationException.class)
    public ResponseEntity<Object> handleTokenCreationException(final TokenCreationException e,
                                                               final WebRequest request) {
        log.error(e.getMessage(), e);
        return buildResponseEntity(e, HttpStatus.INTERNAL_SERVER_ERROR, "토큰 생성 중 오류가 발생했습니다.", request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleInternalServerException(final Exception e, final WebRequest request) {
        log.error(e.getMessage(), e);
        return buildResponseEntity(e, HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.", request);
    }

    private ResponseEntity<Object> buildResponseEntity(
            final Exception e,
            final HttpStatus status,
            final String message,
            final WebRequest request
    ) {
        final String path = ((ServletWebRequest) request).getRequest().getRequestURI();
        final ProblemDetail body = super.createProblemDetail(e, status, message, path, null, request);
        return super.handleExceptionInternal(e, body, new HttpHeaders(), status, request);
    }
}
