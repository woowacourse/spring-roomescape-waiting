package roomescape.config;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration(proxyBeanMethods = false)
public class TestTimeConfig {

    @Bean
    @Primary
    public Clock testClock() {
        return Clock.fixed(
                Instant.parse("2026-05-12T01:00:00Z"),
                ZoneId.of("Asia/Seoul")
        );
    }
}
