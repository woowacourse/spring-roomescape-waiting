package roomescape.global.exception;

import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import roomescape.domain.exception.RoomescapeException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final DomainErrorHttpMapper httpMapper;

    public GlobalExceptionHandler(DomainErrorHttpMapper httpMapper) {
        this.httpMapper = httpMapper;
    }

    @ExceptionHandler(RoomescapeException.class)
    public ResponseEntity<ErrorResponse> handleRoomescape(RoomescapeException e, HttpServletRequest request) {
        HttpStatus status = httpMapper.statusOf(e.getCode());
        return ResponseEntity.status(status)
                .body(new ErrorResponse(e.getCode().name(), e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException e
    ) {
        return ResponseEntity.badRequest()
                .body(new ErrorResponse(
                        "INVALID_REQUEST",
                        "잘못된 요청 형식입니다."
                ));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(
            HttpMessageNotReadableException e
    ) {
        return ResponseEntity.badRequest()
                .body(new ErrorResponse(
                        "INVALID_REQUEST",
                        "잘못된 요청 형식입니다."
                ));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException e
    ) {
        return ResponseEntity.badRequest()
                .body(new ErrorResponse(
                        "INVALID_REQUEST",
                        "잘못된 요청 형식입니다."
                ));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParam(
            MissingServletRequestParameterException e
    ) {
        return ResponseEntity.badRequest()
                .body(new ErrorResponse(
                        "INVALID_REQUEST",
                        "잘못된 요청 형식입니다."
                ));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException e
    ) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(new ErrorResponse(
                        "METHOD_NOT_ALLOWED",
                        "지원하지 않는 요청 메서드입니다."
                ));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFound(
            NoResourceFoundException e
    ) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(
                        "NOT_FOUND",
                        "요청한 리소스를 찾을 수 없습니다."
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception e, HttpServletRequest request) {
        log.error("[500] {} {} - unhandled: {}", request.getMethod(), request.getRequestURI(), e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("INTERNAL_ERROR", "일시적인 오류가 발생했습니다."));
    }
}
