package roomescape.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.web.client.HttpServerErrorException.InternalServerError;

import com.fasterxml.jackson.databind.exc.MismatchedInputException;

@RestControllerAdvice
public class CustomExceptionHandler {
    private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> handleIllegalArgumentException(final IllegalArgumentException e) {
        log.error(e.getMessage());
        return ResponseEntity.status(BAD_REQUEST)
                .body(new ErrorResponse(e.getMessage()));
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> handleNoSuchElementException(final NoSuchElementException e) {
        log.error(e.getMessage());
        return ResponseEntity.status(BAD_REQUEST)
                .body(new ErrorResponse(e.getMessage()));
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> handleAccessUnauthorizedException(final UnauthorizedException e) {
        log.error(e.getMessage());
        return ResponseEntity.status(UNAUTHORIZED)
                .body(new ErrorResponse(e.getMessage()));
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> handleAccessDeniedException(final AccessDeniedException e) {
        log.error(e.getMessage());
        return ResponseEntity.status(FORBIDDEN)
                .body(new ErrorResponse(e.getMessage()));
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> handleInternalServerError(final InternalServerError e) {
        log.error(e.getMessage());
        return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("예기치 못한 서버 에러가 발생하였습니다."));
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> handleException(final Exception e) {
        log.error(e.getMessage());
        return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("예기치 못한 에러가 발생하였습니다."));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String,String>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
            log.warn(error.getDefaultMessage());
        });
        return ResponseEntity.status(BAD_REQUEST)
                .body(errors);
    }
}
