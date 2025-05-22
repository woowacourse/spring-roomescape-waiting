package roomescape.presentation;

import static java.util.stream.Collectors.toMap;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

import com.fasterxml.jackson.databind.JsonMappingException.Reference;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import java.util.Map;
import lombok.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
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
import roomescape.exception.AlreadyExistedException;
import roomescape.exception.AuthenticationException;
import roomescape.exception.AuthorizationException;
import roomescape.exception.BusinessRuleViolationException;
import roomescape.exception.InUseException;
import roomescape.exception.InvalidInputException;
import roomescape.exception.NotFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(final MethodArgumentNotValidException ex,
                                                                  @NonNull final HttpHeaders headers,
                                                                  @NonNull final HttpStatusCode status,
                                                                  @NonNull final WebRequest request) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(ex.getStatusCode(), "유효성 검증에 실패했습니다.");
        Map<String, Object> fieldErrors = ex.getFieldErrors()
                .stream()
                .collect(toMap(FieldError::getField, err -> err.getDefaultMessage()));
        problemDetail.setProperties(Map.of("message", fieldErrors));
        return ResponseEntity.badRequest().body(problemDetail);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(final HttpMessageNotReadableException ex,
                                                                  @NonNull final HttpHeaders headers,
                                                                  @NonNull final HttpStatusCode status,
                                                                  @NonNull final WebRequest request) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(BAD_REQUEST, "해석할 수 없는 요청입니다.");
        if (ex.getCause() instanceof InvalidFormatException ife) {
            Map<String, Object> invalidFields = ife.getPath().stream()
                    .collect(toMap(Reference::getFieldName, r -> ife.getValue()));
            problemDetail.setProperties(Map.of("message", invalidFields));
        }
        return ResponseEntity.badRequest().body(problemDetail);
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(code = NOT_FOUND)
    public ProblemDetail handleNotFound(final NotFoundException ex) {
        return createProblemDetail(NOT_FOUND, "존재하지 않는 값입니다.", ex.getMessage());
    }

    @ExceptionHandler(InUseException.class)
    @ResponseStatus(code = CONFLICT)
    public ProblemDetail handleInUse(final InUseException ex) {
        return createProblemDetail(CONFLICT, "사용 중인 값입니다.", ex.getMessage());
    }

    @ExceptionHandler(AlreadyExistedException.class)
    @ResponseStatus(code = CONFLICT)
    public ProblemDetail handleAlreadyExisted(final AlreadyExistedException ex) {
        return createProblemDetail(CONFLICT, "이미 존재하는 값입니다.", ex.getMessage());
    }

    @ExceptionHandler(BusinessRuleViolationException.class)
    @ResponseStatus(code = UNPROCESSABLE_ENTITY)
    public ProblemDetail handleBusinessRuleViolation(final BusinessRuleViolationException ex) {
        return createProblemDetail(UNPROCESSABLE_ENTITY, "비즈니스 규칙을 위반합니다.", ex.getMessage());
    }

    @ExceptionHandler(AuthorizationException.class)
    @ResponseStatus(code = FORBIDDEN)
    public ProblemDetail handleAuthorization(final AuthorizationException ex) {
        return createProblemDetail(FORBIDDEN, "인가에 실패했습니다.", ex.getMessage());
    }

    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(code = UNAUTHORIZED)
    public ProblemDetail handleAuthentication(final AuthenticationException ex) {
        return createProblemDetail(UNAUTHORIZED, "인증에 실패했습니다.", ex.getMessage());
    }

    @ExceptionHandler(InvalidInputException.class)
    @ResponseStatus(code = BAD_REQUEST)
    public ProblemDetail handleInvalidInput(final InvalidInputException ex) {
        return createProblemDetail(BAD_REQUEST, "올바르지 못한 입력입니다.", ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(code = INTERNAL_SERVER_ERROR)
    public ProblemDetail handleException(final Exception ex) {
        return ProblemDetail.forStatusAndDetail(INTERNAL_SERVER_ERROR, "예기치 못한 오류가 발생했습니다.");
    }

    private ProblemDetail createProblemDetail(final HttpStatus status, final String detail,
                                              final String exceptionMessage) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
        problemDetail.setProperties(Map.of("message", exceptionMessage));
        return problemDetail;
    }
}
