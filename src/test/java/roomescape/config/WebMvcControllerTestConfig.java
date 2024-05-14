package roomescape.config;

import static org.mockito.Mockito.mock;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import roomescape.auth.JwtTokenProvider;

@TestConfiguration
public class WebMvcControllerTestConfig {

    @Bean
    public JwtTokenProvider jwtTokenProvider() {
        return mock(JwtTokenProvider.class);
    }

    @Bean
    public LoginArgumentResolver loginArgumentResolver() {
        return mock(LoginArgumentResolver.class);
    }
}
