package roomescape.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import roomescape.common.dto.ErrorResponse;
import roomescape.payment.exception.PaymentGatewayConfigurationException;
import roomescape.payment.exception.RetryablePaymentException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        ErrorType errorType = e.getErrorType();

        if (e instanceof PaymentGatewayConfigurationException) {
            log.error("[결제 설정 오류] Toss Payments API 키 설정을 확인해야 합니다.", e);
        } else if (e instanceof RetryablePaymentException) {
            log.warn("[결제 일시 오류] Toss Payments 승인 요청을 재시도할 수 있습니다.", e);
        }

        return ResponseEntity.status(errorType.getHttpStatus())
                .body(new ErrorResponse(
                        errorType.getErrorMessage(),
                        errorType.getErrorCode()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgument(MethodArgumentNotValidException e) {
        ErrorType errorType = ErrorType.METHOD_ARGUMENT_NOT_VALID;

        return ResponseEntity.status(errorType.getHttpStatus())
                .body(new ErrorResponse(
                        errorType.getErrorMessage(),
                        errorType.getErrorCode()));
    }

    @ExceptionHandler(MissingPathVariableException.class)
    public ResponseEntity<ErrorResponse> handleMissingPathVariable(MissingPathVariableException e) {
        ErrorType errorType = ErrorType.MISSING_PATH_VARIABLE;

        return ResponseEntity.status(errorType.getHttpStatus())
                .body(new ErrorResponse(
                        errorType.getErrorMessage(),
                        errorType.getErrorCode()));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameter(
            MissingServletRequestParameterException e) {
        ErrorType errorType = ErrorType.MISSING_REQUEST_PARAMETER;

        return ResponseEntity.status(errorType.getHttpStatus())
                .body(new ErrorResponse(
                        errorType.getErrorMessage(),
                        errorType.getErrorCode()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        ErrorType errorType = ErrorType.HTTP_MESSAGE_NOT_READABLE;

        return ResponseEntity.status(errorType.getHttpStatus())
                .body(new ErrorResponse(
                        errorType.getErrorMessage(),
                        errorType.getErrorCode()));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException e) {
        ErrorType errorType = ErrorType.METHOD_ARGUMENT_TYPE_MISMATCH;

        return ResponseEntity.status(errorType.getHttpStatus())
                .body(new ErrorResponse(
                        errorType.getErrorMessage(),
                        errorType.getErrorCode()));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException e) {
        ErrorType errorType = ErrorType.CONSTRAINT_VIOLATION;

        return ResponseEntity.status(errorType.getHttpStatus())
                .body(new ErrorResponse(
                        errorType.getErrorMessage(),
                        errorType.getErrorCode()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(
            Exception e,
            HttpServletRequest request) {
        log.error("[예기치 못한 오류] {} {}", request.getMethod(), request.getRequestURI(), e);

        ErrorType errorType = ErrorType.UNEXPECTED_EXCEPTION;
        return ResponseEntity.status(errorType.getHttpStatus())
                .body(new ErrorResponse(
                        errorType.getErrorMessage(),
                        errorType.getErrorCode()));
    }
}
