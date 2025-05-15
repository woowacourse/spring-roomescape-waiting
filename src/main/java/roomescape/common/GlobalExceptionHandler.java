package roomescape.common;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import roomescape.common.exception.CustomException;
import roomescape.common.exception.message.GlobalExceptionMessage;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final String EXCEPTION_HEADER = "[ERROR] ";

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleException() {
        return ResponseEntity.badRequest()
                .body(EXCEPTION_HEADER + GlobalExceptionMessage.INVALID_INPUT_VALUE.getMessage());
    }

    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<String> handleNullException() {
        return ResponseEntity.badRequest()
                .body(EXCEPTION_HEADER + GlobalExceptionMessage.NULL_VALUE.getMessage());
    }

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<String> handleCustomException(CustomException customException) {
        return ResponseEntity.badRequest()
                .body(EXCEPTION_HEADER + customException.getMessage());
    }
}
