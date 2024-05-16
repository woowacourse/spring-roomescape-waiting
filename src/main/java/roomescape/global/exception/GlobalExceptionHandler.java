package roomescape.global.exception;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

import java.util.NoSuchElementException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(value = AuthException.class)
    public ResponseEntity<ErrorResponse> handle(final AuthException exception) {
        logger.error(exception.getMessage(), exception);
        return ResponseEntity.status(exception.getErrorResponse().getStatusCode())
                .body(exception.getErrorResponse());
    }

    @ExceptionHandler(value = NoSuchElementException.class)
    public ResponseEntity<ErrorResponse> handle(final NoSuchElementException exception) {
        logger.error(exception.getMessage(), exception);
        return ResponseEntity.badRequest()
                .body(new ErrorResponse(BAD_REQUEST, "[ERROR] 요청된 자원이 존재하지 않습니다."));
    }

    @ExceptionHandler({
            HttpRequestMethodNotSupportedException.class,
            HttpMessageNotReadableException.class,
            MethodArgumentNotValidException.class
    })
    public ResponseEntity<ErrorResponse> handle(final RuntimeException exception) {
        logger.error(exception.getMessage(), exception);
        return ResponseEntity.badRequest()
                .body(new ErrorResponse(BAD_REQUEST, "[ERROR] 올바른 형식의 필드를 입력해주세요."));
    }

    @ExceptionHandler(value = IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handle(final IllegalArgumentException exception) {
        return ResponseEntity.badRequest()
                .body(new ErrorResponse(BAD_REQUEST, exception.getMessage()));
    }

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<ErrorResponse> handle(final Exception exception) {
        logger.error(exception.getMessage(), exception);
        return ResponseEntity.internalServerError()
                .body(new ErrorResponse(INTERNAL_SERVER_ERROR, "[ERROR] 서버 내부 오류입니다."));
    }
}
