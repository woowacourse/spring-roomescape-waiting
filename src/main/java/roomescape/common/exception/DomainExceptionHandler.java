package roomescape.common.exception;

import static roomescape.common.exception.GlobalErrorCode.SERVER_ERROR;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class DomainExceptionHandler {

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ErrorResponse> handleDomainException(DomainException exception, HttpServletRequest request) {
        ErrorPolicy errorCode = exception.getErrorPolicy();

        ErrorResponse errorResponse = ErrorResponse.of(request.getRequestURI(), errorCode.code(), errorCode.message());

        return ResponseEntity
                .status(errorCode.status())
                .body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllUncaughtException(Exception exception, HttpServletRequest request) {
        ErrorResponse errorResponse = ErrorResponse.of(request.getRequestURI(), SERVER_ERROR.code(),
                SERVER_ERROR.message());
        return ResponseEntity
                .internalServerError()
                .body(errorResponse);
    }

}
