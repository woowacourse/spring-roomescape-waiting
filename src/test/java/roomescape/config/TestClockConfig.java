package roomescape.config;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TestClockConfig {
    @Bean
    Clock clock() {
        return Clock.fixed(
                Instant.parse("2026-01-01T01:00:00Z"),
                ZoneId.of("Asia/Seoul")
        );
    }
}
