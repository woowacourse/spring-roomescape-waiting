package roomescape.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import roomescape.common.response.ApiResponse;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<?>> handleException(BusinessException e) {
        return ResponseEntity.status(e.getStatus()).body(ApiResponse.createError(e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleValidException(MethodArgumentNotValidException e) {
        BindingResult bindingResult = e.getBindingResult();
        String message = "";
        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            message += fieldError.getDefaultMessage();
        }

        ApiResponse<?> response = ApiResponse.createError(message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleException(Exception e) {
        ErrorCode errorCode = GlobalErrorCode.INTERNAL_SERVER_ERROR;
        log.info(e.getMessage());
        return ResponseEntity.status(errorCode.getStatus()).body(ApiResponse.createError(errorCode.getMessage()));
    }
}
