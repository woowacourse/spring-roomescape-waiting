package roomescape.config;

import static org.mockito.Mockito.mock;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import roomescape.auth.TokenProvider;

@TestConfiguration
public class ControllerConfig {

    @Bean
    public TokenProvider jwtTokenProvider() {
        return mock(TokenProvider.class);
    }

    @Bean
    public LoginArgumentResolver loginArgumentResolver() {
        return mock(LoginArgumentResolver.class);
    }
}
