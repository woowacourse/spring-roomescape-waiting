package roomescape.common.exception.handler;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import roomescape.auth.exception.ForbiddenException;
import roomescape.auth.exception.UnauthorizedException;
import roomescape.common.exception.handler.dto.ExceptionResponse;

@RestControllerAdvice
public class AuthExceptionHandler {

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ExceptionResponse> handleUnauthorize(final UnauthorizedException exception, final HttpServletRequest request) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(
                HttpStatus.UNAUTHORIZED.value(), "[ERROR] " + exception.getMessage(), request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED.value()).body(exceptionResponse);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ExceptionResponse> handleForbidden(final ForbiddenException exception, final HttpServletRequest request) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(
                HttpStatus.FORBIDDEN.value(), "[ERROR] " + exception.getMessage(), request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN.value()).body(exceptionResponse);
    }
}
