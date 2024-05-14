package roomescape.global.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import roomescape.global.dto.response.ErrorResponse;
import roomescape.global.exception.error.ErrorType;
import roomescape.global.exception.model.AssociatedDataExistsException;
import roomescape.global.exception.model.CustomException;
import roomescape.global.exception.model.DataDuplicateException;
import roomescape.global.exception.model.ForbiddenException;
import roomescape.global.exception.model.NotFoundException;
import roomescape.global.exception.model.UnauthorizedException;
import roomescape.global.exception.model.ValidateException;

@RestControllerAdvice
public class ExceptionControllerAdvice {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @ExceptionHandler(value = {NotFoundException.class, ValidateException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleNotFoundException(final CustomException e) {
        logger.error(e.getMessage(), e);
        return ErrorResponse.of(e.getErrorType(), e.getMessage());
    }

    @ExceptionHandler(value = HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleHttpMessageNotReadableException(final HttpMessageNotReadableException e) {
        logger.error(e.getMessage(), e);
        return ErrorResponse.of(ErrorType.INVALID_REQUEST_DATA_TYPE, ErrorType.INVALID_REQUEST_DATA_TYPE.getDescription());
    }

    @ExceptionHandler(value = UnauthorizedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleUnauthorizedException(final UnauthorizedException e) {
        logger.error(e.getMessage(), e);
        return ErrorResponse.of(e.getErrorType(), e.getMessage());
    }

    @ExceptionHandler(value = ForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleForbiddenException(final ForbiddenException e) {
        logger.error(e.getMessage(), e);
        return ErrorResponse.of(e.getErrorType(), e.getMessage());
    }

    @ExceptionHandler(value = HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public ErrorResponse handleHttpRequestMethodNotSupportedException(final HttpRequestMethodNotSupportedException e) {
        logger.error(e.getMessage(), e);
        return ErrorResponse.of(ErrorType.METHOD_NOT_ALLOWED, ErrorType.METHOD_NOT_ALLOWED.getDescription());
    }

    @ExceptionHandler(value = {
            DataDuplicateException.class, AssociatedDataExistsException.class
    })
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleConflictException(final CustomException e) {
        logger.error(e.getMessage(), e);
        return ErrorResponse.of(e.getErrorType(), e.getMessage());
    }

    @ExceptionHandler(value = Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleException(final Exception e) {
        logger.error(e.getMessage(), e);
        return ErrorResponse.of(ErrorType.INTERNAL_SERVER_ERROR, ErrorType.INTERNAL_SERVER_ERROR.getDescription());
    }
}
