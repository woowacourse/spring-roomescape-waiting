package roomescape.util;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TestConfig {
    @Bean
    public Clock fixedClock() {
        return Clock.fixed(Instant.parse("2024-05-10T00:00:00Z"), ZoneId.of("Asia/Seoul"));
    }
}
