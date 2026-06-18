package roomescape.global.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException illegalArgumentException, HttpServletRequest httpServletRequest
    ) {
        ErrorResponse errorResponse = new ErrorResponseBuilder()
                .httpStatus(HttpStatus.BAD_REQUEST)
                .errorMessage(illegalArgumentException.getMessage())
                .apiUrl(httpServletRequest.getRequestURI())
                .timeStamp(LocalDateTime.now())
                .traceId(MDC.get("traceId"))
                .build();

        return ResponseEntity.status(errorResponse.httpStatus()).body(errorResponse);
    }

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(
            CustomException customException, HttpServletRequest httpServletRequest
    ) {
        ErrorResponse errorResponse = new ErrorResponseBuilder()
                .httpStatus(customException.getErrorCode().getHttpStatus())
                .errorMessage(customException.getErrorCode().getMessage())
                .apiUrl(httpServletRequest.getRequestURI())
                .timeStamp(LocalDateTime.now())
                .traceId(MDC.get("traceId"))
                .build();

        return ResponseEntity.status(customException.getHttpStatus()).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException methodArgumentNotValidException, HttpServletRequest httpServletRequest
    ) {
        ErrorResponse errorResponse = new ErrorResponseBuilder()
                .httpStatus(HttpStatus.BAD_REQUEST)
                .errorMessage("요청값이 잘못됐습니다.")
                .apiUrl(httpServletRequest.getRequestURI())
                .timeStamp(LocalDateTime.now())
                .traceId(MDC.get("traceId"))
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }


    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(
            ConstraintViolationException constraintViolationException, HttpServletRequest httpServletRequest
    ) {
        ErrorResponse errorResponse = new ErrorResponseBuilder()
                .httpStatus(HttpStatus.BAD_REQUEST)
                .errorMessage("요청값이 잘못됐습니다.")
                .apiUrl(httpServletRequest.getRequestURI())
                .timeStamp(LocalDateTime.now())
                .traceId(MDC.get("traceId"))
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
}
