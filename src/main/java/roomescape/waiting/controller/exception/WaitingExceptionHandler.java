package roomescape.waiting.controller.exception;

import static org.springframework.http.HttpStatus.CONFLICT;
import static roomescape.waiting.controller.response.WaitingErrorCode.IN_ALREADY_WAITING;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import roomescape.global.response.ApiResponse;
import roomescape.waiting.exception.InAlreadyWaitingException;

@RestControllerAdvice
public class WaitingExceptionHandler {

    @ExceptionHandler(InAlreadyWaitingException.class)
    public ResponseEntity<ApiResponse<Void>> handleInAlreadyWaitingException(InAlreadyWaitingException e) {
        return ResponseEntity
                .status(CONFLICT)
                .body(ApiResponse.fail(IN_ALREADY_WAITING));
    }
}
