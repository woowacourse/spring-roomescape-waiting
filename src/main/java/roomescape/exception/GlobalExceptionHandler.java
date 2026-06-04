package roomescape.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<GlobalErrorResponse> handleIllegalArgumentException(IllegalArgumentException e) {
        return ResponseEntity
                .badRequest()
                .body(new GlobalErrorResponse(e.getMessage()));
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<GlobalErrorResponse> handleDuplicateKeyException(DuplicateKeyException e) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new GlobalErrorResponse("중복된 요청입니다. 다시 시도 해주세요"));
    }

    @ExceptionHandler(IdNotFoundException.class)
    public ResponseEntity<GlobalErrorResponse> handleIdNotFoundException(IdNotFoundException e) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new GlobalErrorResponse(e.getMessage()));
    }

    @ExceptionHandler(NameNotFoundException.class)
    public ResponseEntity<GlobalErrorResponse> handleNameNotFoundException(NameNotFoundException e) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new GlobalErrorResponse(e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<GlobalErrorResponse> handleUnhandledException(Exception e) {
        log.error("처리되지 않은 예외 발생", e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new GlobalErrorResponse("서버 내부에서 예상치 못한 오류가 발생했습니다. 다시 시도 해주세요."));
    }
}
