package roomescape.exception.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(value = RoomescapeException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(RoomescapeException exception) {
        return new ResponseEntity<>(new ErrorResponse(exception.getMessage()), exception.getStatus());
    }

    @ExceptionHandler(value = HttpMessageConversionException.class)
    public ResponseEntity<ErrorResponse> handleJsonParsingException() {
        return new ResponseEntity<>(new ErrorResponse("요청 body에 유효하지 않은 필드가 존재합니다."), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = {
            NoResourceFoundException.class,
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ErrorResponse> handleRequestException() {
        return new ResponseEntity<>(new ErrorResponse("요청 경로에 필요한 변수가 제공되지 않았습니다."), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        e.printStackTrace();
        return new ResponseEntity<>(new ErrorResponse("서버 에러입니다."), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
