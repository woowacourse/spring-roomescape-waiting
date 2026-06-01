package roomescape.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import roomescape.exception.code.CommonErrorCode;
import roomescape.exception.response.ErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        CommonErrorCode errorCode = CommonErrorCode.INVALID_REQUEST_BODY;
        String message = extractFieldErrorMessage(exception);
        log.warn("요청 검증 실패: path={}, errorCode={}, message={}",
                getPath(request), errorCode.getCode(), message);

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(new ErrorResponse(errorCode.getCode(), message));
    }

    private String extractFieldErrorMessage(MethodArgumentNotValidException exception) {
        return exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(FieldError::getDefaultMessage)
                .orElse(CommonErrorCode.INVALID_REQUEST_BODY.getMessage());
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException exception,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        CommonErrorCode errorCode = CommonErrorCode.INVALID_REQUEST_BODY;
        log.warn("요청 본문 파싱 실패: path={}, errorCode={}",
                getPath(request), errorCode.getCode());

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(new ErrorResponse(errorCode));
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(
            TypeMismatchException exception,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        CommonErrorCode errorCode = CommonErrorCode.INVALID_REQUEST_PARAMETER_TYPE;
        log.warn("요청 파라미터 타입 불일치: path={}, errorCode={}, parameter={}",
                getPath(request), errorCode.getCode(), exception.getPropertyName());

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(new ErrorResponse(errorCode));
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception exception,
            Object body,
            HttpHeaders headers,
            HttpStatusCode statusCode,
            WebRequest request
    ) {
        if (!isAlreadyLogged(exception)) {
            log.warn("스프링 기본 예외 처리: path={}, status={}, exception={}, message={}",
                    getPath(request),
                    statusCode.value(),
                    exception.getClass().getSimpleName(),
                    exception.getMessage());
        }
        return ResponseEntity
                .status(statusCode)
                .body(new ErrorResponse(HttpStatus.valueOf(statusCode.value()).name(), exception.getMessage()));
    }

    private boolean isAlreadyLogged(Exception exception) {
        return exception instanceof MethodArgumentNotValidException
                || exception instanceof HttpMessageNotReadableException
                || exception instanceof TypeMismatchException;
    }

    private String getPath(WebRequest request) {
        return request.getDescription(false).replace("uri=", "");
    }

    @ExceptionHandler(RoomescapeException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(RoomescapeException roomescapeException) {
        ErrorCode errorCode = roomescapeException.getExceptionCode();
        log.warn("비즈니스 예외 발생: errorCode={}, message={}",
                errorCode.getCode(), roomescapeException.getMessage());

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(new ErrorResponse(errorCode));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception exception, HttpServletRequest request) {
        log.error("예상하지 못한 서버 오류 발생: method={}, path={}",
                request.getMethod(), request.getRequestURI(), exception);
        return ResponseEntity
                .status(CommonErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus())
                .body(new ErrorResponse(CommonErrorCode.INTERNAL_SERVER_ERROR));
    }
}
