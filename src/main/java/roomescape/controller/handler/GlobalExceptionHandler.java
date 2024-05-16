package roomescape.controller.handler;

import com.fasterxml.jackson.databind.JsonMappingException;
import org.apache.coyote.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({IllegalArgumentException.class, NumberFormatException.class, BadRequestException.class})
    public ResponseEntity<String> badRequestException(Exception ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<String> httpMessageNotReadableException(HttpMessageNotReadableException e) {
        if (e.getCause() instanceof JsonMappingException jsonMappingException) {
            String errorMessage = jsonMappingException.getPath().get(0).getFieldName() + "필드 내용이 잘못되었습니다.";
            return ResponseEntity.badRequest().body(errorMessage);
        }
        String errorMessage = "[ERROR] 적절하지 않은 입력값 입니다";
        return ResponseEntity.badRequest().body(errorMessage);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> unPredictableException(RuntimeException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}
