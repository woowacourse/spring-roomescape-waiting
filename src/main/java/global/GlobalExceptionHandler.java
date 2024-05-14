package global;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import roomescape.exception.RoomescapeErrorCode;

public class GlobalExceptionHandler extends AbstractExceptionHandler {

    @ExceptionHandler
    public ResponseEntity<?> exceptionHandler(RuntimeException exception) {
        logError(exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(RoomescapeErrorCode.INTERNAL_SERVER_ERROR.getMessage());
    }

    @ExceptionHandler
    public ResponseEntity<?> exceptionHandler(Exception exception) {
        logError(exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(RoomescapeErrorCode.INTERNAL_SERVER_ERROR.getMessage());
    }
}
