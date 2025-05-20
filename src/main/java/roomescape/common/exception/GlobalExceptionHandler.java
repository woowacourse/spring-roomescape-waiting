package roomescape.common.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import roomescape.common.response.ApiResponse;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<?>> handleException(BusinessException e) {
        return ResponseEntity.status(e.getStatus()).body(ApiResponse.createError(e.getMessage()));
    }

//    @ExceptionHandler(NullPointerException.class)
//    public ResponseEntity<String> handleNullException() {
//        return ResponseEntity.badRequest()
//                .body(EXCEPTION_HEADER + GlobalExceptionMessage.NULL_VALUE.getMessage());
//    }
//
//    @ExceptionHandler(CustomException.class)
//    public ResponseEntity<String> handleCustomException(CustomException customException) {
//        return ResponseEntity.badRequest()
//                .body(EXCEPTION_HEADER + customException.getMessage());
//    }
}
