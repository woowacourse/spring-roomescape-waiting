package roomescape.global.advice;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static roomescape.global.response.GlobalErrorCode.NO_ELEMENTS;
import static roomescape.global.response.GlobalErrorCode.ROOMESCAPE_SERVER_ERROR;
import static roomescape.global.response.GlobalErrorCode.WRONG_ARGUMENT;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import roomescape.global.exception.InvalidArgumentException;
import roomescape.global.exception.NoElementsException;
import roomescape.global.response.ApiResponse;

@RestControllerAdvice
public class RoomescapeExceptionHandler {

    @ExceptionHandler(NoElementsException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoSuchElementException(NoElementsException e) {
        return ResponseEntity
                .status(NOT_FOUND)
                .body(ApiResponse.fail(NO_ELEMENTS, e.getMessage()));
    }

    @ExceptionHandler(InvalidArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidArgumentException(InvalidArgumentException e) {
        return ResponseEntity
                .status(BAD_REQUEST)
                .body(ApiResponse.fail(WRONG_ARGUMENT, e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValidException() {
        return ResponseEntity
                .status(BAD_REQUEST)
                .body(ApiResponse.fail(WRONG_ARGUMENT, "요청 인자값이 잘못되었습니다."));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Void>> handleException() {
        return ResponseEntity
                .status(INTERNAL_SERVER_ERROR)
                .body(ApiResponse.fail(ROOMESCAPE_SERVER_ERROR, "서버 내부 오류입니다."));
    }
}
