package roomescape.common.config;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class FixedClockConfig {
    public static final String TODAY = "2026-05-10";
    public static final String NOW_TIME = "09:00";
    public static final String FUTURE_DATE = "2026-05-12";

    @Bean
    @Primary
    public Clock testClock() {
        return Clock.fixed(
                Instant.parse("2026-05-10T00:00:00Z"),
                ZoneId.of("Asia/Seoul")
        );
    }
}
