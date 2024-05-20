package roomescape.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@ControllerAdvice
public class RoomescapeExceptionHandler {
    private static class ErrorResponse {
        private static final boolean ERROR_STATUS = false;
        private final String message;

        public ErrorResponse(final Exception e) {
            this.message = e.getMessage();
        }

        public ErrorResponse(final String message) {
            this.message = message;
        }

        public boolean isStatus() {
            return ERROR_STATUS;
        }

        public String getMessage() {
            return message;
        }
    }

    @ExceptionHandler(value = MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        return ResponseEntity.badRequest()
                .body(new ErrorResponse(String.format("%s 의 입력 형식이 올바르지 않습니다.", e.getName())));
    }

    @ExceptionHandler(value = IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleArgumentException(final IllegalArgumentException exception) {
        return ResponseEntity.badRequest()
                .body(new ErrorResponse(exception));
    }

    @ExceptionHandler(value = PastTimeReservationException.class)
    public ResponseEntity<ErrorResponse> handlePastTimeReservationException(final PastTimeReservationException exception) {
        return ResponseEntity.badRequest()
                .body(new ErrorResponse(exception));
    }

    @ExceptionHandler(value = ExistReservationException.class)
    public ResponseEntity<ErrorResponse> handleExistReservationException(final ExistReservationException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(exception));
    }

    @ExceptionHandler(value = AlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleAlreadyExistsException(final AlreadyExistsException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(exception));

    }

    @ExceptionHandler(value = NotExistException.class)
    public ResponseEntity<ErrorResponse> handleNotExistException(final NotExistException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(exception));
    }

    @ExceptionHandler(value = UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnAuthorizationException(final UnauthorizedException exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse(exception));
    }

    @ExceptionHandler(value = ForbiddenException.class)
    public ResponseEntity<ErrorResponse> handleForbiddenException(final ForbiddenException exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse(exception));
    }

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<ErrorResponse> handleException(final Exception exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(exception));
    }

}
