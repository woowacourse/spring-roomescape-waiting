package roomescape.common.exception.handler;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import roomescape.auth.exception.ForbiddenException;
import roomescape.auth.exception.UnauthorizedException;
import roomescape.common.exception.BusinessException;
import roomescape.common.exception.handler.dto.ExceptionResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ExceptionResponse> handleNullPointer(final BusinessException exception,
                                                               final HttpServletRequest request) {
        ExceptionResponse exceptionResponse = createErrorResponse(
            HttpStatus.BAD_REQUEST, exception.getMessage(), request.getRequestURI()
        );

        return ResponseEntity.badRequest().body(exceptionResponse);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ExceptionResponse> handleUnauthorize(final UnauthorizedException exception, final HttpServletRequest request) {
        ExceptionResponse exceptionResponse = createErrorResponse(
            HttpStatus.UNAUTHORIZED, exception.getMessage(), request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED.value()).body(exceptionResponse);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ExceptionResponse> handleForbidden(final ForbiddenException exception, final HttpServletRequest request) {
        ExceptionResponse exceptionResponse = createErrorResponse(
            HttpStatus.FORBIDDEN, exception.getMessage(), request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN.value()).body(exceptionResponse);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ExceptionResponse> handleIllegalArgument(final IllegalArgumentException exception,
                                                                   final HttpServletRequest request) {
        ExceptionResponse exceptionResponse = createErrorResponse(
            HttpStatus.BAD_REQUEST, exception.getMessage(), request.getRequestURI()
        );

        return ResponseEntity.badRequest().body(exceptionResponse);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ExceptionResponse> handleHttpMessageNotReadable(
        final HttpMessageNotReadableException exception, final HttpServletRequest request
    ) {
        Throwable rootCause = exception.getRootCause();
        if (rootCause instanceof IllegalArgumentException) {
            ExceptionResponse exceptionResponse = createErrorResponse(
                HttpStatus.BAD_REQUEST, rootCause.getMessage(), request.getRequestURI()
            );

            return ResponseEntity.badRequest().body(exceptionResponse);
        }

        ExceptionResponse exceptionResponse = createErrorResponse(
            HttpStatus.BAD_REQUEST, "요청 입력이 잘못되었습니다.", request.getRequestURI()
        );

        return ResponseEntity.badRequest().body(exceptionResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponse> handleException(final Exception exception,
                                                             final HttpServletRequest request) {
        ExceptionResponse exceptionResponse = createErrorResponse(
            HttpStatus.BAD_REQUEST, exception.getMessage(), request.getRequestURI()
        );

        return ResponseEntity.badRequest().body(exceptionResponse);
    }

    private ExceptionResponse createErrorResponse(final HttpStatus httpStatus, final String msg, final String uri) {
        return new ExceptionResponse(
            httpStatus.value(), "[ERROR] " + msg, uri
        );
    }
}
