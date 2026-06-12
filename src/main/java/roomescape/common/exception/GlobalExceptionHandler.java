package roomescape.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String BAD_REQUEST_MESSAGE = "잘못된 요청입니다.";
    private static final String NO_RESOURCE_FOUND_MESSAGE = "존재하지 않는 경로입니다.";
    private static final String METHOD_NOT_SUPPORTED_MESSAGE = "지원하지 않는 HTTP 메서드입니다.";
    private static final String MEDIA_TYPE_NOT_SUPPORTED_MESSAGE = "지원하지 않는 Content-Type입니다.";
    private static final String INTERNAL_SERVER_ERROR_MESSAGE = "서버 내부 오류가 발생했습니다.";

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NotFoundException.class)
    public ErrorResponse handleNotFoundException(final NotFoundException exception) {
        return new ErrorResponse(exception.getMessage());
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(ConflictException.class)
    public ErrorResponse handleConflictException(final ConflictException exception) {
        return new ErrorResponse(exception.getMessage());
    }

    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    @ExceptionHandler(UnprocessableContentException.class)
    public ErrorResponse handleUnprocessableContentException(final UnprocessableContentException exception) {
        return new ErrorResponse(exception.getMessage());
    }

    // JSON 본문 파싱 실패, 파라미터 타입 변환 실패, 필수 쿼리 파라미터 누락
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class
    })
    public ErrorResponse handleBadRequestException(final Exception exception) {
        return new ErrorResponse(BAD_REQUEST_MESSAGE);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ErrorResponse handleMethodArgumentNotValidException(final MethodArgumentNotValidException exception) {
        final String message = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(FieldError::getDefaultMessage)
                .orElse(BAD_REQUEST_MESSAGE);

        return new ErrorResponse(message);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(IllegalArgumentException.class)
    public ErrorResponse handleIllegalArgumentException(final IllegalArgumentException exception) {
        return new ErrorResponse(exception.getMessage());
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(IllegalStateException.class)
    public ErrorResponse handleIllegalStateException(final IllegalStateException exception) {
        return new ErrorResponse(exception.getMessage());
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NoResourceFoundException.class)
    public ErrorResponse handleNoResourceFoundException(final NoResourceFoundException exception) {
        return new ErrorResponse(NO_RESOURCE_FOUND_MESSAGE);
    }

    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ErrorResponse handleHttpRequestMethodNotSupportedException(final HttpRequestMethodNotSupportedException exception) {
        return new ErrorResponse(METHOD_NOT_SUPPORTED_MESSAGE);
    }

    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ErrorResponse handleHttpMediaTypeNotSupportedException(final HttpMediaTypeNotSupportedException exception) {
        return new ErrorResponse(MEDIA_TYPE_NOT_SUPPORTED_MESSAGE);
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ErrorResponse handleException(final Exception exception, final HttpServletRequest request) {
        log.error(
                "예상치 못한 예외가 발생했습니다. method={}, uri={}",
                request.getMethod(),
                request.getRequestURI(),
                exception
        );

        return new ErrorResponse(INTERNAL_SERVER_ERROR_MESSAGE);
    }
}
