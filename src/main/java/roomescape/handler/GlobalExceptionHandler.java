package roomescape.handler;

import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import roomescape.exception.AuthorizationException;
import roomescape.exception.AuthorizationMismatchException;
import roomescape.exception.BadRequestException;
import roomescape.exception.ConflictException;
import roomescape.exception.IllegalAuthorizationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class.getName());

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception exception) {
        logger.error(exception.getMessage());
        exception.printStackTrace();
        return ResponseEntity.internalServerError()
                .body("문제가 발생했습니다.");
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<String> handleBadRequestException(BadRequestException exception) {
        return ResponseEntity.badRequest()
                .body(exception.getMessage());
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<String> handleConflictException(ConflictException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        return ResponseEntity.badRequest()
                .body(exception.getBindingResult()
                        .getAllErrors()
                        .get(0)
                        .getDefaultMessage());
    }

    @ExceptionHandler(SQLException.class)
    public ResponseEntity<String> handleInternalDataBaseException(SQLException exception) {
        logger.error(exception.getMessage());
        return ResponseEntity.internalServerError()
                .body("데이터 저장 중 문제가 발생하였습니다.");
    }

    @ExceptionHandler(AuthorizationException.class)
    public ResponseEntity<String> handleAuthorizationException(AuthorizationException exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(exception.getMessage());
    }

    @ExceptionHandler(AuthorizationMismatchException.class)
    public ResponseEntity<String> handleIllegalAuthorizationException(AuthorizationMismatchException exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(exception.getMessage());
    }

    @ExceptionHandler(IllegalAuthorizationException.class)
    public ResponseEntity<String> handleIllegalAuthorizationException(IllegalAuthorizationException exception) {
        return ResponseEntity.badRequest()
                .body(exception.getMessage());
    }

}
