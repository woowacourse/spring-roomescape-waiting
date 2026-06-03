package roomescape.config;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class FixedClockConfig {

    @Bean
    public Clock fixedClock() {
        return Clock.fixed(
                Instant.parse("2026-08-05T01:00:00Z"),
                ZoneId.of("Asia/Seoul")
        );
    }
}
