package roomescape.global.exception;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;
import java.util.Objects;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import roomescape.global.exception.customException.RoomEscapeException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Map<Class<?>, String> TYPE_HINTS = Map.of(
            LocalDate.class, "yyyy-MM-dd",
            LocalTime.class, "HH:mm",
            Long.class, "숫자",
            Integer.class, "숫자"
    );

    @ExceptionHandler(RoomEscapeException.class)
    public ResponseEntity<ErrorResponse> handleRoomEscape(RoomEscapeException e) {
        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity.status(errorCode.getStatus())
                .body(new ErrorResponse(e.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRequestBody(HttpMessageNotReadableException e) {
        if (e.getCause() instanceof InvalidFormatException invalidFormatException) {
            String fieldName = getFieldName(invalidFormatException);
            String typeHint = getExpectedType(invalidFormatException.getTargetType());
            String message = "%s 값의 형식이 올바르지 않습니다. %s 형식으로 입력해 주세요."
                    .formatted(fieldName, typeHint);
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse(message));
        }
        return createErrorResponse(CommonErrorCode.INVALID_REQUEST_BODY);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException e
    ) {
        String parameter = e.getName();
        Object value = e.getValue();
        String expected = getExpectedType(e.getRequiredType());
        String message = "%s는 %s 형식이어야 합니다. 입력값=%s"
                .formatted(parameter, expected, value);
        return ResponseEntity.badRequest()
                .body(new ErrorResponse(message));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .orElse(CommonErrorCode.INVALID_REQUEST_BODY.getMessage());

        return ResponseEntity.badRequest()
                .body(new ErrorResponse(message));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException e) {
        String message = "이미 존재하는 예약 입니다.";
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        return createErrorResponse(CommonErrorCode.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ErrorResponse> createErrorResponse(ErrorCode errorCode) {
        return ResponseEntity.status(errorCode.getStatus())
                .body(new ErrorResponse(errorCode.getMessage()));
    }

    private String getFieldName(InvalidFormatException e) {
        return e.getPath().stream()
                .map(JsonMappingException.Reference::getFieldName)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse("요청 값");
    }

    private String getExpectedType(Class<?> type) {
        if (type == null) {
            return "올바른 형식";
        }
        return TYPE_HINTS.getOrDefault(type, type.getSimpleName());
    }
}
