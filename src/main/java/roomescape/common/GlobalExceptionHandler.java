package roomescape.common;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import roomescape.common.exception.CustomException;
import roomescape.common.exception.message.GlobalExceptionMessage;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final String EXCEPTION_HEADER = "[ERROR] ";

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<String> handleCustomException(CustomException customException) {
        return ResponseEntity.badRequest()
                .body(EXCEPTION_HEADER + customException.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException() {
        return ResponseEntity.badRequest()
                .body(EXCEPTION_HEADER + GlobalExceptionMessage.INVALID_INPUT_VALUE.getMessage());
    }

    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<String> handleNullPointerException() {
        return ResponseEntity.badRequest()
                .body(EXCEPTION_HEADER + GlobalExceptionMessage.NULL_VALUE.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException() {
        return ResponseEntity.badRequest()
                .body(EXCEPTION_HEADER + GlobalExceptionMessage.RUNTIME_EXCEPTION.getMessage());
    }
}
