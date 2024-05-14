package roomescape.global.presentation;

import com.fasterxml.jackson.databind.JsonMappingException.Reference;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import roomescape.auth.exception.AuthorizationException;
import roomescape.global.exception.NotFoundException;
import roomescape.global.exception.ViolationException;
import roomescape.global.dto.ErrorResponse;

import java.util.Objects;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    private final Logger log = LoggerFactory.getLogger(getClass().getSimpleName());

    @Override
    public ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException e, HttpHeaders headers,
                                                               HttpStatusCode status, WebRequest request) {
        logErrorMessage(e);
        String errorMessage = Objects.requireNonNull(e.getBindingResult().getFieldError())
                .getDefaultMessage();
        return ResponseEntity.badRequest()
                .body(new ErrorResponse(errorMessage));
    }

    @Override
    public ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException e, HttpHeaders headers,
                                                               HttpStatusCode status, WebRequest request) {
        logErrorMessage(e);
        if (e.getCause() instanceof InvalidFormatException invalidFormatException) {
            String errorMessage = getMismatchedInputExceptionMessage(invalidFormatException);
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse(errorMessage));
        }
        return ResponseEntity.badRequest()
                .body(new ErrorResponse(e.getMessage()));
    }

    private String getMismatchedInputExceptionMessage(InvalidFormatException e) {
        String type = e.getTargetType().toString();
        String fieldNames = e.getPath()
                .stream()
                .map(Reference::getFieldName)
                .collect(Collectors.joining(", "));
        return fieldNames + String.format("(type: %s) 필드의 값이 잘못되었습니다.", type);
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            MissingServletRequestParameterException e, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        logErrorMessage(e);
        String errorMessage = e.getParameterName() + " 파라미터가 없습니다.";
        return ResponseEntity.badRequest()
                .body(new ErrorResponse(errorMessage));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        logErrorMessage(e);
        String errorMessage = getMethodArgumentTypeMismatchExceptionMessage(e);
        return ResponseEntity.badRequest()
                .body(new ErrorResponse(errorMessage));
    }

    private String getMethodArgumentTypeMismatchExceptionMessage(MethodArgumentTypeMismatchException e) {
        String type = e.getParameter().getParameterType().toString();
        String parameterName = e.getParameter().getParameterName();
        return parameterName + String.format("(type: %s) 파라미터의 타입이 올바르지 않습니다.", type);
    }

    @ExceptionHandler(ViolationException.class)
    public ResponseEntity<ErrorResponse> handleViolationException(ViolationException e) {
        logErrorMessage(e);
        return ResponseEntity.badRequest()
                .body(new ErrorResponse(e.getMessage()));
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFoundException(NotFoundException e) {
        logErrorMessage(e);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(e.getMessage()));
    }

    @ExceptionHandler(AuthorizationException.class)
    public ResponseEntity<ErrorResponse> handleException(AuthorizationException e) {
        logErrorMessage(e);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse(e.getMessage()));
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorResponse> handleDataAccessException(DataAccessException e) {
        logErrorMessage(e);
        return ResponseEntity.internalServerError()
                .body(new ErrorResponse("DATA ACCESS ERROR"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        logErrorMessage(e);
        return ResponseEntity.internalServerError()
                .body(new ErrorResponse("SERVER ERROR"));
    }

    private void logErrorMessage(Exception e) {
        log.error("[Line number] " + e.getStackTrace()[0].getClassName() + " " + e.getStackTrace()[0].getLineNumber() +
                " [Exception] " + e.getClass() +
                " [Message] " + e.getMessage());
    }
}
