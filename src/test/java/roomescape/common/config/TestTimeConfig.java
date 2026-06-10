package roomescape.common.config;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class TestTimeConfig {

    @Bean
    @Primary
    public Clock timeClock() {
        return Clock.fixed(
                Instant.parse("2026-05-07T00:00:00Z"),
                ZoneId.of("UTC")
        );
    }
}
