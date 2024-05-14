package roomescape.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import roomescape.exception.*;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(value = {InvalidReservationException.class, InvalidMemberException.class})
    protected ResponseEntity<ExceptionTemplate> handleInvalidReservationException(Exception exception) {
        return ResponseEntity.badRequest().body(new ExceptionTemplate(exception.getMessage()));
    }

    @ExceptionHandler(value = {UnauthorizedException.class})
    protected ResponseEntity<ExceptionTemplate> handleUnauthorizedException(Exception exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ExceptionTemplate(exception.getMessage()));
    }

    @ExceptionHandler(value = {ForbiddenException.class})
    protected ResponseEntity<ExceptionTemplate> handlerForbiddenException(Exception exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ExceptionTemplate(exception.getMessage()));
    }

    @ExceptionHandler
    protected ResponseEntity<ExceptionTemplate> handleValidationException(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldError().getDefaultMessage();
        return ResponseEntity.badRequest().body(new ExceptionTemplate(message));
    }
}
