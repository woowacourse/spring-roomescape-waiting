package roomescape.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import roomescape.infra.JdbcWaitlistRepository;
import roomescape.service.FailingWaitlistRepository;

@TestConfiguration
public class FailureInjectionConfig {
    @Bean
    @Primary
    public FailingWaitlistRepository failingWaitlistRepository(JdbcWaitlistRepository delegate) {
        return new FailingWaitlistRepository(delegate);
    }
}
