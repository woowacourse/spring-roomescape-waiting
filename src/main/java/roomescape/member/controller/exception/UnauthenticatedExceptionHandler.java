package roomescape.member.controller.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import roomescape.member.resolver.UnauthenticatedException;

@RestControllerAdvice
public class UnauthenticatedExceptionHandler {

    @ExceptionHandler(UnauthenticatedException.class)
    public ResponseEntity<Void> handelUnauthenticatedException() {
        return ResponseEntity.noContent().build();
    }
}
