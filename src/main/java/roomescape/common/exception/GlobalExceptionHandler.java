package roomescape.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;
import java.util.Objects;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ErrorResponse> handleDomainException(DomainException exception, HttpServletRequest request) {
        log.error("Domain exception occurred", exception);
        ErrorCode errorCode = exception.getErrorCode();

        return ResponseEntity
                .status(ErrorStatusMapper.map(errorCode))
                .body(ErrorResponse.of(request.getRequestURI(), errorCode.message()));
    }

    @ExceptionHandler(InfrastructureException.class)
    public ResponseEntity<ErrorResponse> handleInfrastructureException(InfrastructureException exception, HttpServletRequest request) {
        log.error("Infrastructure exception occurred", exception);
        return internalServerError(request.getRequestURI());
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception,
            HttpHeaders headers,
            HttpStatusCode statusCode,
            WebRequest request
    ) {
        log.error("Method Argument exception occurred", exception);
        String path = pathFrom(request);
        List<String> messages = exception.getBindingResult()
                .getAllErrors()
                .stream()
                .map(ObjectError::getDefaultMessage)
                .filter(Objects::nonNull)
                .toList();
        if (messages.isEmpty()) {
            messages = List.of("잘못된 요청입니다.");
        }

        return ResponseEntity
                .status(statusCode)
                .headers(headers)
                .body(ErrorResponse.of(path, messages));
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception exception,
            Object body,
            HttpHeaders headers,
            HttpStatusCode statusCode,
            WebRequest request
    ) {
        log.error("Spring Mvc Internal exception occurred", exception);
        String path = pathFrom(request);

        return ResponseEntity
                .status(statusCode)
                .headers(headers)
                .body(ErrorResponse.of(path, exception.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllUncaughtException(Exception exception, HttpServletRequest request) {
        log.error("Unexpected exception occurred", exception);
        return internalServerError(request.getRequestURI());
    }


    private ResponseEntity<ErrorResponse> internalServerError(String path) {
        return ResponseEntity
                .internalServerError()
                .body(ErrorResponse.of(path, "서버 내부에서 문제가 발생했습니다."));
    }

    private String pathFrom(WebRequest request) {
        return ((ServletWebRequest) request).getRequest().getRequestURI();
    }
}
