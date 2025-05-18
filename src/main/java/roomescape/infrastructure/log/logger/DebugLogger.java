package roomescape.infrastructure.log.logger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DebugLogger implements RoomEscapeLog {
    
    private static final Logger log = LoggerFactory.getLogger(DebugLogger.class);

    @Override
    public void printLog(String message) {
        log.info("[LOG] {}", message);
    }
}
