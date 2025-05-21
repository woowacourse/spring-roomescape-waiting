package roomescape.global.advice;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static roomescape.global.response.GlobalErrorCode.IN_ALREADY_EXCEPTION;
import static roomescape.global.response.GlobalErrorCode.NO_ELEMENTS;
import static roomescape.global.response.GlobalErrorCode.ROOMESCAPE_SERVER_ERROR;
import static roomescape.global.response.GlobalErrorCode.WRONG_ARGUMENT;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import roomescape.global.exception.InAlreadyException;
import roomescape.global.exception.InvalidArgumentException;
import roomescape.global.exception.NotFoundException;
import roomescape.global.response.ApiResponse;

@Slf4j
@RestControllerAdvice
public class RoomescapeExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFoundException() {
        return ResponseEntity.status(NOT_FOUND)
                .body(ApiResponse.fail(NO_ELEMENTS));
    }

    @ExceptionHandler(InvalidArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidArgumentException() {
        return ResponseEntity.status(BAD_REQUEST)
                .body(ApiResponse.fail(WRONG_ARGUMENT));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValidException() {
        return ResponseEntity.status(BAD_REQUEST)
                .body(ApiResponse.fail(WRONG_ARGUMENT));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Void>> handleException(RuntimeException e) {
        log.error("Unexpected error occurred : {}", e.getMessage(), e);
        return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                .body(ApiResponse.fail(ROOMESCAPE_SERVER_ERROR));
    }

    @ExceptionHandler(InAlreadyException.class)
    public ResponseEntity<ApiResponse<Void>> handleInAlreadyException() {
        return ResponseEntity.status(CONFLICT)
                .body(ApiResponse.fail(IN_ALREADY_EXCEPTION));
    }
}
