package roomescape.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({
            DuplicateContentException.class,
            InvalidRequestException.class
    })
    public ProblemDetail handleBadRequestException(Exception ex, HttpServletRequest request) {
        return GlobalProblemDetail.of(HttpStatus.BAD_REQUEST, List.of(ex.getMessage()), request.getRequestURI());
    }

    @ExceptionHandler(NotFoundException.class)
    public ProblemDetail handleNotFoundException(NotFoundException ex, HttpServletRequest request) {
        return GlobalProblemDetail.of(HttpStatus.NOT_FOUND, List.of(ex.getMessage()), request.getRequestURI());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ProblemDetail handleIllegalStateException(IllegalStateException ex, HttpServletRequest request) {
        return GlobalProblemDetail.of(HttpStatus.SERVICE_UNAVAILABLE, List.of(ex.getMessage()),
                request.getRequestURI());
    }

    @ExceptionHandler(ConstrainedDataException.class)
    public ProblemDetail handleConstrainedDataException(ConstrainedDataException ex, HttpServletRequest request) {
        return GlobalProblemDetail.of(HttpStatus.CONFLICT, List.of(ex.getMessage()), request.getRequestURI());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleHttpMessageNotReadableException(HttpMessageNotReadableException ex,
                                                               HttpServletRequest request) {
        return GlobalProblemDetail.of(HttpStatus.BAD_REQUEST, List.of("[ERROR] 입력한 값의 형식이 올바르지 않습니다."),
                request.getRequestURI());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleMethodArgumentNotValidException(MethodArgumentNotValidException ex,
                                                               HttpServletRequest request) {
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .toList();

        return GlobalProblemDetail.of(HttpStatus.BAD_REQUEST, errors, request.getRequestURI());
    }

    @ExceptionHandler(InvalidAuthorizationException.class)
    public ProblemDetail handleInvalidAuthorizationException(InvalidAuthorizationException ex,
                                                             HttpServletRequest request) {
        return GlobalProblemDetail.of(HttpStatus.UNAUTHORIZED, List.of(ex.getMessage()), request.getRequestURI());
    }

    @ExceptionHandler(UnauthorizedAccessException.class)
    public ProblemDetail handleUnauthorizedAccessException(UnauthorizedAccessException ex, HttpServletRequest request) {
        return GlobalProblemDetail.of(HttpStatus.FORBIDDEN, List.of(ex.getMessage()), request.getRequestURI());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex,
                                                                   HttpServletRequest request) {
        MethodParameter parameter = ex.getParameter();
        return GlobalProblemDetail.of(HttpStatus.BAD_REQUEST,
                List.of("[ERROR] " + parameter.getParameterName() + " 값의 형식이 올바르지 않습니다."),
                request.getRequestURI());
    }
}
