package roomescape.global.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import roomescape.global.util.DateTimeService;

@Configuration
public class ClockConfig {

    @Bean
    public DateTimeService dateTimeService() {
        return new DateTimeService(Clock.systemDefaultZone());
    }
}
