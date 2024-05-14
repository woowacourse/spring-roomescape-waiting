package roomescape.config;

import java.util.Optional;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import roomescape.exception.ExceptionTemplate;
import roomescape.exception.ForbiddenException;
import roomescape.exception.InvalidMemberException;
import roomescape.exception.InvalidReservationException;
import roomescape.exception.UnauthorizedException;

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
        String message = Optional.ofNullable(exception.getBindingResult().getFieldError())
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .orElse("요청 형식이 잘못되었습니다.");
        return ResponseEntity.badRequest().body(new ExceptionTemplate(message));
    }
}
