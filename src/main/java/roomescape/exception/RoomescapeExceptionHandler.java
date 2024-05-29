package roomescape.exception;

import static roomescape.exception.ExceptionType.INVALID_DATE_TIME_FORMAT;
import static roomescape.exception.ExceptionType.NO_QUERY_PARAMETER;
import static roomescape.exception.ExceptionType.UN_EXPECTED_ERROR;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class RoomescapeExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(RoomescapeExceptionHandler.class);

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ErrorResponse handle(HttpMessageNotReadableException e) {
        logger.error("{}{}{}", INVALID_DATE_TIME_FORMAT.getLogMessage(), System.lineSeparator(), e.getMessage());
        return ErrorResponse.builder(e, INVALID_DATE_TIME_FORMAT.getStatus(), INVALID_DATE_TIME_FORMAT.getMessage())
                .build();
    }

    @ExceptionHandler(RoomescapeException.class)
    public ErrorResponse handle(RoomescapeException e) {
        logger.error(e.getLogMessage());
        return ErrorResponse.builder(e, e.getHttpStatus(), e.getMessage())
                .build();
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ErrorResponse handle(MissingServletRequestParameterException e) {
        logger.error("{}{}{}", NO_QUERY_PARAMETER.getLogMessage(), System.lineSeparator(), e.getMessage());
        return ErrorResponse.builder(e, NO_QUERY_PARAMETER.getStatus(), NO_QUERY_PARAMETER.getMessage())
                .build();
    }

    @ExceptionHandler(Exception.class)
    public ErrorResponse handle(Exception e) {
        logger.error("{}{}{}", UN_EXPECTED_ERROR.getLogMessage(), System.lineSeparator(), e.getMessage());
        return ErrorResponse.builder(e, UN_EXPECTED_ERROR.getStatus(), UN_EXPECTED_ERROR.getMessage())
                .build();
    }
}
