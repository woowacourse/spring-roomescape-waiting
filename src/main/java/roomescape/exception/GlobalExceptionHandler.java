package roomescape.exception;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(final ApiException exception) {
        return errorResponse(exception.getStatus(), exception.getCode(), exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
            final MethodArgumentNotValidException exception
    ) {
        FieldError fieldError = findFirstFieldError(exception);
        return buildValidationErrorResponse(fieldError);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBindException(final BindException exception) {
        FieldError fieldError = findFirstFieldError(exception);
        return buildBindingErrorResponse(fieldError);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(
            final HttpMessageNotReadableException exception
    ) {
        return buildHttpMessageNotReadableResponse(exception);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatch(
            final MethodArgumentTypeMismatchException exception
    ) {
        String code = resolveTypeMismatchCode(exception.getRequiredType());
        String message = resolveTypeMismatchMessage(exception.getRequiredType());

        return badRequest(code, message);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameter(
            final MissingServletRequestParameterException exception
    ) {
        return badRequest(
                resolveMissingParameterCode(exception.getParameterName()),
                resolveMissingParameterMessage(exception.getParameterName())
        );
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFound(final NoResourceFoundException exception) {
        return errorResponse(
                HttpStatus.NOT_FOUND,
                ErrorCode.RESOURCE_NOT_FOUND.getCode(),
                "요청한 리소스를 찾을 수 없습니다."
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
            final DataIntegrityViolationException exception
    ) {
        return errorResponse(
                HttpStatus.CONFLICT,
                ErrorCode.DATA_CONFLICT.getCode(),
                "저장 중 충돌이 발생했습니다. 잠시 후 다시 시도해 주세요."
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(final IllegalArgumentException exception) {
        return badRequest(ErrorCode.INVALID_INPUT.getCode(), exception.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(final Exception exception) {
        log.error("예상하지 못한 예외가 발생했습니다.", exception);
        return errorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorCode.INTERNAL_SERVER_ERROR.getCode(),
                "서버 내부 오류가 발생했습니다."
        );
    }

    private ResponseEntity<ErrorResponse> buildHttpMessageNotReadableResponse(
            final HttpMessageNotReadableException exception
    ) {
        Throwable cause = exception.getCause();

        if (cause instanceof InvalidFormatException invalidFormatException) {
            Class<?> targetType = invalidFormatException.getTargetType();

            return badRequest(
                    resolveTypeMismatchCode(targetType),
                    resolveTypeMismatchMessage(targetType)
            );
        }

        if (cause instanceof JsonParseException) {
            return badRequest(ErrorCode.INVALID_INPUT.getCode(), "요청 본문 JSON 형식이 올바르지 않습니다.");
        }

        if (cause instanceof MismatchedInputException) {
            return badRequest(ErrorCode.INVALID_INPUT.getCode(), "요청 값의 타입이 올바르지 않습니다.");
        }

        return badRequest(ErrorCode.INVALID_INPUT.getCode(), "요청 형식이 올바르지 않습니다.");
    }

    private ResponseEntity<ErrorResponse> buildValidationErrorResponse(final FieldError fieldError) {
        if (Objects.isNull(fieldError)) {
            return badRequest(ErrorCode.INVALID_INPUT.getCode(), "유효하지 않은 입력입니다.");
        }

        return badRequest(
                ErrorCode.INVALID_INPUT.getCode(),
                resolveValidationMessage(fieldError)
        );
    }

    private ResponseEntity<ErrorResponse> buildBindingErrorResponse(final FieldError fieldError) {
        if (Objects.isNull(fieldError)) {
            return badRequest(ErrorCode.INVALID_INPUT.getCode(), "유효하지 않은 입력입니다.");
        }

        if ("typeMismatch".equals(fieldError.getCode())) {
            return badRequest(ErrorCode.INVALID_INPUT.getCode(), fieldError.getDefaultMessage());
        }

        return badRequest(
                ErrorCode.INVALID_INPUT.getCode(),
                resolveValidationMessage(fieldError)
        );
    }

    private FieldError findFirstFieldError(final BindException exception) {
        return exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .orElse(null);
    }

    private ResponseEntity<ErrorResponse> badRequest(final String code, final String message) {
        return errorResponse(HttpStatus.BAD_REQUEST, code, message);
    }

    private ResponseEntity<ErrorResponse> errorResponse(
            final HttpStatus status,
            final String code,
            final String message
    ) {
        return ResponseEntity.status(status)
                .body(new ErrorResponse(code, status.value(), message));
    }

    private String resolveValidationMessage(final FieldError fieldError) {
        String defaultMessage = fieldError.getDefaultMessage();

        if (Objects.isNull(defaultMessage)) {
            return "유효하지 않은 입력입니다.";
        }

        return defaultMessage;
    }

    private String resolveMissingParameterCode(final String parameterName) {
        return ErrorCode.INVALID_INPUT.getCode();
    }

    private String resolveMissingParameterMessage(final String parameterName) {
        return String.format("'%s' 파라미터가 누락되었습니다.", parameterName);
    }

    private String resolveTypeMismatchCode(final Class<?> targetType) {
        return ErrorCode.INVALID_INPUT.getCode();
    }

    private String resolveTypeMismatchMessage(final Class<?> targetType) {
        if (Objects.isNull(targetType)) {
            return "요청 값의 타입이 올바르지 않습니다.";
        }

        if (LocalDate.class.equals(targetType)) {
            return "날짜 형식이 올바르지 않습니다. yyyy-MM-dd 형식이어야 합니다.";
        }

        if (LocalTime.class.equals(targetType)) {
            return "시간 형식이 올바르지 않습니다. HH:mm 형식이어야 합니다.";
        }

        if (targetType.isEnum()) {
            return "허용되지 않는 요청 값입니다.";
        }

        return "요청 값의 타입이 올바르지 않습니다.";
    }
}
