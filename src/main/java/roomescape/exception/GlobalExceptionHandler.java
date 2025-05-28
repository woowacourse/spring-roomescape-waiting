package roomescape.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.List;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<List<FieldErrorResponse>> handle(MethodArgumentNotValidException e) {
        BindingResult bindingResult = e.getBindingResult();
        List<FieldErrorResponse> fieldErrorResponses = bindingResult.getFieldErrors().stream()
                .map(fieldError -> new FieldErrorResponse(
                                LocalDateTime.now(),
                                fieldError.getCode(),
                                fieldError.getField(),
                                fieldError.getDefaultMessage()
                        )
                )
                .toList();

        return ResponseEntity
                .status(ErrorCode.INVALID_INPUT.getHttpStatus())
                .body(fieldErrorResponses);
    }

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleInvalidInput(CustomException e) {
        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity
            .status(errorCode.getHttpStatus())
            .body(new ErrorResponse(LocalDateTime.now(), errorCode.name(), e.getMessage()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDeleteConflict(DataIntegrityViolationException e) {
        ErrorCode errorCode = ErrorCode.DELETE_CONFLICT;
        return ResponseEntity
            .status(errorCode.getHttpStatus())
            .body(new ErrorResponse(LocalDateTime.now(), errorCode.name(), e.getMessage()));
    }
}
