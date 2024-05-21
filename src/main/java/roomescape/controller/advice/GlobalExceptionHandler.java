package roomescape.controller.advice;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import roomescape.exception.RoomescapeException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(value = RoomescapeException.class)
    public ResponseEntity<String> handleCustomException(RoomescapeException exception) {
        return new ResponseEntity<>(exception.getMessage(), exception.getStatus());
    }

    @ExceptionHandler(value = HttpMessageConversionException.class)
    public ResponseEntity<String> handleJsonParsingException() {
        return new ResponseEntity<>("요청 body에 유효하지 않은 필드가 존재합니다.", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = {NoResourceFoundException.class, MissingServletRequestParameterException.class})
    public ResponseEntity<String> handleRequestException() {
        return new ResponseEntity<>("요청 경로에 필요한 변수가 제공되지 않았습니다.", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        e.printStackTrace();
        return new ResponseEntity<>("서버 에러입니다.", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
