package roomescape.exception;

import jakarta.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RoomescapeException.class)
    ResponseEntity<ApiExceptionResponse<String>> handleRoomescapeException(RoomescapeException ex) {
        return ResponseEntity.status(ex.getHttpStatus())
                .body(new ApiExceptionResponse<>(ex.getHttpStatus(), ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiExceptionResponse<Map<String, String>>> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors()
                .forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ResponseEntity.badRequest()
                .body(new ApiExceptionResponse<>(HttpStatus.BAD_REQUEST, errors));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiExceptionResponse<String>> handleMethodConstraintViolationException(ConstraintViolationException ex) {
        return ResponseEntity.badRequest()
                .body(new ApiExceptionResponse<>(HttpStatus.BAD_REQUEST, ex.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiExceptionResponse<String>> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest()
                .body(new ApiExceptionResponse<>(HttpStatus.BAD_REQUEST, ex.getMessage()));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiExceptionResponse<String>> handleAuthenticationException(AuthenticationException ex) {
        return ResponseEntity.status(ex.getHttpStatus())
                .body(new ApiExceptionResponse<>(ex.getHttpStatus(), ex.getMessage()));
    }

    @ExceptionHandler(RuntimeException.class)
    ResponseEntity<ApiExceptionResponse<String>> handleRuntimeException() {
        return ResponseEntity.internalServerError()
                .body(new ApiExceptionResponse<>(HttpStatus.INTERNAL_SERVER_ERROR, "서버에서 예기치 못한 오류가 발생했습니다. 문제가 지속되는 경우 관리자에게 문의해주세요."));
    }
}
