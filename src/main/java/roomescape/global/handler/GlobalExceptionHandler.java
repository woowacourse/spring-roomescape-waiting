package roomescape.global.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import roomescape.global.handler.exception.AuthenticationException;
import roomescape.global.handler.exception.NotFoundException;
import roomescape.global.handler.exception.ValidationException;

@ControllerAdvice
public class GlobalExceptionHandler {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<String> handleDateTimeParseException(HttpMessageNotReadableException exception) {
        logger.error(exception.getMessage(), exception);
        return ResponseEntity.badRequest()
                .body("request body 형식이 잘못되었습니다.");
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<String> handleValidationException(ValidationException exception) {
        logger.error(exception.getMessage(), exception);
        return ResponseEntity.badRequest()
                .body(exception.getMessage());
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<String> handleNotFoundException(NotFoundException exception) {
        logger.error(exception.getMessage(), exception);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(exception.getMessage());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<String> handleAuthenticationException(AuthenticationException exception) {
        logger.error(exception.getMessage(), exception);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(exception.getMessage());
    }

    @ExceptionHandler(value = IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(final IllegalArgumentException exception) {
        logger.error(exception.getMessage());
        return ResponseEntity.badRequest()
                .body(exception.getMessage());
    }

    @ExceptionHandler(NumberFormatException.class)
    public ResponseEntity<String> handleNumberFormatException(NumberFormatException exception) {
        logger.error(exception.getMessage(), exception);
        return ResponseEntity.badRequest()
                .body("잘못된 숫자 형식입니다");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        logger.error(exception.getMessage(), exception);
        String errorMessage = exception.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        return ResponseEntity.badRequest()
                .body(errorMessage);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception exception) {
        logger.error(exception.getMessage(), exception);
        System.out.println();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("서버 장애 오류입니다.");
    }
}
