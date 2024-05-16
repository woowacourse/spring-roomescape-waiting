package global;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import roomescape.RoomescapeApplication;

@RestControllerAdvice
public abstract class AbstractExceptionHandler {
    protected final Logger log = LoggerFactory.getLogger(RoomescapeApplication.class);

    protected void logError(Exception exception) {
        log.error("Error occur {}", exception);
    }
}
