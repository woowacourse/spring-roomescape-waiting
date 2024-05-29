package roomescape.exception;

import global.AbstractExceptionHandler;
import java.time.format.DateTimeParseException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RoomescapeExceptionHandler extends AbstractExceptionHandler {
    @ExceptionHandler
    public ResponseEntity<String> roomescapeExceptionHandler(RoomescapeException exception) {
        logError(exception);
        return ResponseEntity.status(exception.getHttpStatus())
                .body(exception.getMessage());
    }

    @ExceptionHandler
    public ResponseEntity<String> dateTimeParseExceptionHandler(DateTimeParseException exception) {
        logError(exception);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("올바르지 않은 시간/날짜 형식입니다.");
    }
}
