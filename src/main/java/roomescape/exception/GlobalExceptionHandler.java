package roomescape.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import roomescape.common.api.ApiResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        ErrorCode errorCode = ErrorCode.INVALID_INPUT;
        return ResponseEntity.status(errorCode.getHttpStatus())
                .body(ApiResponse.fail(errorCode.getCode(), errorCode.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<?>> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        ErrorCode errorCode = ErrorCode.INVALID_INPUT;
        return ResponseEntity.status(errorCode.getHttpStatus())
                .body(ApiResponse.fail(errorCode.getCode(), errorCode.getMessage()));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<?>> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        ErrorCode errorCode = ErrorCode.INVALID_INPUT;
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.fail(errorCode.getCode(), errorCode.getMessage()));
    }

    @ExceptionHandler(EscapeRoomException.class)
    public ResponseEntity<ApiResponse<?>> handleEscapeRoomException(EscapeRoomException e) {
        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity.status(errorCode.getHttpStatus())
                .body(ApiResponse.fail(errorCode.getCode(), e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleException(Exception e) {
        ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(errorCode.getHttpStatus())
                .body(ApiResponse.fail(errorCode.getCode(), errorCode.getMessage()));
    }
}
