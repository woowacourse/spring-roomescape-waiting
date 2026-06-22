package roomescape.global.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

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


    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException e, HttpServletRequest httpServletRequest
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

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(
            Exception exception, HttpServletRequest httpServletRequest
    ) {
        log.error("처리되지 않은 예외 발생 traceId={}", MDC.get("traceId"), exception);

        ErrorResponse errorResponse = new ErrorResponseBuilder()
                .httpStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                .errorMessage("서버 내부 오류가 발생했습니다.") // 내부 메시지 노출 금지
                .apiUrl(httpServletRequest.getRequestURI())
                .timeStamp(LocalDateTime.now())
                .traceId(MDC.get("traceId"))
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}
