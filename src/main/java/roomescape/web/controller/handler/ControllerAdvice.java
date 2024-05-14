package roomescape.web.controller.handler;

import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import roomescape.service.exception.PastReservationException;
import roomescape.service.exception.ReservationExistsException;
import roomescape.web.exception.AuthorizationException;

@RestControllerAdvice
public class ControllerAdvice {

    @ExceptionHandler(value = {
        PastReservationException.class,
        ReservationExistsException.class,
        IllegalArgumentException.class,
        NoSuchElementException.class,
        IllegalStateException.class,
    })
    public ResponseEntity<String> handleServiceException(RuntimeException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(AuthorizationException.class)
    public ResponseEntity<String> handleAuthException(AuthorizationException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        BindingResult result = e.getBindingResult();
        String errMessage = result.getFieldErrors().stream()
            .map(DefaultMessageSourceResolvable::getDefaultMessage)
            .collect(Collectors.joining(","));

        return ResponseEntity.badRequest().body(errMessage);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handlerException(Exception e) {
        return ResponseEntity.internalServerError().body("예기치 못한 에러 발생");
    }
}
