package roomescape.config;

import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import roomescape.exception.*;

import java.time.format.DateTimeParseException;
import java.util.Optional;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(value = {InvalidReservationException.class, InvalidMemberException.class})
    public ResponseEntity<ExceptionTemplate> handleInvalidReservationException(Exception exception) {
        return ResponseEntity.badRequest().body(new ExceptionTemplate(exception.getMessage()));
    }

    @ExceptionHandler(value = {UnauthorizedException.class})
    public ResponseEntity<ExceptionTemplate> handleUnauthorizedException(UnauthorizedException exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ExceptionTemplate(exception.getMessage()));
    }

    @ExceptionHandler(value = {ForbiddenException.class})
    public ResponseEntity<ExceptionTemplate> handlerForbiddenException(ForbiddenException exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ExceptionTemplate(exception.getMessage()));
    }

    @ExceptionHandler(value = {MethodArgumentNotValidException.class})
    public ResponseEntity<ExceptionTemplate> handleValidationException(MethodArgumentNotValidException exception) {
        String message = Optional.ofNullable(exception.getBindingResult().getFieldError())
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .orElse("요청 형식이 잘못되었습니다.");
        return ResponseEntity.badRequest().body(new ExceptionTemplate(message));
    }

    @ExceptionHandler(value = {HttpMessageNotReadableException.class})
    public ResponseEntity<ExceptionTemplate> handleValidationException(HttpMessageNotReadableException exception) {
        if (exception.getRootCause() instanceof DateTimeParseException) {
            return ResponseEntity.badRequest().body(new ExceptionTemplate("시간/날짜 형식이 잘못되었습니다."));
        }
        return ResponseEntity.badRequest().body(new ExceptionTemplate("잘못된 요청입니다."));
    }
}
