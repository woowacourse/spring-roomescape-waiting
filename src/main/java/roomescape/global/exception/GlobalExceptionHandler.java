package roomescape.global.exception;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

//TODO : 구체적인 예외 추상화 시켜 핸들링하기
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ResponseStatus(INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ExceptionResponse handleException(Exception e) {
        log.error("Unhandled exception occurred", e);
        return new ExceptionResponse(INTERNAL_SERVER_ERROR.value(), "서버 에러입니다", LocalDateTime.now());
    }

    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ExceptionResponse handleValidationException(MethodArgumentNotValidException e) {
        String exceptionMessage = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("\n"));

        return new ExceptionResponse(BAD_REQUEST.value(), exceptionMessage, LocalDateTime.now());
    }

    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler(value = HttpMessageNotReadableException.class)
    public ExceptionResponse handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        Throwable rootCause = e.getRootCause();
        String exceptionMessage = "잘못된 형식의 값이 입력되었습니다.";
        if (rootCause instanceof DateTimeException) {
            exceptionMessage = "잘못된 날짜 또는 시간 형식입니다.";
        }
        return new ExceptionResponse(BAD_REQUEST.value(), exceptionMessage, LocalDateTime.now());
    }

    // TODO : 잘못 입력된 곳이 HTTP Body인지, 경로변수 혹은 쿼리파라미터 등인지 파악하고 자세하게 예외 메시지를 출력해야 할까? 고민해보기
    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ExceptionResponse handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        String exceptionMessage = "잘못된 형식의 값이 입력되었습니다.";
        return new ExceptionResponse(BAD_REQUEST.value(), exceptionMessage, LocalDateTime.now());
    }

    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ExceptionResponse handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        String exceptionMessage = "필수 정보(파라미터)가 누락되었습니다";
        return new ExceptionResponse(BAD_REQUEST.value(), exceptionMessage, LocalDateTime.now());
    }

    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler(IllegalArgumentException.class)
    public ExceptionResponse handleIllegalArgumentException(IllegalArgumentException e) {
        return new ExceptionResponse(BAD_REQUEST.value(), e.getMessage(), LocalDateTime.now());
    }

    @ResponseStatus(UNPROCESSABLE_ENTITY)
    @ExceptionHandler(BusinessRuleViolationException.class)
    public ExceptionResponse handleBusinessRuleViolationException(BusinessRuleViolationException e) {
        return new ExceptionResponse(UNPROCESSABLE_ENTITY.value(), e.getMessage(), LocalDateTime.now());
    }

    @ResponseStatus(NOT_FOUND)
    @ExceptionHandler(ResourceNotFoundException.class)
    public ExceptionResponse handleResourceNotFoundException(ResourceNotFoundException e) {
        return new ExceptionResponse(NOT_FOUND.value(), e.getMessage(), LocalDateTime.now());
    }

    @ResponseStatus(CONFLICT)
    @ExceptionHandler(ResourceInUseException.class)
    public ExceptionResponse handleResourceInUseException(ResourceInUseException e) {
        return new ExceptionResponse(CONFLICT.value(), e.getMessage(), LocalDateTime.now());
    }

    @ResponseStatus(UNAUTHORIZED)
    @ExceptionHandler(AuthenticationException.class)
    public ExceptionResponse handleAuthenticationException(AuthenticationException e) {
        return new ExceptionResponse(UNAUTHORIZED.value(), e.getMessage(), LocalDateTime.now());
    }

    @ResponseStatus(FORBIDDEN)
    @ExceptionHandler(AuthorizationException.class)
    public ExceptionResponse handleAuthorizationException(AuthorizationException e) {
        return new ExceptionResponse(FORBIDDEN.value(), e.getMessage(), LocalDateTime.now());
    }
}
