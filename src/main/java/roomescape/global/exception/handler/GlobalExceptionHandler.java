package roomescape.global.exception.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import roomescape.global.exception.BadRequestException;
import roomescape.global.exception.BusinessException;
import roomescape.global.exception.response.ErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String INTERNAL_SERVER_ERROR_MESSAGE = "서버 내부 예외가 발생했습니다. 잠시 후 다시 시도해 주세요.";
    private static final String MISSING_PARAMETER_MESSAGE = "필수 요청 파라미터가 누락되었습니다. 입력 값을 다시 확인해 주세요.";
    private static final String EMPTY_REQUEST_BODY_MESSAGE = "요청 본문이 비어있습니다. 전송하려는 데이터를 확인해 주세요.";
    private static final String INVALID_PATH_MESSAGE = "잘못된 경로입니다. 요청하신 주소가 올바른지 확인해 주세요.";
    private static final String METHOD_NOT_ALLOWED_MESSAGE = "지원하지 않는 HTTP 메서드입니다. 올바른 요청 방식인지 확인해 주세요.";
    private static final String UNSUPPORTED_MEDIA_TYPE_MESSAGE = "지원하지 않는 미디어 타입입니다. 전송하는 데이터의 형식을 확인해 주세요.";
    private static final String TYPE_MISMATCH_MESSAGE = "요청 파라미터 타입이 일치하지 않습니다. 입력 값의 형식을 확인해 주세요.";

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception e) {
        return ResponseEntity
                .internalServerError()
                .body(new ErrorResponse(INTERNAL_SERVER_ERROR_MESSAGE));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<?> handleMissingServletRequestParameter(
            MissingServletRequestParameterException e
    ) {
        return ResponseEntity
                .badRequest()
                .body(new ErrorResponse(MISSING_PARAMETER_MESSAGE));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        return ResponseEntity
                .status(e.getStatus())
                .body(ErrorResponse.of(e));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        if (e.getMostSpecificCause() instanceof BusinessException businessException) {
            return handleBusinessException(businessException);
        }
        return handleBusinessException(new BadRequestException(EMPTY_REQUEST_BODY_MESSAGE));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(NoResourceFoundException e) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(INVALID_PATH_MESSAGE));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupportedException(
            HttpRequestMethodNotSupportedException e) {
        return ResponseEntity
                .status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(new ErrorResponse(METHOD_NOT_ALLOWED_MESSAGE));
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleHttpMediaTypeNotSupportedException(
            HttpMediaTypeNotSupportedException e) {
        return ResponseEntity
                .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .body(new ErrorResponse(UNSUPPORTED_MEDIA_TYPE_MESSAGE));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException e) {
        return ResponseEntity
                .badRequest()
                .body(new ErrorResponse(TYPE_MISMATCH_MESSAGE));
    }
}
