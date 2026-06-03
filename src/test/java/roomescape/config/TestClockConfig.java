package roomescape.config;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class TestClockConfig {

    public static final ZoneId TEST_ZONE = ZoneId.of("Asia/Seoul");
    public static final Instant FIXED_INSTANT = Instant.parse("2026-01-01T01:00:00Z");
    public static final LocalDateTime FIXED_NOW = LocalDateTime.ofInstant(FIXED_INSTANT, TEST_ZONE);

    @Bean
    @Primary
    Clock fixedTestClock() {
        return Clock.fixed(FIXED_INSTANT, TEST_ZONE);
    }
}
