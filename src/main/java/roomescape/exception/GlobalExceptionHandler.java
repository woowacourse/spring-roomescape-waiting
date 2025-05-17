package roomescape.exception;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import roomescape.exception.custom.ForbiddenException;
import roomescape.exception.custom.UnauthorizedException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(exception = IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e) {
        ErrorResponse response = ErrorResponse.from(e.getMessage());

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(exception = UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedException(UnauthorizedException e) {
        ErrorResponse response = ErrorResponse.from(e.getMessage());

        return ResponseEntity.status(UNAUTHORIZED)
                .body(response);
    }

    @ExceptionHandler(exception = ForbiddenException.class)
    public ResponseEntity<ErrorResponse> handleForbiddenException(ForbiddenException e) {
        ErrorResponse response = ErrorResponse.from(e.getMessage());

        return ResponseEntity.status(FORBIDDEN)
                .body(response);
    }

    @ExceptionHandler(exception = MethodArgumentNotValidException.class)
    public ResponseEntity<List<ErrorResponse>> handleValidationException(MethodArgumentNotValidException e) {
        List<ErrorResponse> responses = createValidationErrorMessage(e.getBindingResult());

        return ResponseEntity.badRequest().body(responses);
    }

    private List<ErrorResponse> createValidationErrorMessage(BindingResult bindingResult) {
        return bindingResult.getAllErrors()
                .stream()
                .map(ObjectError::getDefaultMessage)
                .map(ErrorResponse::from)
                .toList();
    }
}
