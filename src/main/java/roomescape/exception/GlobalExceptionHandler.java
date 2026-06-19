package roomescape.exception;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import roomescape.infrastructure.payment.TossPaymentException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final ErrorTypeHttpStatusMapper statusMapper = new ErrorTypeHttpStatusMapper();

    @ExceptionHandler(RoomescapeException.class)
    public ResponseEntity<ErrorResponse> handleRoomescapeException(RoomescapeException e) {
        return response(e.getErrorType(), e.getMessage());
    }

    @ExceptionHandler(TossPaymentException.class)
    public ResponseEntity<ErrorResponse> handleTossPaymentException(TossPaymentException e) {
        return ResponseEntity.status(e.getStatus())
                .body(new ErrorResponse(e.getCode(), e.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleBodyParseError(HttpMessageNotReadableException e) {
        return bodyParseErrorResponse(e);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleParamTypeMismatch(MethodArgumentTypeMismatchException e) {
        return malformed(e.getName(), e.getValue(), e.getRequiredType());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingRequestParameter(MissingServletRequestParameterException e) {
        return badRequest("필수 요청 파라미터 '" + e.getParameterName() + "'이(가) 누락되었습니다.");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationFailure(MethodArgumentNotValidException e) {
        return validationErrorResponse(e);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException e) {
        return constraintViolationErrorResponse(e);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFound(NoResourceFoundException e) {
        return response(ErrorType.ENDPOINT_NOT_FOUND, "요청한 경로를 찾을 수 없습니다.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(Exception e) {
        return internalServerErrorResponse();
    }

    private ResponseEntity<ErrorResponse> bodyParseErrorResponse(HttpMessageNotReadableException e) {
        if (e.getCause() instanceof InvalidFormatException ife) {
            return malformed(lastPathField(ife), ife.getValue(), ife.getTargetType());
        }
        return badRequest("요청 본문 형식이 올바르지 않습니다.");
    }

    private ResponseEntity<ErrorResponse> validationErrorResponse(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(this::formatFieldError)
                .collect(Collectors.joining(", "));
        return badRequest(message);
    }

    private ResponseEntity<ErrorResponse> constraintViolationErrorResponse(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .map(this::formatConstraintViolation)
                .collect(Collectors.joining(", "));
        return badRequest(message);
    }

    private ResponseEntity<ErrorResponse> internalServerErrorResponse() {
        return response(ErrorType.INTERNAL_SERVER_ERROR, "알 수 없는 서버 에러가 발생했습니다. 잠시 후 다시 시도해주세요.");
    }

    private ResponseEntity<ErrorResponse> badRequest(String message) {
        return response(ErrorType.INVALID_REQUEST, message);
    }

    private ResponseEntity<ErrorResponse> response(ErrorType errorType, String message) {
        return ResponseEntity.status(statusMapper.statusOf(errorType))
                .body(new ErrorResponse(errorType.name(), message));
    }

    private ResponseEntity<ErrorResponse> malformed(String field, Object value, Class<?> type) {
        String message = String.format(
                "'%s' 값 '%s'은(는) %s 형식이어야 합니다.",
                field, value, SupportedRequestFormat.describe(type));
        return badRequest(message);
    }

    private String formatFieldError(FieldError error) {
        ConstraintViolation<?> violation = error.unwrap(ConstraintViolation.class);
        return ConstraintMessage.describe(
                error.getField(),
                violation.getConstraintDescriptor().getAnnotation().annotationType(),
                violation.getConstraintDescriptor().getAttributes());
    }

    private String formatConstraintViolation(ConstraintViolation<?> violation) {
        return ConstraintMessage.describe(
                lastPathSegment(violation.getPropertyPath()),
                violation.getConstraintDescriptor().getAnnotation().annotationType(),
                violation.getConstraintDescriptor().getAttributes());
    }

    private String lastPathSegment(Path path) {
        String full = path.toString();
        int idx = full.lastIndexOf('.');
        return idx < 0 ? full : full.substring(idx + 1);
    }

    private String lastPathField(InvalidFormatException ife) {
        var path = ife.getPath();
        if (path.isEmpty()) {
            return "?";
        }
        JsonMappingException.Reference last = path.get(path.size() - 1);
        return last.getFieldName();
    }
}
