package roomescape.controller.handler;

import org.apache.coyote.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler({IllegalArgumentException.class, NumberFormatException.class, BadRequestException.class})
    public ResponseEntity<String> badRequestException(final Exception ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }
}
