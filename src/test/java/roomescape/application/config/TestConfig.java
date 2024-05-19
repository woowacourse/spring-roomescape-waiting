package roomescape.application.config;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import roomescape.application.auth.JwtTokenManager;
import roomescape.application.auth.TokenManager;
import roomescape.application.auth.dto.JwtProperties;

@TestConfiguration
public class TestConfig {

    @Autowired
    private JwtProperties jwtProperties;

    @Bean
    @Primary
    public Clock testClock() {
        return Clock.fixed(Instant.parse("2000-01-01T00:00:00Z"), ZoneId.systemDefault());
    }

    @Bean
    @Primary
    public TokenManager testTokenManager() {
        return new JwtTokenManager(jwtProperties, testClock());
    }
}
