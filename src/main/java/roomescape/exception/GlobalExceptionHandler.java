package roomescape.exception;

import jakarta.validation.ConstraintViolationException;
import java.time.format.DateTimeParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(value = NotFoundException.class)
    public ResponseEntity<String> handleNotFoundException(NotFoundException exception) {
        logger.info("[ERROR] ", exception);
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(value = BadRequestException.class)
    public ResponseEntity<String> handleBadRequestException(BadRequestException exception) {
        logger.warn("[ERROR] ", exception);
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = DuplicatedException.class)
    public ResponseEntity<String> handleDuplicatedException(DuplicatedException exception) {
        logger.info("[ERROR] ", exception);
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = DateTimeParseException.class)
    public ResponseEntity<String> handleDateTimeParseException(DateTimeParseException exception) {
        logger.info("[ERROR] ", exception);
        return new ResponseEntity<>("[ERROR] 유효하지 않은 요청입니다.", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException exception) {
        logger.info("[ERROR] ", exception);
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(value = AuthorizationException.class)
    public ResponseEntity<String> handleAuthorizationException(AuthorizationException exception) {
        logger.info("[ERROR] ", exception);
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        logger.info("[ERROR] ", exception);
        String message = exception.getAllErrors().get(0).getDefaultMessage();
        return new ResponseEntity<>("[REQUEST_ERROR] " + message, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = ConstraintViolationException.class)
    public ResponseEntity<String> handleConstraintViolationException(ConstraintViolationException exception) {
        logger.info("[ERROR] ", exception);
        return new ResponseEntity<>("[REQUEST_ERROR] " + exception.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = HandlerMethodValidationException.class)
    public ResponseEntity<String> handleHandlerMethodValidationException(HandlerMethodValidationException exception) {
        logger.info("[ERROR] ", exception);
        return new ResponseEntity<>("[REQUEST_ERROR] " + exception.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = IllegalStateException.class)
    public ResponseEntity<String> handleIllegalStateException(IllegalStateException exception) {
        logger.info("[ERROR] ", exception);
        return new ResponseEntity<>("[ERROR] " + exception.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception exception) {
        logger.error("[ERROR] ", exception);
        return new ResponseEntity<>("[ERROR] 예상하지 못한 오류로 해당 요청을 처리할 수 없습니다.",
                HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
