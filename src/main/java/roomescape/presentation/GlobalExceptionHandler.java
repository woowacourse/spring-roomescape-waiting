package roomescape.presentation;

import static java.util.stream.Collectors.toMap;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import com.fasterxml.jackson.databind.JsonMappingException.Reference;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import roomescape.application.AuthorizationException;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(final MethodArgumentNotValidException ex, final HttpHeaders headers, final HttpStatusCode status, final WebRequest request) {
        var problemDetail = ProblemDetail.forStatusAndDetail(ex.getStatusCode(), "유효성 검증에 실패했습니다.");
        var fieldErrors = ex.getFieldErrors()
            .stream()
            .collect(toMap(FieldError::getField, err -> (Object) err.getDefaultMessage()));
        problemDetail.setProperties(Map.of("message", fieldErrors));
        return ResponseEntity.badRequest().body(problemDetail);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(final HttpMessageNotReadableException ex, final HttpHeaders headers, final HttpStatusCode status, final WebRequest request) {
        var problemDetail = ProblemDetail.forStatusAndDetail(BAD_REQUEST, "해석할 수 없는 요청입니다.");
        if (ex.getCause() instanceof InvalidFormatException ife) {
            var invalidFields = ife.getPath().stream().collect(toMap(Reference::getFieldName, r -> ife.getValue()));
            problemDetail.setProperties(Map.of("message", invalidFields));
        }
        return ResponseEntity.badRequest().body(problemDetail);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(code = BAD_REQUEST)
    public ProblemDetail handleIllegalArgument(final IllegalArgumentException ex) {
        var problemDetail = ProblemDetail.forStatusAndDetail(BAD_REQUEST, "잘못된 요청 매개변수입니다.");
        problemDetail.setProperties(Map.of("message", ex.getMessage()));
        return problemDetail;
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(code = BAD_REQUEST)
    public ProblemDetail handleIllegalState(final IllegalStateException ex) {
        var problemDetail = ProblemDetail.forStatusAndDetail(BAD_REQUEST, "요청을 처리하는 과정에서 실패했습니다.");
        problemDetail.setProperties(Map.of("message", ex.getMessage()));
        return problemDetail;
    }

    @ExceptionHandler(AuthorizationException.class)
    @ResponseStatus(code = UNAUTHORIZED)
    public ProblemDetail handleAuthentication(final AuthorizationException ex) {
        var problemDetail = ProblemDetail.forStatusAndDetail(UNAUTHORIZED, "인증에 실패했습니다.");
        problemDetail.setProperties(Map.of("message", ex.getMessage()));
        return problemDetail;
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(code = INTERNAL_SERVER_ERROR)
    public ProblemDetail handleException(final Exception ex) {
        return ProblemDetail.forStatusAndDetail(INTERNAL_SERVER_ERROR, "예기치 못한 오류가 발생했습니다.");
    }
}
