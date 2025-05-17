package roomescape.common;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import roomescape.application.CurrentTimeService;
import roomescape.fake.FixedCurrentTimeService;

@TestConfiguration
public class TestConfig {

    @Bean
    public CurrentTimeService currentTimeService() {
        return new FixedCurrentTimeService();
    }
}
