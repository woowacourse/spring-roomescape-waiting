package roomescape.common;

import com.fasterxml.jackson.databind.JsonMappingException;
import io.jsonwebtoken.JwtException;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final String EXCEPTION_PREFIX = "[ERROR] ";

    @ExceptionHandler
    public ResponseEntity<ProblemDetail> catchInternalServerException(Exception ex) {
        System.out.println(EXCEPTION_PREFIX + ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage()));
    }

    @ExceptionHandler
    public ResponseEntity<ProblemDetail> catchValidationException(MethodArgumentNotValidException ex) {
        String exceptionMessages = ex.getBindingResult().getFieldErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining("\n"));

        System.out.println(exceptionMessages);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exceptionMessages));
    }

    @ExceptionHandler
    public ResponseEntity<ProblemDetail> catchHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        if (ex.getCause() instanceof JsonMappingException jsonMappingException) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                            jsonMappingException.getPath()
                                    .stream()
                                    .map(JsonMappingException.Reference::getFieldName)
                                    .collect(Collectors.joining(", "))
                                    + " 필드의 형식이 잘못되었습니다."));
        }

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "잘못된 JSON 형식입니다."));
    }

    @ExceptionHandler
    public ResponseEntity<ProblemDetail> catchBadRequestException(IllegalArgumentException ex) {
        System.out.println(EXCEPTION_PREFIX + ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage()));
    }

    @ExceptionHandler({
            SecurityException.class,
            JwtException.class
    })
    public ResponseEntity<ProblemDetail> catchUnauthorizedException(Exception ex) {
        System.out.println(EXCEPTION_PREFIX + ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, ex.getMessage()));
    }

    @ExceptionHandler
    public ResponseEntity<ProblemDetail> catchNotFoundException(NoSuchElementException ex) {
        System.out.println(EXCEPTION_PREFIX + ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage()));
    }

    @ExceptionHandler
    public ResponseEntity<ProblemDetail> catchConflictException(IllegalStateException ex) {
        System.out.println(EXCEPTION_PREFIX + ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage()));
    }
}
