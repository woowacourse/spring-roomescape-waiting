package roomescape.web;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;
import roomescape.exception.CustomException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final String ERROR_PREFIX = "exception occur : {}";

    @ExceptionHandler(value = CustomException.class)
    public ResponseEntity<String> handleCustomException(CustomException exception) {
        log.error(ERROR_PREFIX, exception.getMessage());
        return new ResponseEntity<>(exception.getMessage(), exception.getStatus());
    }

    @ExceptionHandler(value = BindException.class)
    public ResponseEntity<String> handleValidationException(BindException exception) {
        log.error(ERROR_PREFIX, exception.getMessage());
        return new ResponseEntity<>(exception.getBindingResult().getAllErrors().get(0).getDefaultMessage(),
                BAD_REQUEST);
    }

    @ExceptionHandler(value = HttpMessageConversionException.class)
    public ResponseEntity<String> handleJsonParsingException(Exception exception) {
        log.error(ERROR_PREFIX, exception.getMessage());
        return new ResponseEntity<>("유효하지 않은 필드가 존재합니다.", BAD_REQUEST);
    }

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<String> handleException(Exception exception) {
        exception.printStackTrace();
        log.error(ERROR_PREFIX, exception.getMessage());
        return new ResponseEntity<>("서버 에러입니다.", INTERNAL_SERVER_ERROR);
    }
}
