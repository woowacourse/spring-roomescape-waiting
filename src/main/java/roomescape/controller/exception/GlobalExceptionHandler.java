package roomescape.controller.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import roomescape.config.ForbiddenAccessException;
import roomescape.dto.exception.InputNotAllowedException;
import roomescape.service.exception.OperationNotAllowedException;
import roomescape.service.exception.ResourceNotFoundException;

@RestControllerAdvice(basePackages = "roomescape")
public class GlobalExceptionHandler {

    @ExceptionHandler(ForbiddenAccessException.class)
    public ResponseEntity<CustomExceptionResponse> handleForbiddenAccess(ForbiddenAccessException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new CustomExceptionResponse(e.getTitle(), e.getDetail()));
    }

    @ExceptionHandler(InputNotAllowedException.class)
    public ResponseEntity<CustomExceptionResponse> handleInputNotAllowed(InputNotAllowedException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new CustomExceptionResponse(e.getTitle(), e.getDetail()));
    }

    @ExceptionHandler(OperationNotAllowedException.class)
    public ResponseEntity<CustomExceptionResponse> handleOperationNotAllowed(OperationNotAllowedException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new CustomExceptionResponse(e.getTitle(), e.getDetail()));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<CustomExceptionResponse> handleResourceNotFound(ResourceNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new CustomExceptionResponse(e.getTitle(), e.getDetail()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<CustomExceptionResponse> handelError(Exception e) {
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new CustomExceptionResponse("서버 내부 문제가 발생했습니다.", "알 수 없는 문제가 발생했습니다."));
    }
}
