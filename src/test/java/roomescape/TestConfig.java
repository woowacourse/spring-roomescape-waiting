package roomescape;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TestConfig {

    @Bean
    public Clock clock() {
        ZoneId zoneId = ZoneId.of("Asia/Seoul");

        return Clock.fixed(
                LocalDateTime.of(2026, 5, 5, 9, 0)
                        .atZone(zoneId)
                        .toInstant(),
                zoneId
        );
    }
}
