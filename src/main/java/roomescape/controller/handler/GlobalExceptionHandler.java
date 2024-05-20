package roomescape.controller.handler;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonMappingException.Reference;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.coyote.BadRequestException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String MESSAGE_HEADER = "[ERROR] ";

    @ExceptionHandler({IllegalArgumentException.class, NumberFormatException.class, BadRequestException.class})
    public ResponseEntity<String> badRequestException(Exception e) {
        return ResponseEntity.badRequest()
                .body(MESSAGE_HEADER + e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> methodArgumentValidException(MethodArgumentNotValidException e) {
        List<ObjectError> allErrors = e.getBindingResult()
                .getAllErrors();
        String errorMessages = allErrors.stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining(System.lineSeparator()));
        return ResponseEntity.badRequest()
                .body(MESSAGE_HEADER + errorMessages);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<String> httpMessageNotReadableException(HttpMessageNotReadableException e) {
        if (e.getCause() instanceof JsonMappingException jsonMappingException) {
            List<Reference> paths = jsonMappingException.getPath();
            String errorMessages = paths.stream()
                    .map(reference -> reference.getFieldName() + " 필드 내용이 잘못되었습니다.")
                    .collect(Collectors.joining(System.lineSeparator()));
            return ResponseEntity.badRequest()
                    .body(MESSAGE_HEADER + errorMessages);
        }
        String errorMessage = MESSAGE_HEADER + "적절하지 않은 입력값 입니다";
        return ResponseEntity.badRequest()
                .body(errorMessage);
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<String> securityException(SecurityException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> unPredictableException(RuntimeException e) {
        return ResponseEntity.badRequest()
                .body(MESSAGE_HEADER + e.getMessage());
    }
}
