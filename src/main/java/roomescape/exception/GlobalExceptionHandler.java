package roomescape.exception;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import roomescape.dto.response.ErrorResponse;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(RuntimeException e) {
        ResponseStatus status = e.getClass().getAnnotation(ResponseStatus.class);
        if (status == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("서버 내부 오류가 발생했습니다."));
        }
        return ResponseEntity.status(status.value())
                .body(new ErrorResponse(e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException exception) {
        List<FieldError> fieldErrors = exception.getBindingResult().getFieldErrors();
        if (fieldErrors.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("유효하지 않은 요청입니다."));
        }

        String fields = fieldErrors.stream()
                .map(error -> error.getField() + " (" + error.getDefaultMessage() + ")")
                .collect(Collectors.joining(", "));
        return ResponseEntity.badRequest()
                .body(new ErrorResponse("유효하지 않은 값입니다: " + fields));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleMessageNotReadable(HttpMessageNotReadableException exception) {
        if (exception.getCause() instanceof InvalidFormatException invalidFormat) {
            String fieldName = invalidFormat.getPath().stream()
                    .map(JsonMappingException.Reference::getFieldName)
                    .findFirst()
                    .orElse("알 수 없는 필드");
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("요청 형식이 올바르지 않습니다: " + fieldName));
        }
        return ResponseEntity.badRequest()
                .body(new ErrorResponse("요청 형식이 올바르지 않습니다."));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException exception) {
        return ResponseEntity.badRequest()
                .body(new ErrorResponse("요청 형식이 올바르지 않습니다: " + exception.getName()));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException exception) {
        String messages = exception.getConstraintViolations().stream()
                .map(violation -> violation.getMessage())
                .collect(Collectors.joining(", "));
        return ResponseEntity.badRequest()
                .body(new ErrorResponse(messages));
    }
}
