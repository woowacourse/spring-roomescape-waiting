package roomescape.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final String MESSAGE_FORMAT = "%s 필드명 : [%s]";

    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException e, HttpServletRequest req) {
        logWarn(e);
        final ErrorResponse errorResponse =
                new ErrorResponse(HttpStatus.BAD_REQUEST, req.getRequestURI(), e.getMessage());
        return ResponseEntity.status(e.getHttpStatus()).body(errorResponse);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException e,
                                                                               HttpServletRequest req) {
        logWarn(e);
        final ErrorResponse errorResponse =
                new ErrorResponse(HttpStatus.BAD_REQUEST, req.getRequestURI(), e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Object handleMissingServletRequestParameterException(
            MissingServletRequestParameterException e, HttpServletRequest req) {
        logWarn(e);
        final String message = MESSAGE_FORMAT
                .formatted(ErrorType.EMPTY_VALUE_NOT_ALLOWED.getMessage(), e.getParameterName());
        final ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST, req.getRequestURI(), message);
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException e,
                                                            HttpServletRequest req) {
        logWarn(e);
        final String message = MESSAGE_FORMAT
                .formatted(ErrorType.INVALID_DATA_TYPE.getMessage(), e.getName(), e.getValue().toString());
        final ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST, req.getRequestURI(), message);
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<ErrorResponse> handleUnhandledException(Exception e, HttpServletRequest req) {
        logError(e);
        final ErrorResponse errorResponse =
                new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, req.getRequestURI(), "서버 에러입니다.");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    private void logWarn(Exception exception) {
        logger.warn(exception.toString());
    }

    private void logError(Exception exception) {
        logger.error(exception.toString());
    }
}
