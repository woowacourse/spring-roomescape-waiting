package roomescape.controller.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "roomescape")
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseCustomException.class)
    public ResponseEntity<CustomExceptionResponse> handleCustomException(BaseCustomException ex) {
        return ResponseEntity.status(ex.getHttpStatus())
                .body(new CustomExceptionResponse(ex.getTitle(), ex.getDetail()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<CustomExceptionResponse> handelError(Exception e) {
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new CustomExceptionResponse("서버 내부 문제가 발생했습니다.", "알 수 없는 문제가 발생했습니다."));
    }
}
