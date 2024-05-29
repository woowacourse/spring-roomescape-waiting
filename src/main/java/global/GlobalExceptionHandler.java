package global;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;

public class GlobalExceptionHandler extends AbstractExceptionHandler {
    @ExceptionHandler
    public ResponseEntity<String> exceptionHandler(RuntimeException exception) {
        logError(exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("서버 내부에서 에러가 발생했습니다.");
    }

    @ExceptionHandler
    public ResponseEntity<String> exceptionHandler(Exception exception) {
        logError(exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("서버 내부에서 에러가 발생했습니다.");
    }
}
