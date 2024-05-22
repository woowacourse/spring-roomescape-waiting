package roomescape.common;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonMappingException.Reference;
import io.jsonwebtoken.JwtException;
import java.util.NoSuchElementException;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import roomescape.common.exception.ForbiddenException;
import roomescape.common.exception.UnAuthorizationException;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = Logger.getLogger("Logeer");
    private static final String EXCEPTION_PREFIX = "[ERROR] ";

    @ExceptionHandler
    public ResponseEntity<ProblemDetail> catchValidationException(MethodArgumentNotValidException ex) {
        String exceptionMessages = ex.getBindingResult().getFieldErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining("\n"));

        logger.warning(EXCEPTION_PREFIX + exceptionMessages);
        return ResponseEntity.status(400)
                .body(ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exceptionMessages));
    }

    @ExceptionHandler
    public ResponseEntity<ProblemDetail> catchHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        logger.warning(EXCEPTION_PREFIX + ex.getClass() + " " + ex.getMessage());
        String exceptionMessage = "잘못된 JSON 형식입니다.";
        if (ex.getCause() instanceof JsonMappingException jsonMappingException) {
            exceptionMessage = jsonMappingException.getPath().stream()
                    .map(Reference::getFieldName)
                    .collect(Collectors.joining(" ")) + " 필드의 형식이 잘못되었습니다.";
        }

        return ResponseEntity.status(400)
                .body(ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exceptionMessage));
    }

    @ExceptionHandler
    public ResponseEntity<ProblemDetail> catchBadRequestException(IllegalArgumentException ex) {
        System.out.println(EXCEPTION_PREFIX + ex.getMessage());
        return ResponseEntity.status(400)
                .body(ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage()));
    }

    @ExceptionHandler({
            UnAuthorizationException.class,
            JwtException.class
    })
    public ResponseEntity<ProblemDetail> catchUnauthorizedException(Exception ex) {
        System.out.println(EXCEPTION_PREFIX + ex.getMessage());
        return ResponseEntity.status(401)
                .body(ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, ex.getMessage()));
    }

    @ExceptionHandler
    public ResponseEntity<ProblemDetail> catchForbiddenException(ForbiddenException ex) {
        System.out.println(EXCEPTION_PREFIX + ex.getMessage());
        return ResponseEntity.status(403)
                .body(ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.getMessage()));
    }

    @ExceptionHandler({
            InvalidDataAccessApiUsageException.class,
            NoSuchElementException.class
    })
    public ResponseEntity<ProblemDetail> catchNotFoundException(RuntimeException ex) {
        System.out.println(EXCEPTION_PREFIX + ex.getMessage());
        return ResponseEntity.status(404)
                .body(ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage()));
    }

    @ExceptionHandler
    public ResponseEntity<ProblemDetail> catchConflictException(IllegalStateException ex) {
        System.out.println(EXCEPTION_PREFIX + ex.getMessage());
        return ResponseEntity.status(409)
                .body(ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage()));
    }

    @ExceptionHandler
    public ResponseEntity<ProblemDetail> catchInternalServerException(Exception ex) {
        logger.warning(EXCEPTION_PREFIX + ex.getClass() + " " + ex.getMessage());
        return ResponseEntity.status(500)
                .body(ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage()));
    }
}
