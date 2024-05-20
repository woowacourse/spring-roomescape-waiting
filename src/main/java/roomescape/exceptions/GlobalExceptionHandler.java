package roomescape.exceptions;

import java.time.format.DateTimeParseException;
import javax.naming.AuthenticationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleValidationException(ValidationException e) {
        return e.getMessage();
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        if (e.getCause() instanceof DateTimeParseException) {
            return handleDateTimeParseException();
        } else {
            return "요청 본문을 읽는 도중 오류가 발생했습니다.";
        }
    }

    private String handleDateTimeParseException() {
        return "올바르지 않은 날짜 / 시간 형식입니다.";
    }

    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public String handleException(AuthenticationException e) {
        return e.getMessage();
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleException() {
        return "서버에서 예기치 않은 오류가 발생했습니다.";
    }
}
