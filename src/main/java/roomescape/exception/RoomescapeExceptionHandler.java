package roomescape.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import io.jsonwebtoken.ExpiredJwtException;

@ControllerAdvice
public class RoomescapeExceptionHandler {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @ExceptionHandler(ForbiddenAccessException.class)
    public ResponseEntity<CustomExceptionResponse> handleForbiddenAccess(ForbiddenAccessException e) {
        logger.error(e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new CustomExceptionResponse(e.getTitle(), e.getDetail()));
    }

    @ExceptionHandler(InputNotAllowedException.class)
    public ResponseEntity<CustomExceptionResponse> handleInputNotAllowed(InputNotAllowedException e) {
        logger.error(e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new CustomExceptionResponse(e.getTitle(), e.getDetail()));
    }

    @ExceptionHandler(OperationNotAllowedException.class)
    public ResponseEntity<CustomExceptionResponse> handleOperationNotAllowed(OperationNotAllowedException e) {
        logger.error(e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new CustomExceptionResponse(e.getTitle(), e.getDetail()));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<CustomExceptionResponse> handleResourceNotFound(ResourceNotFoundException e) {
        logger.error(e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new CustomExceptionResponse(e.getTitle(), e.getDetail()));
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<CustomExceptionResponse> handleExpiredJwtException(ExpiredJwtException e) {
        logger.error(e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new CustomExceptionResponse("토큰이 만료되었습니다", ""));
    }

    @ExceptionHandler(TokenValidationFailureException.class)
    public ResponseEntity<CustomExceptionResponse> handleTokenValidationFailure(TokenValidationFailureException e) {
        logger.error(e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new CustomExceptionResponse(e.getTitle(), e.getDetail()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<CustomExceptionResponse> handelError(Exception e) {
        logger.error(e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new CustomExceptionResponse("서버 내부 문제가 발생했습니다.", "알 수 없는 문제가 발생했습니다."));
    }
}
