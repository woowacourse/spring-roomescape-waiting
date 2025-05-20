package roomescape.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.List;
import java.util.Map;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ProblemDetail> handleNotFoundException(NotFoundException ex, HttpServletRequest request) {
        return ResponseEntity.status(ex.getHttpStatus()).body(getProblemDetail(ex, request.getRequestURI()));
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ProblemDetail> handleBadRequestException(BadRequestException ex, HttpServletRequest request) {
        return ResponseEntity.status(ex.getHttpStatus()).body(getProblemDetail(ex, request.getRequestURI()));
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ProblemDetail> handleConflictException(ConflictException ex, HttpServletRequest request) {
        return ResponseEntity.status(ex.getHttpStatus()).body(getProblemDetail(ex, request.getRequestURI()));
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ProblemDetail> handleUnauthorizedException(UnauthorizedException ex,
                                                                     HttpServletRequest request) {
        return ResponseEntity.status(ex.getHttpStatus()).body(getProblemDetail(ex, request.getRequestURI()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex,
                                                                               HttpServletRequest request) {
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .toList();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(getProblemDetail(HttpStatus.BAD_REQUEST, errors, request.getRequestURI()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex,
                                                                               HttpServletRequest request) {
        ProblemDetail problemDetail = getProblemDetail(new BadRequestException(ExceptionCause.INPUT_INVALID),
                request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
    }


    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ProblemDetail> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request) {
        ProblemDetail problemDetail = getProblemDetail(HttpStatus.BAD_REQUEST,
                List.of(ex.getParameter().getParameterName() + " 값의 형식이 올바르지 않습니다."),
                request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
    }

    private ProblemDetail getProblemDetail(HttpStatus httpStatus, List<String> message, String requestUri) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(httpStatus);
        problemDetail.setProperties(Map.of("errors", message));
        problemDetail.setType(URI.create(requestUri));
        return problemDetail;
    }

    private ProblemDetail getProblemDetail(RoomEscapeException exception, String requestUri) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(exception.getHttpStatus());
        problemDetail.setProperties(Map.of("errors", exception.getMessage()));
        problemDetail.setType(URI.create(requestUri));
        return problemDetail;
    }
}
