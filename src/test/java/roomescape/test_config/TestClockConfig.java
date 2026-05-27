package roomescape.test_config;

import java.time.Clock;
import java.time.ZoneId;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class TestClockConfig {

    @Bean
    @Primary
    public MutableClock mutableClock() {
        return new MutableClock(Clock.system(ZoneId.of("Asia/Seoul")));
    }
}
