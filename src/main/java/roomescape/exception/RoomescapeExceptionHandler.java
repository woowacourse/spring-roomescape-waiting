package roomescape.exception;

import static roomescape.exception.ExceptionType.INVALID_DATE_TIME_FORMAT;

import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import roomescape.dto.ErrorResponse;

@ControllerAdvice
public class RoomescapeExceptionHandler {

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handle(HttpMessageNotReadableException e) {
        e.printStackTrace();
        return ResponseEntity.status(INVALID_DATE_TIME_FORMAT.getStatus())
                .body(new ErrorResponse(INVALID_DATE_TIME_FORMAT.getMessage()));
    }

    @ExceptionHandler(RoomescapeException.class)
    public ResponseEntity<ErrorResponse> handle(RoomescapeException e) {
        e.printStackTrace();
        return ResponseEntity
                .status(e.getHttpStatus())
                .body(new ErrorResponse(e.getMessage()));
    }
}
