package roomescape.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = NotFoundException.class)
    public ResponseEntity<ExceptionResponse> handleNotFoundException(NotFoundException ex) {
        ex.printStackTrace();
        ExceptionResponse response = new ExceptionResponse(HttpStatus.NOT_FOUND, ex.getMessage());
        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }

    @ExceptionHandler(value = {BadRequestException.class, DuplicatedException.class})
    public ResponseEntity<ExceptionResponse> handleBadRequestAndDuplicatedException(RuntimeException ex) {
        ex.printStackTrace();
        ExceptionResponse response = new ExceptionResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
        return ResponseEntity
                .status(response.getStatus())
                .body(response);

    }

    @ExceptionHandler(value = AuthorizationException.class)
    public ResponseEntity<ExceptionResponse> handleAuthorizationException(AuthorizationException ex) {
        ex.printStackTrace();
        ExceptionResponse response = new ExceptionResponse(HttpStatus.UNAUTHORIZED, ex.getMessage());
        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }

    @ExceptionHandler(value = HttpMessageNotReadableException.class)
    public ResponseEntity<ExceptionResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        ex.printStackTrace();
        ExceptionResponse response = new ExceptionResponse(HttpStatus.BAD_REQUEST, "[REQUEST] 올바른 형태의 필드를 입력해주세요.");
        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        ex.printStackTrace();
        String message = ex.getAllErrors().get(0).getDefaultMessage();
        ExceptionResponse response = new ExceptionResponse(HttpStatus.BAD_REQUEST, "[REQUEST BODY] " + message);
        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }

    @ExceptionHandler(value = ConstraintViolationException.class)
    public ResponseEntity<ExceptionResponse> handleConstraintViolationException(ConstraintViolationException ex) {
        ex.printStackTrace();
        ExceptionResponse response = new ExceptionResponse(HttpStatus.BAD_REQUEST, "[REQUEST PARAMETER] " + ex.getMessage());
        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }

    @ExceptionHandler(value = IllegalStateException.class)
    public ResponseEntity<ExceptionResponse> handleIllegalStateException(IllegalStateException ex) {
        ex.printStackTrace();
        ExceptionResponse response = new ExceptionResponse(HttpStatus.BAD_REQUEST, "[ERROR] " + ex.getMessage());
        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }

    @ExceptionHandler(value = RuntimeException.class)
    public ResponseEntity<ExceptionResponse> handleRuntimeException(RuntimeException ex) {
        ex.printStackTrace();
        ExceptionResponse response = new ExceptionResponse(HttpStatus.INTERNAL_SERVER_ERROR, "[SERVER] 관리자에게 문의해주세요.");
        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }
}
