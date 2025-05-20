package roomescape.global.advice;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static roomescape.global.response.GlobalErrorCode.NO_ELEMENTS;
import static roomescape.global.response.GlobalErrorCode.ROOMESCAPE_SERVER_ERROR;
import static roomescape.global.response.GlobalErrorCode.WRONG_ARGUMENT;

import java.util.NoSuchElementException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import roomescape.global.exception.InvalidArgumentException;
import roomescape.global.response.ApiResponse;

@RestControllerAdvice
public class RoomescapeExceptionHandler {

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoSuchElementException() {
        return ResponseEntity.status(NOT_FOUND)
                .body(ApiResponse.fail(NO_ELEMENTS));
    }

    @ExceptionHandler(InvalidArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidArgumentException(InvalidArgumentException e) {
        return ResponseEntity.status(BAD_REQUEST)
                .body(ApiResponse.fail(WRONG_ARGUMENT));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValidException() {
        return ResponseEntity.status(BAD_REQUEST)
                .body(ApiResponse.fail(WRONG_ARGUMENT));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Void>> handleException() {
        return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                .body(ApiResponse.fail(ROOMESCAPE_SERVER_ERROR));
    }
}
