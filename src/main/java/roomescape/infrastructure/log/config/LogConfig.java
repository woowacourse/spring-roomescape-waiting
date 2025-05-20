package roomescape.infrastructure.log.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import roomescape.infrastructure.log.logger.DebugLogger;
import roomescape.infrastructure.log.logger.PublishLog;
import roomescape.infrastructure.log.logger.RoomEscapeLog;

@Configuration
public class LogConfig {

    @Bean
    @Profile("!test")
    public RoomEscapeLog roomEscapeLog() {
        return new PublishLog();
    }

    @Bean
    @Profile("test")
    public RoomEscapeLog debugLog() {
        return new DebugLogger();
    }
}
