package roomescape.exception;

import io.jsonwebtoken.JwtException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(annotations = RestController.class)
public class ExceptionApiController {

    private static final String INTERNAL_SERVER_ERROR_MESSAGE = "예기치 못한 오류가 발생하였습니다. 관리자에게 문의주세요.";

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ExceptionInfo> IllegalArgExHandler(IllegalArgumentException exception) {
        ExceptionInfo exceptionInfo = new ExceptionInfo(exception.getMessage());

        return ResponseEntity.badRequest().body(exceptionInfo);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> methodArgumentExHandler(MethodArgumentNotValidException exception) {
        Map<String, String> errors = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(error -> {
            String fieldName = error.getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ExceptionInfo> jwtExHandler(JwtException exception) {
        ExceptionInfo exceptionInfo = new ExceptionInfo(exception.getMessage());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(exceptionInfo);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionInfo> ExceptionExHandler() {
        ExceptionInfo exceptionInfo = new ExceptionInfo(INTERNAL_SERVER_ERROR_MESSAGE);

        return ResponseEntity.internalServerError().body(exceptionInfo);
    }
}
