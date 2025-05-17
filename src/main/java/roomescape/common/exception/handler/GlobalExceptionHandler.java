package roomescape.common.exception.handler;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import roomescape.common.exception.BusinessException;
import roomescape.common.exception.handler.dto.ExceptionResponse;
import roomescape.member.exception.EmailException;
import roomescape.member.exception.NameException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ExceptionResponse> handleNullPointer(final BusinessException exception, final HttpServletRequest request) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(
                HttpStatus.BAD_REQUEST.value(), "[ERROR] " + exception.getMessage(), request.getRequestURI()
        );

        return ResponseEntity.badRequest().body(exceptionResponse);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ExceptionResponse> handleIllegalArgument(final IllegalArgumentException exception, final HttpServletRequest request) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(
                HttpStatus.BAD_REQUEST.value(), "[ERROR] " + exception.getMessage(), request.getRequestURI()
        );

        return ResponseEntity.badRequest().body(exceptionResponse);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ExceptionResponse> handleHttpMessageNotReadable(
            final HttpMessageNotReadableException exception, final HttpServletRequest request
    ) {
        Throwable rootCause = exception.getRootCause();
        if (rootCause instanceof IllegalArgumentException) {
            ExceptionResponse exceptionResponse = new ExceptionResponse(
                    HttpStatus.BAD_REQUEST.value(), "[ERROR] " + rootCause.getMessage(), request.getRequestURI()
            );
            return ResponseEntity.badRequest().body(exceptionResponse);
        }

        ExceptionResponse exceptionResponse = new ExceptionResponse(
                HttpStatus.BAD_REQUEST.value(), "[ERROR] 요청 입력이 잘못되었습니다.", request.getRequestURI()
        );
        return ResponseEntity.badRequest().body(exceptionResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponse> handleException(final Exception exception, final HttpServletRequest request) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(
                HttpStatus.BAD_REQUEST.value(), "[ERROR] " + exception.getMessage(), request.getRequestURI()
        );

        return ResponseEntity.badRequest().body(exceptionResponse);
    }
}
