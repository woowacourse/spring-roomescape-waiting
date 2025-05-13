package roomescape.member.controller.exception;

import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import roomescape.member.resolver.UnauthenticatedException;

@RestControllerAdvice
@Order(value = 1)
public class UnauthenticatedExceptionHandler {

    @ExceptionHandler(UnauthenticatedException.class)
    public ResponseEntity<Void> handelUnauthenticatedException() {
        return ResponseEntity.noContent().build();
    }
}
