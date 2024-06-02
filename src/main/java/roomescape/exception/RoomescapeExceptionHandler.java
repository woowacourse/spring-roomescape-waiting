package roomescape.exception;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class RoomescapeExceptionHandler {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @ExceptionHandler(RoomescapeException.class)
    public ResponseEntity<ExceptionResponse> handleRoomescapeException(RoomescapeException e) {
        logger.error(e.getMessage(), e);
        return ResponseEntity.status(e.getHttpStatusCode())
                .body(new ExceptionResponse(e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponse> handelError(Exception e) {
        logger.error(e.getMessage(), e);
        return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                .body(new ExceptionResponse(INTERNAL_SERVER_ERROR.getReasonPhrase()));
    }
}
