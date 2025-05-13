package roomescape.global;

import java.util.List;
import java.util.NoSuchElementException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import roomescape.global.dto.ErrorResponse;
import roomescape.global.exception.AccessDeniedException;
import roomescape.global.exception.AuthenticationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthentication(final AuthenticationException exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.from(exception));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(final AuthenticationException exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorResponse.from(exception));
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErrorResponse> handleNoSuchElementException(final NoSuchElementException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.from(exception));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(final IllegalArgumentException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse.from(exception));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(final IllegalStateException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ErrorResponse.from(exception));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<List<ErrorResponse>> handleValidationExceptions(
            final MethodArgumentNotValidException exception
    ) {
        List<ErrorResponse> errorResponses = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> new ErrorResponse(
                        String.format("%s:%s", error.getField(), error.getDefaultMessage())
                ))
                .toList();
        return ResponseEntity.badRequest().body(errorResponses);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(final Exception exception) {
        log.error("서버 에러 발생", exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ErrorResponse("예상치 못한 서버 에러가 발생했습니다.")
        );
    }
}
