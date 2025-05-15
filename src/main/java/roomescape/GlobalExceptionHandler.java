package roomescape;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import roomescape.exception.DuplicateException;
import roomescape.exception.InvalidDateAndTimeException;
import roomescape.exception.NotFoundException;
import roomescape.exception.UnauthorizedException;

@ControllerAdvice
public class GlobalExceptionHandler {

    private final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Void> handleNotFoundException(final NotFoundException e) {
        logger.warn("NotFoundException occurred", e);

        return ResponseEntity.notFound()
                .build();
    }

    @ExceptionHandler(DuplicateException.class)
    public ResponseEntity<Void> handleDuplicateException(final DuplicateException e) {
        logger.warn("DuplicateException occurred", e);

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Void> handleIllegalArgumentException(final IllegalArgumentException e) {
        logger.warn("IllegalArgumentException occurred", e);

        return ResponseEntity.badRequest()
                .build();
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Void> handleUnauthorizedException(final UnauthorizedException e) {
        logger.warn("UnauthorizedException occurred", e);

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .build();
    }

    @ExceptionHandler(InvalidDateAndTimeException.class)
    public ResponseEntity<Void> handleInvalidDateAndTimeException(final InvalidDateAndTimeException e) {
        logger.warn("InvalidDateAndTimeException occurred", e);

        return ResponseEntity.badRequest()
                .build();
    }
}
